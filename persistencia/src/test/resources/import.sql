-- Hibernate ejecuta este archivo una sola vez, justo despues de crear el esquema (ddl-auto=create).
-- Hace que los ids generados empiecen en 1000 para que nunca choquen con los ids fijos de unishop.sql.
UPDATE hibernate_sequence SET next_val = 1000;
