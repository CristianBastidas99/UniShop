INSERT INTO Administrador (id, nombre, email, password) VALUES (1, "Cristian Bastidas", "cfbastidaso@uqvirtual.edu.co", "12345");
INSERT INTO Administrador (id, nombre, email, password) VALUES (2, "Juanito Grandez", "juanito@uniquindio.com", "12345");
INSERT INTO Administrador (id, nombre, email, password) VALUES (3, "La Roca", "laroca@uniquindio.com", "12345");
INSERT INTO Administrador (id, nombre, email, password) VALUES (4, "Profesor", "profesor@uniquindio.com", "12345");

INSERT INTO Ciudad (id, nombre) VALUES (1, "armenia");
INSERT INTO Ciudad (id, nombre) VALUES (2, "medellin");
INSERT INTO Ciudad (id, nombre) VALUES (3, "bogota");
INSERT INTO Ciudad (id, nombre) VALUES (4, "cartagena");


INSERT INTO Usuario (id, nombre, email, password, id_ciudad) VALUES (1, "Lorena Lopez", "lorena@email.com", "12345", "1");
INSERT INTO Usuario (id, nombre, email, password, id_ciudad) VALUES (2, "Marta Wayne", "marta@email.com", "12345", "4");
INSERT INTO Usuario (id, nombre, email, password, id_ciudad) VALUES (3, "Pepe Perez", "pepe@email.com", "12345", "2");
INSERT INTO Usuario (id, nombre, email, password, id_ciudad) VALUES (4, "Pedro Martinez", "pedro@email.com", "12345", "3");

INSERT INTO Producto (id, nombre, unidades, descripcion, precio, fecha_limite, descuento, id_usuario, id_ciudad)
VALUES (1, "Cepillo Electrico", 20, "El mejor producto", 30000, "2022-7-04", 0.3, "1", "2");
INSERT INTO Producto (id, nombre, unidades, descripcion, precio, fecha_limite, descuento, id_usuario, id_ciudad)
VALUES (2, "SmartPhone", 3, "El mejor producto", 300000, "2022-1-04", 0.1, "2", "3");
INSERT INTO Producto (id, nombre, unidades, descripcion, precio, fecha_limite, descuento, id_usuario, id_ciudad)
VALUES (3, "Juguete Perro", 50, "El mejor producto", 3000, "2022-5-04", 0.2, "3", "2");


INSERT INTO Chat (id, id_usuario, id_producto) VALUES (1, "1", "3");
INSERT INTO Chat (id, id_usuario, id_producto) VALUES (2, "2", "2");
INSERT INTO Chat (id, id_usuario, id_producto) VALUES (3, "3", "1");

INSERT INTO Mensaje (id, mensaje, fecha, id_chat) VALUES (1, "Disponible?", "2021-11-22", "3");
INSERT INTO Mensaje (id, mensaje, fecha, id_chat) VALUES (2, "Precio?", "2021-12-22", "2");
INSERT INTO Mensaje (id, mensaje, fecha, id_chat) VALUES (3, "Cuantos tienes??", "2021-10-22", "1");

INSERT INTO Categoria (id, nombre) VALUES (1, "Aseo");
INSERT INTO Categoria (id, nombre) VALUES (2, "Cocina");
INSERT INTO Categoria (id, nombre) VALUES (3, "Tecnologia");