CREATE TABLE IF NOT EXISTS ficha_familiar (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(255) NOT NULL,
    cedula VARCHAR(10) NOT NULL,
    celular VARCHAR(30) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS nino_hijo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ficha_familiar_id BIGINT NOT NULL,
    nombre_completo VARCHAR(255) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    CONSTRAINT fk_nino_ficha FOREIGN KEY (ficha_familiar_id) REFERENCES ficha_familiar (id)
);
