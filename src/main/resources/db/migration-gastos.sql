-- =====================================================================
-- Ejercicio #6 — Entidad Gasto
-- Script de migracion idempotente para la base compartida del curso.
--
-- IMPORTANTE: este script NO es destructivo y esta disenado para poder
-- ejecutarse sobre una base que YA contiene datos de otros ejercicios.
-- No contiene DROP, TRUNCATE ni ALTER. Si los objetos ya existen, no
-- hace nada. La base `app-gastos-ejercicio6_bd` en Alwaysdata es
-- compartida: cualquier sentencia destructiva afectaria a terceros.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Tabla de usuarios del dominio comun (id, clave, nombre, rol).
--    `id` actua como identificador de inicio de sesion y es la clave
--    referenciada por los gastos.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id      VARCHAR(50)  NOT NULL,
    clave   VARCHAR(255) NOT NULL,
    nombre  VARCHAR(100) NOT NULL,
    rol     VARCHAR(50)  NOT NULL,
    email   VARCHAR(100) NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 2. Tabla de la entidad asignada: Gasto.
--    Los 7 atributos de negocio exigidos por el ejercicio N.º 6:
--    fecha, valorTotalSinIVA, ivaTotal, valorTotalConIVA,
--    nombreUsuario, lugar y descripcion.
--
--    `nombre_usuario` es clave foranea contra `usuarios(id)`: todo gasto
--    debe pertenecer a un usuario existente. Esta restriccion la impone
--    la base y el dominio debe respetarla antes de persistir.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS gastos (
    id_gasto             INT(11)      NOT NULL AUTO_INCREMENT,
    fecha                DATE         NOT NULL,
    valor_total_sin_iva  DOUBLE       NOT NULL,
    iva_total            DOUBLE       NOT NULL,
    valor_total_con_iva  DOUBLE       NOT NULL,
    nombre_usuario       VARCHAR(50)  NOT NULL,
    lugar                VARCHAR(150) NOT NULL,
    descripcion          TEXT         NULL,
    PRIMARY KEY (id_gasto),
    KEY fk_usuario (nombre_usuario),
    CONSTRAINT fk_usuario FOREIGN KEY (nombre_usuario) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 3. Migracion del usuario administrador base.
--    INSERT IGNORE evita duplicar la fila si ya existe, de modo que el
--    script puede ejecutarse tantas veces como haga falta.
--    La clave se almacena cifrada con BCrypt; nunca en texto plano.
-- ---------------------------------------------------------------------
INSERT IGNORE INTO usuarios (id, clave, nombre, rol, email)
VALUES (
    'admin',
    '$2y$12$sfwkajsls.fktQb0Ngw5cOiSiV3z4LIwbucSAy4sw2wyNlxbUBt4q',
    'Administrador',
    'ADMIN',
    'rcarmonan@unicartagena.edu.co'
);

-- ---------------------------------------------------------------------
-- 4. Verificacion posterior (solo lectura).
-- ---------------------------------------------------------------------
-- SELECT COUNT(*) AS usuarios FROM usuarios;
-- SELECT COUNT(*) AS gastos   FROM gastos;
