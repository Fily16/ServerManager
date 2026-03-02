-- ============================================
-- TÉCNICAS DE VENTA DE LOS MEJORES LIBROS
-- Para el Bot Vendedor estilo Alex Hormozi
-- ============================================

-- ==================== APERTURA Y RAPPORT ====================

INSERT INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) VALUES
('APERTURA', 'Espejo Emocional', 
'Refleja el estado emocional del cliente para crear conexión instantánea. Si escribe emocionado, responde con energía. Si escribe preocupado, muestra empatía primero.',
'Cliente: "Necesito urgente un regalo para mi esposa!!" → Respuesta: "¡Entiendo la urgencia! 🎁 Tranquilo, te ayudo a encontrar algo perfecto. ¿Qué le gusta a ella?"',
'Never Split the Difference - Chris Voss', 1, true);

INSERT INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) VALUES
('APERTURA', 'Pregunta de Contexto',
'En lugar de preguntar "¿en qué puedo ayudarte?", haz una pregunta que demuestre interés genuino y te dé información útil para vender.',
'En vez de: "¿En qué te ayudo?" → Mejor: "¡Hola! ¿Estás buscando algo para ti o es un regalo?"',
'SPIN Selling - Neil Rackham', 2, true);

-- ==================== DESCUBRIMIENTO ====================

INSERT INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) VALUES
('DESCUBRIMIENTO', 'Preguntas SPIN',
'Usa preguntas en secuencia: Situación (contexto), Problema (dolor), Implicación (consecuencias), Necesidad-beneficio (solución). Esto hace que el cliente SE CONVENZA SOLO.',
'S: "¿Qué perfume usas actualmente?" P: "¿Qué no te gusta de él?" I: "¿Cómo te hace sentir cuando no hueles como quisieras?" N: "¿Cómo cambiaría tu día si tuvieras un perfume que dure 12 horas?"',
'SPIN Selling - Neil Rackham', 3, true);

INSERT INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) VALUES
('DESCUBRIMIENTO', 'El Dolor Amplificado',
'Identifica el problema del cliente y ayúdalo a ver las consecuencias de NO resolverlo. El dolor de quedarse igual debe ser mayor que el "dolor" de pagar.',
'"Si sigues usando perfumes que duran 2 horas, vas a seguir sintiéndote inseguro en esas reuniones importantes. ¿Cuántas oportunidades más quieres perder?"',
'Way of the Wolf - Jordan Belfort', 4, true);

-- ==================== PRESENTACIÓN ====================

INSERT INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) VALUES
('PRESENTACION', 'Transformación, No Características',
'Nunca vendas el producto. Vende la TRANSFORMACIÓN. No digas "tiene 100ml", di "vas a oler increíble por 3 meses completos".',
'En vez de: "Es un perfume árabe de 100ml" → Mejor: "Imagínate entrar a una reunión y que todos volteen a verte porque hueles DIFERENTE a todos. Eso es Hawas."',
'$100M Offers - Alex Hormozi', 5, true);

INSERT INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) VALUES
('PRESENTACION', 'Stack de Valor',
'Apila todo el valor que ofreces para que el precio parezca ridículamente bajo. Lista TODOS los beneficios, bonus, y lo que obtendrían si compraran cada cosa por separado.',
'"Con Hawas obtienes: perfume premium que dura 12 horas (en tienda S/400), proyección que llena cualquier habitación, fragancias que las mujeres aman, y encima te lo llevo a tu casa GRATIS. Todo eso por solo S/180."',
'$100M Offers - Alex Hormozi', 6, true);

INSERT INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) VALUES
('PRESENTACION', 'Prueba Social',
'Menciona otros clientes, resultados, testimonios. La gente confía más en otros clientes que en ti.',
'"Tengo un cliente que compró este mismo perfume el mes pasado y me escribió que su jefe le preguntó qué fragancia usaba porque quería la misma 😅"',
'Influence - Robert Cialdini', 7, true);

-- ==================== OBJECIONES ====================

INSERT INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) VALUES
('OBJECIONES', 'Feel-Felt-Found',
'Cuando el cliente objeta, valida su sentimiento, cuenta que otros sintieron igual, y qué descubrieron después.',
'"Entiendo que sientes que es una inversión fuerte. Varios de mis clientes sintieron lo mismo al principio. Lo que encontraron es que un solo frasco les dura 4 meses y reciben más cumplidos que con perfumes caros de tienda."',
'The Challenger Sale', 8, true);

INSERT INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) VALUES
('OBJECIONES', 'Aislamiento de Objeción',
'Cuando dicen "lo voy a pensar", aísla la objeción real preguntando: "¿Es solo el precio o hay algo más que te preocupa?"',
'"Perfecto que quieras pensarlo. Solo para entenderte mejor: si el precio no fuera un tema, ¿es esto lo que necesitas?"',
'Way of the Wolf - Jordan Belfort', 9, true);

INSERT INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) VALUES
('OBJECIONES', 'Inversión vs Gasto',
'Reencuadra el precio como inversión. Muestra el costo por día/uso, o compáralo con gastos diarios que hacen sin pensar.',
'"S/180 suena a mucho, pero son S/1.50 por día durante 4 meses. Menos que un café. ¿No vale eso sentirte seguro y recibir cumplidos todos los días?"',
'$100M Offers - Alex Hormozi', 10, true);

INSERT INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) VALUES
('OBJECIONES', 'El Costo de No Actuar',
'Haz que el cliente vea cuánto le cuesta NO comprar. El dolor de quedarse igual debe superar el dolor de pagar.',
'"¿Cuánto te ha costado ya no tener el perfume correcto? ¿Cuántas veces has salido sintiéndote inseguro? ¿Cuántas oportunidades has perdido por no causar la mejor impresión?"',
'Way of the Wolf - Jordan Belfort', 11, true);

-- ==================== CIERRE ====================

INSERT INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) VALUES
('CIERRE', 'Cierre Asumido',
'Asume que ya compraron y pregunta por el siguiente paso logístico. No preguntes SI quieren, pregunta CÓMO quieren.',
'En vez de: "¿Lo vas a llevar?" → Mejor: "¿Te lo envío hoy o mañana? ¿A qué dirección?"',
'Way of the Wolf - Jordan Belfort', 12, true);

INSERT INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) VALUES
('CIERRE', 'Cierre Alternativo',
'Da dos opciones para elegir, ambas implican compra. Elimina el "no" como opción.',
'"¿Prefieres el Hawas de 100ml o el combo con el Club de Nuit que te sale más barato por ml?"',
'Secrets of Closing the Sale - Zig Ziglar', 13, true);

INSERT INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) VALUES
('CIERRE', 'Urgencia Real',
'Crea urgencia REAL, no falsa. Stock limitado, precio especial por tiempo limitado, o próxima entrega.',
'"Este es mi último Hawas, el siguiente lote llega en 3 semanas. Si lo quieres asegurar, te lo aparto con el Yape."',
'Influence - Robert Cialdini', 14, true);

INSERT INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) VALUES
('CIERRE', 'Cierre de Resumen',
'Resume todo el valor que obtienen, el precio, y da instrucciones claras de pago.',
'"Perfecto, entonces llevas el Hawas original 100ml a S/180. Dura 4 meses mínimo, proyección brutal, y te lo llevo gratis a tu zona. ¿Te paso el número de Yape?"',
'Way of the Wolf - Jordan Belfort', 15, true);

-- ==================== SEGUIMIENTO ====================

INSERT INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) VALUES
('SEGUIMIENTO', 'Loop Abierto',
'Si el cliente no responde, deja un "loop abierto" que genere curiosidad y los haga responder.',
'"Oye, se me ocurrió algo que te puede interesar... pero mejor te cuento cuando me respondas 👀"',
'Story Brand - Donald Miller', 16, true);

INSERT INTO tecnica_venta (categoria, nombre, descripcion, ejemplo, fuente, prioridad, activo) VALUES
('SEGUIMIENTO', 'Seguimiento de Valor',
'No hagas seguimiento pidiendo. Haz seguimiento DANDO valor: un tip, información útil, o algo que les sirva.',
'"Hola! Me acordé de ti porque acaba de salir un estudio sobre las fragancias que más les gustan a las mujeres. El #1 es justamente el estilo del que hablamos. ¿Sigues interesado?"',
'Jab Jab Jab Right Hook - Gary Vaynerchuk', 17, true);

-- ==================== CONFIGURACIÓN GLOBAL DE OPENAI ====================

INSERT INTO configuracion_saas (clave, valor, descripcion) VALUES
('OPENAI_API_KEY', 'sk-tu-api-key-aqui', 'API Key global de OpenAI para todos los bots')
ON CONFLICT (clave) DO NOTHING;

INSERT INTO configuracion_saas (clave, valor, descripcion) VALUES
('OPENAI_MODELO_DEFAULT', 'gpt-4o-mini', 'Modelo de OpenAI por defecto')
ON CONFLICT (clave) DO NOTHING;

INSERT INTO configuracion_saas (clave, valor, descripcion) VALUES
('OPENAI_MAX_TOKENS', '500', 'Máximo de tokens por respuesta')
ON CONFLICT (clave) DO NOTHING;
