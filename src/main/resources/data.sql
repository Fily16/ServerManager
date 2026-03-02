-- ============================================
-- DATOS INICIALES - ServerManager
-- Se ejecuta automaticamente en perfil dev
-- Usa MERGE INTO para no duplicar datos entre reinicios
-- ============================================

-- ==================== TECNICAS DE VENTA ====================
-- Basadas en los mejores libros de ventas

-- APERTURA Y RAPPORT
MERGE INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) KEY(nombre) VALUES
('APERTURA', 'Espejo Emocional',
'Refleja el estado emocional del cliente para crear conexion instantanea. Si escribe emocionado, responde con energia. Si escribe preocupado, muestra empatia primero.',
'Cliente: "Necesito urgente un regalo para mi esposa!!" - Respuesta: "Entiendo la urgencia! Te ayudo a encontrar algo perfecto. Que le gusta a ella?"',
'Never Split the Difference - Chris Voss', 1, true);

MERGE INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) KEY(nombre) VALUES
('APERTURA', 'Pregunta de Contexto',
'En lugar de preguntar "en que puedo ayudarte?", haz una pregunta que demuestre interes genuino y te de informacion util para vender.',
'En vez de: "En que te ayudo?" - Mejor: "Hola! Estas buscando algo para ti o es un regalo?"',
'SPIN Selling - Neil Rackham', 2, true);

-- DESCUBRIMIENTO
MERGE INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) KEY(nombre) VALUES
('DESCUBRIMIENTO', 'Preguntas SPIN',
'Usa preguntas en secuencia: Situacion (contexto), Problema (dolor), Implicacion (consecuencias), Necesidad-beneficio (solucion). Esto hace que el cliente SE CONVENZA SOLO.',
'S: "Que perfume usas actualmente?" P: "Que no te gusta de el?" I: "Como te hace sentir cuando no hueles como quisieras?" N: "Como cambiaria tu dia si tuvieras un perfume que dure 12 horas?"',
'SPIN Selling - Neil Rackham', 3, true);

MERGE INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) KEY(nombre) VALUES
('DESCUBRIMIENTO', 'El Dolor Amplificado',
'Identifica el problema del cliente y ayudalo a ver las consecuencias de NO resolverlo. El dolor de quedarse igual debe ser mayor que el "dolor" de pagar.',
'"Si sigues usando perfumes que duran 2 horas, vas a seguir sintiendote inseguro en esas reuniones importantes. Cuantas oportunidades mas quieres perder?"',
'Way of the Wolf - Jordan Belfort', 4, true);

-- PRESENTACION
MERGE INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) KEY(nombre) VALUES
('PRESENTACION', 'Transformacion, No Caracteristicas',
'Nunca vendas el producto. Vende la TRANSFORMACION. No digas "tiene 100ml", di "vas a oler increible por 3 meses completos".',
'En vez de: "Es un perfume arabe de 100ml" - Mejor: "Imaginate entrar a una reunion y que todos volteen a verte porque hueles DIFERENTE a todos. Eso es Hawas."',
'$100M Offers - Alex Hormozi', 5, true);

MERGE INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) KEY(nombre) VALUES
('PRESENTACION', 'Stack de Valor',
'Apila todo el valor que ofreces para que el precio parezca ridiculamente bajo. Lista TODOS los beneficios, bonus, y lo que obtendrian si compraran cada cosa por separado.',
'"Con Hawas obtienes: perfume premium que dura 12 horas (en tienda S/400), proyeccion que llena cualquier habitacion, fragancias que las mujeres aman, y encima te lo llevo a tu casa GRATIS. Todo eso por solo S/180."',
'$100M Offers - Alex Hormozi', 6, true);

MERGE INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) KEY(nombre) VALUES
('PRESENTACION', 'Prueba Social',
'Menciona otros clientes, resultados, testimonios. La gente confia mas en otros clientes que en ti.',
'"Tengo un cliente que compro este mismo perfume el mes pasado y me escribio que su jefe le pregunto que fragancia usaba porque queria la misma"',
'Influence - Robert Cialdini', 7, true);

-- OBJECIONES
MERGE INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) KEY(nombre) VALUES
('OBJECIONES', 'Feel-Felt-Found',
'Cuando el cliente objeta, valida su sentimiento, cuenta que otros sintieron igual, y que descubrieron despues.',
'"Entiendo que sientes que es una inversion fuerte. Varios de mis clientes sintieron lo mismo al principio. Lo que encontraron es que un solo frasco les dura 4 meses y reciben mas cumplidos que con perfumes caros de tienda."',
'The Challenger Sale', 8, true);

MERGE INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) KEY(nombre) VALUES
('OBJECIONES', 'Aislamiento de Objecion',
'Cuando dicen "lo voy a pensar", aisla la objecion real preguntando: "Es solo el precio o hay algo mas que te preocupa?"',
'"Perfecto que quieras pensarlo. Solo para entenderte mejor: si el precio no fuera un tema, es esto lo que necesitas?"',
'Way of the Wolf - Jordan Belfort', 9, true);

MERGE INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) KEY(nombre) VALUES
('OBJECIONES', 'Inversion vs Gasto',
'Reencuadra el precio como inversion. Muestra el costo por dia/uso, o comparalo con gastos diarios que hacen sin pensar.',
'"S/180 suena a mucho, pero son S/1.50 por dia durante 4 meses. Menos que un cafe. No vale eso sentirte seguro y recibir cumplidos todos los dias?"',
'$100M Offers - Alex Hormozi', 10, true);

MERGE INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) KEY(nombre) VALUES
('OBJECIONES', 'El Costo de No Actuar',
'Haz que el cliente vea cuanto le cuesta NO comprar. El dolor de quedarse igual debe superar el dolor de pagar.',
'"Cuanto te ha costado ya no tener el perfume correcto? Cuantas veces has salido sintiendote inseguro? Cuantas oportunidades has perdido por no causar la mejor impresion?"',
'Way of the Wolf - Jordan Belfort', 11, true);

-- CIERRE
MERGE INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) KEY(nombre) VALUES
('CIERRE', 'Cierre Asumido',
'Asume que ya compraron y pregunta por el siguiente paso logistico. No preguntes SI quieren, pregunta COMO quieren.',
'En vez de: "Lo vas a llevar?" - Mejor: "Te lo envio hoy o manana? A que direccion?"',
'Way of the Wolf - Jordan Belfort', 12, true);

MERGE INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) KEY(nombre) VALUES
('CIERRE', 'Cierre Alternativo',
'Da dos opciones para elegir, ambas implican compra. Elimina el "no" como opcion.',
'"Prefieres el Hawas de 100ml o el combo con el Club de Nuit que te sale mas barato por ml?"',
'Secrets of Closing the Sale - Zig Ziglar', 13, true);

MERGE INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) KEY(nombre) VALUES
('CIERRE', 'Urgencia Real',
'Crea urgencia REAL, no falsa. Stock limitado, precio especial por tiempo limitado, o proxima entrega.',
'"Este es mi ultimo Hawas, el siguiente lote llega en 3 semanas. Si lo quieres asegurar, te lo aparto con el Yape."',
'Influence - Robert Cialdini', 14, true);

MERGE INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) KEY(nombre) VALUES
('CIERRE', 'Cierre de Resumen',
'Resume todo el valor que obtienen, el precio, y da instrucciones claras de pago.',
'"Perfecto, entonces llevas el Hawas original 100ml a S/180. Dura 4 meses minimo, proyeccion brutal, y te lo llevo gratis a tu zona. Te paso el numero de Yape?"',
'Way of the Wolf - Jordan Belfort', 15, true);

-- SEGUIMIENTO
MERGE INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) KEY(nombre) VALUES
('SEGUIMIENTO', 'Loop Abierto',
'Si el cliente no responde, deja un "loop abierto" que genere curiosidad y los haga responder.',
'"Oye, se me ocurrio algo que te puede interesar... pero mejor te cuento cuando me respondas"',
'Story Brand - Donald Miller', 16, true);

MERGE INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) KEY(nombre) VALUES
('SEGUIMIENTO', 'Seguimiento de Valor',
'No hagas seguimiento pidiendo. Haz seguimiento DANDO valor: un tip, informacion util, o algo que les sirva.',
'"Hola! Me acorde de ti porque acaba de salir un estudio sobre las fragancias que mas les gustan a las mujeres. El #1 es justamente el estilo del que hablamos. Sigues interesado?"',
'Jab Jab Jab Right Hook - Gary Vaynerchuk', 17, true);

-- ==================== CONFIGURACION SAAS (OpenAI) ====================
-- Usar INSERT condicional para NO sobreescribir valores que el usuario haya cambiado desde el dashboard

INSERT INTO configuracion_saas (clave, valor, descripcion)
SELECT 'OPENAI_API_KEY', 'CONFIGURAR-DESDE-DASHBOARD', 'API Key global de OpenAI para todos los bots'
WHERE NOT EXISTS (SELECT 1 FROM configuracion_saas WHERE clave = 'OPENAI_API_KEY');

INSERT INTO configuracion_saas (clave, valor, descripcion)
SELECT 'OPENAI_MODELO_DEFAULT', 'gpt-4o-mini', 'Modelo de OpenAI por defecto'
WHERE NOT EXISTS (SELECT 1 FROM configuracion_saas WHERE clave = 'OPENAI_MODELO_DEFAULT');

INSERT INTO configuracion_saas (clave, valor, descripcion)
SELECT 'OPENAI_MAX_TOKENS', '500', 'Maximo de tokens por respuesta'
WHERE NOT EXISTS (SELECT 1 FROM configuracion_saas WHERE clave = 'OPENAI_MAX_TOKENS');

-- ==================== ELEVENLABS (Voz) ====================

INSERT INTO configuracion_saas (clave, valor, descripcion)
SELECT 'ELEVENLABS_API_KEY', 'CONFIGURAR-DESDE-DASHBOARD', 'API Key de ElevenLabs para text-to-speech'
WHERE NOT EXISTS (SELECT 1 FROM configuracion_saas WHERE clave = 'ELEVENLABS_API_KEY');

INSERT INTO configuracion_saas (clave, valor, descripcion)
SELECT 'ELEVENLABS_VOICE_ID', 'ECOET12tGKHdXyB0CfqU', 'ID de la voz de ElevenLabs'
WHERE NOT EXISTS (SELECT 1 FROM configuracion_saas WHERE clave = 'ELEVENLABS_VOICE_ID');

UPDATE configuracion_saas SET valor = 'ECOET12tGKHdXyB0CfqU' WHERE clave = 'ELEVENLABS_VOICE_ID' AND valor != 'ECOET12tGKHdXyB0CfqU';


-- ==================== CREACION DE EMPRESA Y BOT (AROMASTUDIO) ====================
-- INSERT condicional: solo crea si no existe, para preservar cambios del dashboard

INSERT INTO empresa (id, activo, fecha_creacion, ruc, nombre, plan)
SELECT 1, true, CURRENT_TIMESTAMP, '10123456789', 'AromaStudio', 'PRO'
WHERE NOT EXISTS (SELECT 1 FROM empresa WHERE id = 1);

INSERT INTO configuracion_bot (id, activo, auto_respuesta, empresa_id, evolution_api_url, evolution_api_key, evolution_instancia, nombre_bot, tono_conversacion, tiempo_entrega, link_tiktok, fecha_creacion)
SELECT 1, true, false, 1, 'http://localhost:3001/api', '', 'AromaStudio', 'Bot Perfumero', 'AMIGABLE', '1 a 2 semanas', 'https://www.tiktok.com/@aromastudio', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM configuracion_bot WHERE id = 1);

-- ==================== SEGUNDA EMPRESA (para tu compañero) ====================

INSERT INTO empresa (id, activo, fecha_creacion, ruc, nombre, plan)
SELECT 2, true, CURRENT_TIMESTAMP, '20987654321', 'NegocioSocio', 'PRO'
WHERE NOT EXISTS (SELECT 1 FROM empresa WHERE id = 2);

INSERT INTO configuracion_bot (id, activo, auto_respuesta, empresa_id, evolution_api_url, evolution_api_key, evolution_instancia, nombre_bot, tono_conversacion, tiempo_entrega, fecha_creacion)
SELECT 2, true, false, 2, 'http://localhost:3001/api', '', 'NegocioSocio', 'Bot Socio', 'AMIGABLE', '3 a 5 dias', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM configuracion_bot WHERE id = 2);

-- ==================== USUARIOS ====================
-- Password: admin123 (BCrypt hash)

INSERT INTO usuario (id, email, password, nombre, empresa_id, activo, fecha_creacion)
SELECT 1, 'admin@aromastudio.com', '$2b$10$FM44dP3RzORti2EMDc8JaOKIqOlrMZAQtFhUtryYMC2nrYi4EC74q', 'Admin AromaStudio', 1, true, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE id = 1);

INSERT INTO usuario (id, email, password, nombre, empresa_id, activo, fecha_creacion)
SELECT 2, 'admin@negociosocio.com', '$2b$10$FM44dP3RzORti2EMDc8JaOKIqOlrMZAQtFhUtryYMC2nrYi4EC74q', 'Admin Socio', 2, true, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE id = 2);
