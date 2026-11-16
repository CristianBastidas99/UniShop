-- =====================================================================
-- Datos de prueba de UniShop (Paso 1.5 del plan de ejecucion)
-- Se carga con @Sql("classpath:unishop.sql") en las pruebas de persistencia y de negocio
-- (negocio lo toma de esta misma carpeta, ver negocio/pom.xml).
--
-- Reglas:
-- - Las tablas van en minuscula, como las crea Hibernate (MySQL en Linux distingue mayusculas).
-- - Sin tildes: @Sql lee el archivo con la codificacion por defecto del sistema.
-- - Usuario y Administrador heredan de Persona (TABLE_PER_CLASS): sus ids no se pueden repetir
--   entre las dos tablas. Administradores: 1 a 4. Usuarios: 5 a 9.
-- - Las fechas limite vigentes son de 2030. El producto 6 y la subasta 3 estan vencidos a proposito.
-- - Ojo: los ids son fijos y la secuencia de Hibernate (hibernate_sequence) empieza en 1 en cada
--   ejecucion, asi que una prueba que cargue este archivo y luego guarde entidades nuevas puede
--   chocar con estos ids si es de las primeras en ejecutarse.
-- =====================================================================

-- ===== Administradores (ids 1 a 4) =====
INSERT INTO administrador (id, nombre, email, password) VALUES (1, 'Cristian Bastidas', 'cfbastidaso@uqvirtual.edu.co', '12345');
INSERT INTO administrador (id, nombre, email, password) VALUES (2, 'Juanito Grandez', 'juanito@uniquindio.com', '12345');
INSERT INTO administrador (id, nombre, email, password) VALUES (3, 'La Roca', 'laroca@uniquindio.com', '12345');
INSERT INTO administrador (id, nombre, email, password) VALUES (4, 'Profesor', 'profesor@uniquindio.com', '12345');

-- ===== Ciudades =====
INSERT INTO ciudad (id, nombre) VALUES (1, 'Armenia');
INSERT INTO ciudad (id, nombre) VALUES (2, 'Medellin');
INSERT INTO ciudad (id, nombre) VALUES (3, 'Bogota');
INSERT INTO ciudad (id, nombre) VALUES (4, 'Cartagena');

-- ===== Usuarios (ids 5 a 9) =====
INSERT INTO usuario (id, nombre, email, password, id_ciudad) VALUES (5, 'Lorena Lopez', 'lorena@email.com', '12345', 1);
INSERT INTO usuario (id, nombre, email, password, id_ciudad) VALUES (6, 'Marta Wayne', 'marta@email.com', '12345', 4);
INSERT INTO usuario (id, nombre, email, password, id_ciudad) VALUES (7, 'Pepe Perez', 'pepe@email.com', '12345', 2);
INSERT INTO usuario (id, nombre, email, password, id_ciudad) VALUES (8, 'Pedro Martinez', 'pedro@email.com', '12345', 3);
INSERT INTO usuario (id, nombre, email, password, id_ciudad) VALUES (9, 'Sofia Ramirez', 'sofia@email.com', '12345', 1);

INSERT INTO telefono (id_usuario, num_telefono) VALUES (5, '3101234567');
INSERT INTO telefono (id_usuario, num_telefono) VALUES (5, '3157654321');
INSERT INTO telefono (id_usuario, num_telefono) VALUES (6, '3009876543');
INSERT INTO telefono (id_usuario, num_telefono) VALUES (7, '3204567890');
INSERT INTO telefono (id_usuario, num_telefono) VALUES (8, '3112223344');

-- ===== Categorias =====
INSERT INTO categoria (id, nombre) VALUES (1, 'Aseo');
INSERT INTO categoria (id, nombre) VALUES (2, 'Cocina');
INSERT INTO categoria (id, nombre) VALUES (3, 'Tecnologia');
INSERT INTO categoria (id, nombre) VALUES (4, 'Mascotas');
INSERT INTO categoria (id, nombre) VALUES (5, 'Hogar');

-- ===== Productos (vendedor = id_usuario) =====
INSERT INTO producto (id, nombre, unidades, descripcion, precio, fecha_limite, descuento, id_usuario, id_ciudad)
VALUES (1, 'Cepillo Electrico', 20, 'Cepillo dental recargable con 3 modos de limpieza', 30000, '2030-07-04 23:59:59', 0.3, 5, 1);
INSERT INTO producto (id, nombre, unidades, descripcion, precio, fecha_limite, descuento, id_usuario, id_ciudad)
VALUES (2, 'SmartPhone', 3, 'Celular de 128 GB con camara de 48 MP', 300000, '2030-01-04 23:59:59', 0.1, 6, 4);
INSERT INTO producto (id, nombre, unidades, descripcion, precio, fecha_limite, descuento, id_usuario, id_ciudad)
VALUES (3, 'Juguete Perro', 50, 'Hueso de caucho resistente para perros medianos', 3000, '2030-05-04 23:59:59', 0, 7, 2);
INSERT INTO producto (id, nombre, unidades, descripcion, precio, fecha_limite, descuento, id_usuario, id_ciudad)
VALUES (4, 'Licuadora', 8, 'Licuadora de 10 velocidades con vaso de vidrio', 150000, '2030-03-15 23:59:59', 0, 5, 1);
INSERT INTO producto (id, nombre, unidades, descripcion, precio, fecha_limite, descuento, id_usuario, id_ciudad)
VALUES (5, 'Portatil', 2, 'Portatil de 14 pulgadas, 16 GB de RAM', 2500000, '2030-09-01 23:59:59', 0.05, 8, 3);
INSERT INTO producto (id, nombre, unidades, descripcion, precio, fecha_limite, descuento, id_usuario, id_ciudad)
VALUES (6, 'Lampara de Mesa', 4, 'Lampara LED de luz calida (publicacion vencida)', 80000, '2024-01-01 00:00:00', 0, 6, 4);

INSERT INTO imagen (id_producto, ruta) VALUES (1, 'cepillo.png');
INSERT INTO imagen (id_producto, ruta) VALUES (2, 'smartphone.png');
INSERT INTO imagen (id_producto, ruta) VALUES (2, 'smartphone-2.png');
INSERT INTO imagen (id_producto, ruta) VALUES (3, 'juguete-perro.png');
INSERT INTO imagen (id_producto, ruta) VALUES (4, 'licuadora.png');
INSERT INTO imagen (id_producto, ruta) VALUES (5, 'portatil.png');
INSERT INTO imagen (id_producto, ruta) VALUES (6, 'lampara.png');

INSERT INTO producto_categoria (id_categoria, id_producto) VALUES (1, 1);
INSERT INTO producto_categoria (id_categoria, id_producto) VALUES (3, 1);
INSERT INTO producto_categoria (id_categoria, id_producto) VALUES (3, 2);
INSERT INTO producto_categoria (id_categoria, id_producto) VALUES (4, 3);
INSERT INTO producto_categoria (id_categoria, id_producto) VALUES (2, 4);
INSERT INTO producto_categoria (id_categoria, id_producto) VALUES (5, 4);
INSERT INTO producto_categoria (id_categoria, id_producto) VALUES (3, 5);
INSERT INTO producto_categoria (id_categoria, id_producto) VALUES (5, 6);

-- ===== Favoritos =====
INSERT INTO favorito (id_usuario, id_producto) VALUES (5, 2);
INSERT INTO favorito (id_usuario, id_producto) VALUES (5, 5);
INSERT INTO favorito (id_usuario, id_producto) VALUES (6, 1);
INSERT INTO favorito (id_usuario, id_producto) VALUES (7, 2);

-- ===== Comentarios (calificacion de 0 a 5) =====
INSERT INTO comentario (id, mensaje, respuesta, fecha, calificacion, id_usuario, id_producto)
VALUES (1, 'Funciona muy bien, la bateria dura bastante', NULL, '2026-02-01 10:00:00', 5, 6, 1);
INSERT INTO comentario (id, mensaje, respuesta, fecha, calificacion, id_usuario, id_producto)
VALUES (2, 'Buen producto, pero llego tarde', 'Disculpa la demora, ya mejoramos los envios', '2026-02-05 15:30:00', 4, 7, 1);
INSERT INTO comentario (id, mensaje, respuesta, fecha, calificacion, id_usuario, id_producto)
VALUES (3, 'La camara no es tan buena como dice', NULL, '2026-02-12 08:45:00', 3, 5, 2);
INSERT INTO comentario (id, mensaje, respuesta, fecha, calificacion, id_usuario, id_producto)
VALUES (4, 'A mi perro le encanto', NULL, '2026-03-01 19:20:00', 5, 9, 3);
INSERT INTO comentario (id, mensaje, respuesta, fecha, calificacion, id_usuario, id_producto)
VALUES (5, 'Rapido y liviano', NULL, '2026-03-10 11:00:00', 4, 5, 5);

-- ===== Cupones de descuento (necesidad extra) =====
INSERT INTO cupon (id, codigo, porcentaje, fecha_vencimiento, usos_maximos, usos_realizados)
VALUES (1, 'BIENVENIDA10', 0.1, '2030-12-31 23:59:59', 100, 2);
INSERT INTO cupon (id, codigo, porcentaje, fecha_vencimiento, usos_maximos, usos_realizados)
VALUES (2, 'NAVIDAD20', 0.2, '2030-12-31 23:59:59', 50, 0);
INSERT INTO cupon (id, codigo, porcentaje, fecha_vencimiento, usos_maximos, usos_realizados)
VALUES (3, 'VERANO15', 0.15, '2024-06-30 23:59:59', 20, 20);

-- ===== Compras y sus detalles (precio_producto = precio con descuento al momento de la compra) =====
INSERT INTO compra (id, fecha_compra, medio_pago, id_usuario, id_cupon) VALUES (1, '2026-01-15 12:00:00', 'TARJETA', 6, 1);
INSERT INTO compra (id, fecha_compra, medio_pago, id_usuario, id_cupon) VALUES (2, '2026-02-10 09:30:00', 'PSE', 7, NULL);
INSERT INTO compra (id, fecha_compra, medio_pago, id_usuario, id_cupon) VALUES (3, '2026-03-05 18:15:00', 'PAYPAL', 5, NULL);
INSERT INTO compra (id, fecha_compra, medio_pago, id_usuario, id_cupon) VALUES (4, '2026-03-20 14:00:00', 'BALOTO', 9, 1);

INSERT INTO detalle_compra (id, unidades, precio_producto, id_compra, id_producto) VALUES (1, 2, 21000, 1, 1);
INSERT INTO detalle_compra (id, unidades, precio_producto, id_compra, id_producto) VALUES (2, 1, 3000, 1, 3);
INSERT INTO detalle_compra (id, unidades, precio_producto, id_compra, id_producto) VALUES (3, 1, 270000, 2, 2);
INSERT INTO detalle_compra (id, unidades, precio_producto, id_compra, id_producto) VALUES (4, 1, 2375000, 3, 5);
INSERT INTO detalle_compra (id, unidades, precio_producto, id_compra, id_producto) VALUES (5, 1, 150000, 4, 4);

-- ===== Chats (id_usuario = comprador) y mensajes =====
INSERT INTO chat (id, id_usuario, id_producto) VALUES (1, 5, 3);
INSERT INTO chat (id, id_usuario, id_producto) VALUES (2, 7, 2);
INSERT INTO chat (id, id_usuario, id_producto) VALUES (3, 6, 1);
INSERT INTO chat (id, id_usuario, id_producto) VALUES (4, 9, 5);

INSERT INTO mensaje (id, mensaje, fecha, id_chat, id_remitente) VALUES (1, 'Hola, esta disponible?', '2026-03-01 09:00:00', 1, 5);
INSERT INTO mensaje (id, mensaje, fecha, id_chat, id_remitente) VALUES (2, 'Si, quedan 50 unidades', '2026-03-01 09:05:00', 1, 7);
INSERT INTO mensaje (id, mensaje, fecha, id_chat, id_remitente) VALUES (3, 'Cual es el precio final?', '2026-02-08 16:40:00', 2, 7);
INSERT INTO mensaje (id, mensaje, fecha, id_chat, id_remitente) VALUES (4, 'Hace envios a Cartagena?', '2026-01-10 11:10:00', 3, 6);

-- ===== Subastas y pujas (el vendedor no puja en sus propios productos) =====
INSERT INTO subasta (id, fecha_limite, id_producto, cerrada, id_compra) VALUES (1, '2030-06-30 18:00:00', 5, 0, NULL);
INSERT INTO subasta (id, fecha_limite, id_producto, cerrada, id_compra) VALUES (2, '2030-02-28 18:00:00', 2, 0, NULL);
INSERT INTO subasta (id, fecha_limite, id_producto, cerrada, id_compra) VALUES (3, '2024-01-01 00:00:00', 6, 0, NULL);

INSERT INTO subasta_usuario (id, valor, fecha_subasta, id_subasta, id_usuario) VALUES (1, 2000000, '2026-04-01 10:00:00', 1, 5);
INSERT INTO subasta_usuario (id, valor, fecha_subasta, id_subasta, id_usuario) VALUES (2, 2100000, '2026-04-02 12:30:00', 1, 6);
INSERT INTO subasta_usuario (id, valor, fecha_subasta, id_subasta, id_usuario) VALUES (3, 250000, '2026-04-03 08:00:00', 2, 7);
INSERT INTO subasta_usuario (id, valor, fecha_subasta, id_subasta, id_usuario) VALUES (4, 260000, '2026-04-03 09:15:00', 2, 9);

-- ===== Reportes de productos (necesidad extra) =====
INSERT INTO reporte_producto (id, motivo, descripcion, fecha, estado, respuesta_administrador, id_usuario, id_producto, id_administrador)
VALUES (1, 'FRAUDE', 'El vendedor pide el pago por fuera de la plataforma', '2026-03-12 20:00:00', 'PENDIENTE', NULL, 9, 3, NULL);
INSERT INTO reporte_producto (id, motivo, descripcion, fecha, estado, respuesta_administrador, id_usuario, id_producto, id_administrador)
VALUES (2, 'INFORMACION_FALSA', 'La descripcion dice 16 GB pero las fotos muestran 8 GB', '2026-03-15 13:00:00', 'EN_REVISION', NULL, 7, 5, 1);
INSERT INTO reporte_producto (id, motivo, descripcion, fecha, estado, respuesta_administrador, id_usuario, id_producto, id_administrador)
VALUES (3, 'OTRO', 'El precio es demasiado bajo', '2026-02-20 10:30:00', 'RECHAZADO', 'El precio es correcto, el vendedor tiene descuento', 5, 2, 2);

-- ===== Historial de precios (necesidad extra) =====
INSERT INTO historial_precio (id, precio_anterior, precio_nuevo, descuento_anterior, descuento_nuevo, fecha, id_producto)
VALUES (1, 35000, 30000, 0.2, 0.3, '2026-01-02 08:00:00', 1);
INSERT INTO historial_precio (id, precio_anterior, precio_nuevo, descuento_anterior, descuento_nuevo, fecha, id_producto)
VALUES (2, 320000, 300000, 0.1, 0.1, '2026-01-20 08:00:00', 2);
INSERT INTO historial_precio (id, precio_anterior, precio_nuevo, descuento_anterior, descuento_nuevo, fecha, id_producto)
VALUES (3, 2600000, 2500000, 0, 0.05, '2026-02-15 08:00:00', 5);
