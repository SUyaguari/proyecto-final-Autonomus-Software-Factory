-- Datos precargados para desarrollo local y como fixture de pruebas de
-- integración/funcionales (ver quickstart.md). No se usan en producción.

MERGE INTO ficha_familiar (id, nombre_completo, cedula, celular, direccion, estado, fecha_creacion) VALUES
    (1, 'Maria Perez', '1710034065', '0991234567', 'Av. Siempre Viva 123', 'COMPLETA', '2026-07-01 09:00:00'),
    (2, 'Carlos Ruiz', '1710034016', '0987654321', 'Calle Falsa 456', 'INCOMPLETA', '2026-07-02 10:00:00');

MERGE INTO nino_hijo (id, ficha_familiar_id, nombre_completo, fecha_nacimiento) VALUES
    (1, 1, 'Juan Perez', '2016-05-10'),
    (2, 1, 'Ana Perez', '2018-09-02');

-- RESTART WITH se calcula dinámicamente (en vez de un literal fijo) porque este script
-- se re-ejecuta en cada arranque de contexto de Spring (spring.sql.init.mode: always)
-- contra la misma base H2 en memoria durante una misma ejecución de pruebas; con un
-- literal fijo, una segunda ejecución podría colisionar con filas ya autogeneradas por
-- pruebas anteriores en la misma corrida.
ALTER TABLE ficha_familiar ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 2) + 1 FROM ficha_familiar);
ALTER TABLE nino_hijo ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 2) + 1 FROM nino_hijo);
