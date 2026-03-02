const express = require('express');
const cors = require('cors');
const QRCode = require('qrcode');
const { default: makeWASocket, useMultiFileAuthState, DisconnectReason, fetchLatestBaileysVersion } = require('@whiskeysockets/baileys');
const pino = require('pino');
const path = require('path');
const fs = require('fs');
const os = require('os');
const { execFile } = require('child_process');
const ffmpegPath = require('ffmpeg-static');

const app = express();
app.use(cors());
app.use(express.json({ limit: '10mb' }));

const PORT = process.env.PORT || 3001;
const AUTH_DIR = path.join(__dirname, 'auth');
const MAX_RETRIES = 5;
const BACKEND_WEBHOOK_URL = process.env.BACKEND_URL || 'http://localhost:8085/webhook/evolution';
// --- Multi-Instance State ---
// Map<instanceName, { sock, currentQR, qrBase64, connectionStatus, retryCount, manualReconnect }>
const instances = new Map();

function getInstance(name) {
  if (!instances.has(name)) {
    instances.set(name, {
      sock: null,
      currentQR: null,
      qrBase64: null,
      connectionStatus: 'disconnected',
      retryCount: 0,
      manualReconnect: false, // flag to prevent close handler from auto-reconnecting
    });
  }
  return instances.get(name);
}

// --- Baileys Connection (per instance) ---
async function connectWhatsApp(instanceName) {
  const instance = getInstance(instanceName);
  const authDir = path.join(AUTH_DIR, instanceName);

  // Crear directorio de auth si no existe
  if (!fs.existsSync(authDir)) {
    fs.mkdirSync(authDir, { recursive: true });
  }

  const { state, saveCreds } = await useMultiFileAuthState(authDir);
  const { version } = await fetchLatestBaileysVersion();

  const sock = makeWASocket({
    version,
    auth: state,
    logger: pino({ level: 'silent' }), // Puedes poner 'info' si quieres ver más detalles
    browser: ['Ubuntu', 'Chrome', '20.0.04'], // Cambiamos la firma para que WhatsApp sea más amigable
    connectTimeoutMs: 60000, // ¡CLAVE! Le damos 60 segundos a Render para procesar en vez de los 20s por defecto
    defaultQueryTimeoutMs: 0, // Evita que se corten consultas internas por lentitud
    keepAliveIntervalMs: 10000,
    syncFullHistory: false, // Evitamos que intente descargar todo el historial de chats al conectar
  });

  instance.sock = sock;

  sock.ev.on('creds.update', saveCreds);

  // --- Reenviar mensajes entrantes al backend Spring Boot ---
  sock.ev.on('messages.upsert', async ({ messages, type }) => {
    if (type !== 'notify') return;

    for (const msg of messages) {
      try {
        const jid = msg.key?.remoteJid || '';
        if (jid === 'status@broadcast' || jid.endsWith('@broadcast') || !jid) continue;

        if (jid.endsWith('@lid')) {
          console.log(`[${instanceName}] Mensaje con LID JID: ${jid}`);
        }

        let texto = null;
        let messageType = 'unknown';

        if (msg.message?.conversation) {
          texto = msg.message.conversation;
          messageType = 'conversation';
        } else if (msg.message?.extendedTextMessage?.text) {
          texto = msg.message.extendedTextMessage.text;
          messageType = 'extendedTextMessage';
        } else if (msg.message?.imageMessage?.caption) {
          texto = msg.message.imageMessage.caption;
          messageType = 'imageMessage';
        } else if (msg.message?.videoMessage?.caption) {
          texto = msg.message.videoMessage.caption;
          messageType = 'videoMessage';
        } else if (msg.message?.audioMessage || msg.message?.pttMessage) {
          messageType = msg.message?.pttMessage ? 'pttMessage' : 'audioMessage';
          texto = null;
        } else if (msg.message) {
          messageType = Object.keys(msg.message)[0] || 'unknown';
        }

        const payload = {
          event: 'messages.upsert',
          instance: instanceName,
          data: {
            key: {
              remoteJid: msg.key.remoteJid,
              fromMe: msg.key.fromMe || false,
              id: msg.key.id,
            },
            pushName: msg.pushName || null,
            message: msg.message || {},
            messageType: messageType,
            messageTimestamp: typeof msg.messageTimestamp === 'object'
              ? msg.messageTimestamp.low || Math.floor(Date.now() / 1000)
              : msg.messageTimestamp || Math.floor(Date.now() / 1000),
          },
        };

        const isGroup = jid.endsWith('@g.us');
        const direction = msg.key.fromMe ? 'SALIENTE' : 'ENTRANTE';
        console.log(`[${instanceName}] ${direction} ${isGroup ? 'GRUPO' : 'DM'} de ${msg.pushName || jid}: ${texto ? texto.substring(0, 50) : '[multimedia]'}`);

        fetch(BACKEND_WEBHOOK_URL, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        }).then(res => {
          if (!res.ok) console.error(`[${instanceName}] Backend error: ${res.status}`);
        }).catch(err => {
          console.error(`[${instanceName}] Backend no disponible: ${err.message}`);
        });

      } catch (err) {
        console.warn(`[${instanceName}] Error procesando mensaje: ${err.message}`);
      }
    }
  });

  sock.ev.on('connection.update', async (update) => {
    const { connection, lastDisconnect, qr } = update;

    if (qr) {
      instance.currentQR = qr;
      instance.qrBase64 = await QRCode.toDataURL(qr, { width: 300, margin: 2 });
      instance.connectionStatus = 'connecting';
      console.log(`[${instanceName}] QR code generado - escanea desde el frontend`);
    }

    if (connection === 'open') {
      instance.currentQR = null;
      instance.qrBase64 = null;
      instance.connectionStatus = 'open';
      instance.retryCount = 0;
      const user = sock.user;
      console.log(`[${instanceName}] Conectado como ${user?.name || user?.id || 'desconocido'}`);
    }

    if (connection === 'close') {
      instance.connectionStatus = 'disconnected';
      instance.currentQR = null;
      instance.qrBase64 = null;

      // If manual reconnect was triggered, don't auto-reconnect from here
      if (instance.manualReconnect) {
        console.log(`[${instanceName}] Socket cerrado por reconexion manual, ignorando auto-reconnect.`);
        instance.manualReconnect = false;
        return;
      }

      const statusCode = lastDisconnect?.error?.output?.statusCode;
      const isLoggedOut = statusCode === DisconnectReason.loggedOut;

      if (isLoggedOut) {
        console.log(`[${instanceName}] Sesion cerrada/expirada. Limpiando auth y reconectando para QR nuevo...`);
        const authDir = path.join(AUTH_DIR, instanceName);
        try {
          fs.rmSync(authDir, { recursive: true, force: true });
        } catch (e) { /* ignore */ }
        // Reconnect after cleanup to generate fresh QR
        instance.retryCount = 0;
        setTimeout(() => connectWhatsApp(instanceName), 2000);
      } else if (instance.retryCount < MAX_RETRIES) {
        instance.retryCount++;
        const delay = Math.min(instance.retryCount * 2000, 10000);
        console.log(`[${instanceName}] Desconectado (code: ${statusCode}). Reintentando en ${delay / 1000}s... (${instance.retryCount}/${MAX_RETRIES})`);
        setTimeout(() => connectWhatsApp(instanceName), delay);
      } else {
        console.log(`[${instanceName}] Maximo de reintentos. Limpiando auth para proximo intento.`);
        const authDir = path.join(AUTH_DIR, instanceName);
        try {
          fs.rmSync(authDir, { recursive: true, force: true });
        } catch (e) { /* ignore */ }
        // Reconnect with clean state to show QR
        instance.retryCount = 0;
        setTimeout(() => connectWhatsApp(instanceName), 3000);
      }
    }
  });

  return sock;
}

// --- Utility Functions ---

function calcularDelayTotal(texto) {
  const delayLectura = 1500 + Math.random() * 2000;
  const delayEscritura = Math.min(texto.length * 35, 6000);
  const random = Math.random() * 3000;
  const total = delayLectura + delayEscritura + random;
  return Math.max(3000, Math.min(total, 12000));
}

async function enviarMensajeHumano(sock, jid, text) {
  const delay = calcularDelayTotal(text);
  console.log(`[WhatsApp] Simulando humano: ${Math.round(delay)}ms total (${text.length} chars)`);

  await sock.presenceSubscribe(jid);
  await sock.sendPresenceUpdate('available', jid);
  await new Promise(resolve => setTimeout(resolve, 1500 + Math.random() * 1000));

  await sock.sendPresenceUpdate('composing', jid);
  await new Promise(resolve => setTimeout(resolve, delay - 2000));

  await sock.sendPresenceUpdate('paused', jid);
  const result = await sock.sendMessage(jid, { text });
  return result;
}

async function convertMp3ToOggOpus(mp3Buffer) {
  const tmpDir = os.tmpdir();
  const ts = Date.now();
  const inputPath = path.join(tmpDir, `wa_input_${ts}.mp3`);
  const outputPath = path.join(tmpDir, `wa_output_${ts}.ogg`);

  fs.writeFileSync(inputPath, mp3Buffer);

  return new Promise((resolve, reject) => {
    execFile(ffmpegPath, [
      '-i', inputPath,
      '-c:a', 'libopus',
      '-b:a', '128k',
      '-ac', '1',
      '-ar', '48000',
      '-application', 'voip',
      '-y',
      outputPath
    ], { timeout: 30000 }, (error, stdout, stderr) => {
      try { fs.unlinkSync(inputPath); } catch(e) {}

      if (error) {
        console.error('[ffmpeg] Error convirtiendo:', error.message);
        try { fs.unlinkSync(outputPath); } catch(e) {}
        reject(error);
        return;
      }

      try {
        const oggBuffer = fs.readFileSync(outputPath);
        fs.unlinkSync(outputPath);

        if (oggBuffer.length === 0) {
          reject(new Error('FFmpeg produjo archivo vacio'));
          return;
        }

        let seconds = 0;
        const durationMatch = (stderr || '').match(/Duration: (\d+):(\d+):(\d+)\.(\d+)/);
        if (durationMatch) {
          seconds = parseInt(durationMatch[1]) * 3600 + parseInt(durationMatch[2]) * 60 + parseInt(durationMatch[3]);
        }
        if (seconds === 0) {
          seconds = Math.max(1, Math.ceil(oggBuffer.length / 16000));
        }

        console.log(`[ffmpeg] Convertido: MP3 (${mp3Buffer.length} bytes) -> OGG Opus (${oggBuffer.length} bytes, ~${seconds}s)`);
        resolve({ buffer: oggBuffer, seconds });
      } catch(e) {
        reject(e);
      }
    });
  });
}

// --- Middleware: extraer instancia y validar conexión ---
function requireInstance(req, res, next) {
  const instance = getInstance(req.params.instance);
  req.instance = instance;
  req.instanceName = req.params.instance;
  next();
}

function requireConnected(req, res, next) {
  if (req.instance.connectionStatus !== 'open') {
    return res.status(503).json({ error: 'WhatsApp no conectado', status: req.instance.connectionStatus });
  }
  next();
}

// --- API Endpoints (multi-instance: /api/:instance/...) ---

// Status
app.get('/api/:instance/status', requireInstance, (req, res) => {
  const { sock, connectionStatus } = req.instance;
  res.json({
    status: connectionStatus,
    user: sock?.user ? { id: sock.user.id, name: sock.user.name } : null,
  });
});

// QR Code
app.get('/api/:instance/qr', requireInstance, (req, res) => {
  const { connectionStatus, qrBase64 } = req.instance;
  if (connectionStatus === 'open') {
    return res.json({ status: 'open', qr: null, message: 'Ya conectado' });
  }
  if (!qrBase64) {
    return res.json({ status: connectionStatus, qr: null, message: 'QR aun no disponible, espera unos segundos...' });
  }
  res.json({ status: 'connecting', qr: qrBase64 });
});

// Force reconnect
app.post('/api/:instance/reconnect', requireInstance, async (req, res) => {
  try {
    const instance = req.instance;
    const cleanAuth = req.body?.cleanAuth === true;

    // Set flag so close handler doesn't auto-reconnect
    instance.manualReconnect = true;

    if (instance.sock) {
      try {
        instance.sock.ev.removeAllListeners('connection.update');
        instance.sock.ev.removeAllListeners('messages.upsert');
        instance.sock.ev.removeAllListeners('creds.update');
        instance.sock.end();
      } catch (e) {
        console.warn(`[${req.instanceName}] Error cerrando socket anterior: ${e.message}`);
      }
      instance.sock = null;
    }

    // If cleanAuth requested or reconnect from stale state, remove auth files to force fresh QR
    if (cleanAuth) {
      const authDir = path.join(AUTH_DIR, req.instanceName);
      try {
        fs.rmSync(authDir, { recursive: true, force: true });
        console.log(`[${req.instanceName}] Auth files limpiados para reconexion limpia.`);
      } catch (e) { /* ignore */ }
    }

    instance.retryCount = 0;
    instance.connectionStatus = 'connecting';
    instance.currentQR = null;
    instance.qrBase64 = null;
    instance.manualReconnect = false;
    // Small delay to let old socket fully clean up
    await new Promise(resolve => setTimeout(resolve, 1000));

    await connectWhatsApp(req.instanceName);
    res.json({ message: cleanAuth ? 'Reconexion limpia... espera el QR' : 'Reconectando... espera el QR' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Logout
app.post('/api/:instance/logout', requireInstance, async (req, res) => {
  try {
    const instance = req.instance;
    instance.manualReconnect = true; // prevent auto-reconnect on close

    if (instance.sock) {
      try {
        instance.sock.ev.removeAllListeners('connection.update');
        instance.sock.ev.removeAllListeners('messages.upsert');
        instance.sock.ev.removeAllListeners('creds.update');
        await instance.sock.logout();
      } catch (e) {
        console.warn(`[${req.instanceName}] Error en logout: ${e.message}`);
      }
      instance.sock = null;
    }

    instance.connectionStatus = 'disconnected';
    instance.currentQR = null;
    instance.qrBase64 = null;
    instance.manualReconnect = false;

    // Clean auth files
    const authDir = path.join(AUTH_DIR, req.instanceName);
    try {
      fs.rmSync(authDir, { recursive: true, force: true });
      console.log(`[${req.instanceName}] Auth files limpiados.`);
    } catch (e) { /* ignore */ }

    res.json({ message: 'Sesion cerrada.' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// List all groups
app.get('/api/:instance/groups', requireInstance, requireConnected, async (req, res) => {
  try {
    const allGroups = await req.instance.sock.groupFetchAllParticipating();
    const groups = Object.values(allGroups)
      .filter(g => g.subject && g.subject.trim() !== '')
      .map(g => ({
        id: g.id,
        subject: g.subject,
        size: g.size || (g.participants ? g.participants.length : 0),
        owner: g.owner || null,
        desc: g.desc || null,
      }));
    console.log(`[${req.instanceName}] Listados ${groups.length} grupos`);
    res.json(groups);
  } catch (err) {
    console.error(`[${req.instanceName}] Error listando grupos:`, err.message);
    res.status(500).json({ error: 'Error listando grupos: ' + err.message });
  }
});

// Get group participants
app.get('/api/:instance/groups/:jid/participants', requireInstance, requireConnected, async (req, res) => {
  try {
    const sock = req.instance.sock;
    let participants = [];

    try {
      const metadata = await sock.groupMetadata(req.params.jid);
      const rawParticipants = metadata.participants || [];
      participants = rawParticipants
        .filter(p => p.id || p.jid)
        .map(p => ({ id: p.jid || p.id, admin: p.admin || null }));
    } catch (metaErr) {
      console.warn(`[${req.instanceName}] groupMetadata fallo: ${metaErr.message}`);
    }

    if (participants.length === 0) {
      const allGroups = await sock.groupFetchAllParticipating();
      const group = allGroups[req.params.jid];
      if (group) {
        const rawP = group.participants || [];
        participants = rawP
          .filter(p => p.id || p.jid)
          .map(p => ({ id: p.jid || p.id, admin: p.admin || null }));
      }
    }

    res.json(participants);
  } catch (err) {
    console.error(`[${req.instanceName}] Error obteniendo participantes:`, err.message);
    res.status(500).json({ error: 'Error obteniendo participantes: ' + err.message });
  }
});

// Send text message
app.post('/api/:instance/send-message', requireInstance, requireConnected, async (req, res) => {
  const { jid, text } = req.body;
  if (!jid || !text) {
    return res.status(400).json({ error: 'Se requiere jid y text' });
  }

  try {
    const result = await enviarMensajeHumano(req.instance.sock, jid, text);
    console.log(`[${req.instanceName}] Mensaje enviado a ${jid}`);
    res.json({ success: true, messageId: result.key.id });
  } catch (err) {
    console.error(`[${req.instanceName}] Error enviando mensaje:`, err.message);
    res.status(500).json({ error: 'Error enviando mensaje: ' + err.message });
  }
});

// Send image message
app.post('/api/:instance/send-image', requireInstance, requireConnected, async (req, res) => {
  const { jid, imageUrl, caption } = req.body;
  if (!jid || !imageUrl) {
    return res.status(400).json({ error: 'Se requiere jid y imageUrl' });
  }

  try {
    const sock = req.instance.sock;
    await sock.presenceSubscribe(jid);
    await sock.sendPresenceUpdate('composing', jid);

    const response = await fetch(imageUrl);
    if (!response.ok) throw new Error(`Error descargando imagen: ${response.status}`);
    const buffer = Buffer.from(await response.arrayBuffer());

    await new Promise(resolve => setTimeout(resolve, 1000 + Math.random() * 1500));
    await sock.sendPresenceUpdate('paused', jid);

    const result = await sock.sendMessage(jid, { image: buffer, caption: caption || '' });
    console.log(`[${req.instanceName}] Imagen enviada a ${jid} (${buffer.length} bytes)`);
    res.json({ success: true, messageId: result.key.id });
  } catch (err) {
    console.error(`[${req.instanceName}] Error enviando imagen:`, err.message);
    res.status(500).json({ error: 'Error enviando imagen: ' + err.message });
  }
});

// Send audio/voice note
app.post('/api/:instance/send-audio', requireInstance, requireConnected, async (req, res) => {
  const { jid, audioBase64 } = req.body;
  if (!jid || !audioBase64) {
    return res.status(400).json({ error: 'Se requiere jid y audioBase64' });
  }

  try {
    const sock = req.instance.sock;
    await sock.presenceSubscribe(jid);
    await sock.sendPresenceUpdate('recording', jid);

    const mp3Buffer = Buffer.from(audioBase64, 'base64');
    console.log(`[${req.instanceName}] Audio recibido: ${mp3Buffer.length} bytes MP3, convirtiendo...`);

    const { buffer: oggBuffer, seconds } = await convertMp3ToOggOpus(mp3Buffer);

    await new Promise(resolve => setTimeout(resolve, 1500 + Math.random() * 2000));
    await sock.sendPresenceUpdate('paused', jid);

    const result = await sock.sendMessage(jid, {
      audio: oggBuffer,
      mimetype: 'audio/ogg; codecs=opus',
      ptt: true,
      seconds: seconds
    });

    console.log(`[${req.instanceName}] Nota de voz enviada a ${jid} (${oggBuffer.length} bytes, ${seconds}s)`);
    res.json({ success: true, messageId: result.key.id });
  } catch (err) {
    console.error(`[${req.instanceName}] Error enviando nota de voz:`, err.message);
    res.status(500).json({ error: 'Error enviando nota de voz: ' + err.message });
  }
});

// List all active instances
app.get('/api/instances', (req, res) => {
  const list = [];
  for (const [name, inst] of instances) {
    list.push({
      name,
      status: inst.connectionStatus,
      user: inst.sock?.user ? { id: inst.sock.user.id, name: inst.sock.user.name } : null,
    });
  }
  res.json(list);
});

// --- Global error handling ---
process.on('uncaughtException', (err) => {
  console.error('[WhatsApp] Error no capturado:', err.message);
});
process.on('unhandledRejection', (err) => {
  console.error('[WhatsApp] Promise rechazada:', err?.message || err);
});

// --- Start ---
app.listen(PORT, async () => {
  console.log(`[WhatsApp Service] Corriendo en http://localhost:${PORT}`);
  console.log('[WhatsApp Service] Multi-instancia habilitado');
  console.log('  Endpoints: /api/:instance/status, /qr, /reconnect, /logout, /groups, /send-message, /send-image, /send-audio');
  console.log('  GET /api/instances - Listar instancias activas');
  console.log('');

  // Auto-reconectar instancias que tienen auth guardado
  if (fs.existsSync(AUTH_DIR)) {
    const dirs = fs.readdirSync(AUTH_DIR).filter(f => {
      try { return fs.statSync(path.join(AUTH_DIR, f)).isDirectory(); } catch { return false; }
    });
    for (const dir of dirs) {
      console.log(`[WhatsApp] Auto-reconectando instancia: ${dir}`);
      try {
        await connectWhatsApp(dir);
      } catch (err) {
        console.error(`[WhatsApp] Error reconectando ${dir}:`, err.message);
      }
    }
    if (dirs.length === 0) {
      console.log('[WhatsApp] No hay instancias previas. Usa POST /api/{nombre}/reconnect para crear una.');
    }
  }
});
