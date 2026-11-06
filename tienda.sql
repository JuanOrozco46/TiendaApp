-- ============================================================
-- SCRIPT DE BASE DE DATOS: TiendaApp (tienda_db)
-- Proyecto: API REST con autenticación JWT y control de roles
-- ============================================================

CREATE SCHEMA IF NOT EXISTS tienda_db;
USE tienda_db;

-- 1. Tabla: roles
CREATE TABLE IF NOT EXISTS roles (
  id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL
);

-- 2. Tabla: users
CREATE TABLE IF NOT EXISTS users (
  id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  rol_id INT NOT NULL,
  FOREIGN KEY (rol_id) REFERENCES roles (id)
);

-- 3. Tabla: categories
CREATE TABLE IF NOT EXISTS categories (
  id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL UNIQUE
);

-- 4. Tabla: products
CREATE TABLE IF NOT EXISTS products (
  id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  name VARCHAR(150) NOT NULL,
  description VARCHAR(500),
  price DECIMAL(12,2) NOT NULL,
  stock INT NOT NULL,
  category_id INT NOT NULL,
  seller_id INT NOT NULL,
  FOREIGN KEY (category_id) REFERENCES categories (id),
  FOREIGN KEY (seller_id) REFERENCES users (id)
);

-- 5. Tabla: orders
CREATE TABLE IF NOT EXISTS orders (
  id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  client_id INT NOT NULL,
  product_id INT NOT NULL,
  quantity INT NOT NULL,
  total DECIMAL(12,2) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (client_id) REFERENCES users (id),
  FOREIGN KEY (product_id) REFERENCES products (id)
);

-- Datos iniciales: Roles
INSERT INTO roles (id, name) VALUES 
  (1, 'ADMIN'),
  (2, 'CLIENTE'),
  (3, 'VENDEDOR')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Usuario ADMIN inicial
-- Credenciales:
-- Username: admin
-- Email: admin@tienda.com
-- Contraseña en texto plano: admin123
-- Hash BCrypt: $2a$10$NbUom3pEENiqBdp9Y4PnVupsPnt4n9spv4jg9Jzo8aNvQolczCIp2
INSERT INTO users (id, username, email, password, active, rol_id)
VALUES (1, 'admin', 'admin@tienda.com', '$2a$10$NbUom3pEENiqBdp9Y4PnVupsPnt4n9spv4jg9Jzo8aNvQolczCIp2', TRUE, 1)
ON DUPLICATE KEY UPDATE 
  password = VALUES(password),
  active = VALUES(active),
  rol_id = VALUES(rol_id);
