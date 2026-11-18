CREATE DATABASE IF NOT EXISTS pagoflex_usuarios CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS pagoflex_compromisos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS pagoflex_pagos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS pagoflex_movimientos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON pagoflex_usuarios.* TO 'pagoflex'@'%';
GRANT ALL PRIVILEGES ON pagoflex_compromisos.* TO 'pagoflex'@'%';
GRANT ALL PRIVILEGES ON pagoflex_pagos.* TO 'pagoflex'@'%';
GRANT ALL PRIVILEGES ON pagoflex_movimientos.* TO 'pagoflex'@'%';
FLUSH PRIVILEGES;
