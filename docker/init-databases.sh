#!/bin/bash
set -e

# Create databases
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE auth_db;
    CREATE DATABASE company_db;
    CREATE DATABASE booking_db;
    CREATE DATABASE notification_db;
EOSQL

# Seed auth_db
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "auth_db" <<-'EOSQL'
    CREATE TABLE users (
        id BIGSERIAL PRIMARY KEY,
        username VARCHAR(255) NOT NULL UNIQUE,
        first_name VARCHAR(255) NOT NULL,
        last_name VARCHAR(255) NOT NULL,
        email VARCHAR(255) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        role VARCHAR(50) NOT NULL,
        company_id BIGINT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    INSERT INTO users (username, first_name, last_name, email, password_hash, role, company_id) VALUES
        ('superadmin', 'Super', 'Admin', 'superadmin@test.com', '$2a$10$0ZYC0pyxjVHgZJs0YP1VteiT3ofHmPF.oqzsinDzZ2R8SaM6N2kte', 'SUPER_ADMIN', NULL),
        ('admin', 'Marcos', 'Fernández', 'admin@test.com', '$2a$10$0ZYC0pyxjVHgZJs0YP1VteiT3ofHmPF.oqzsinDzZ2R8SaM6N2kte', 'ADMIN', 1),
        ('operator1', 'Carlos', 'Gómez', 'opbooklynow@yopmail.com', '$2a$10$0ZYC0pyxjVHgZJs0YP1VteiT3ofHmPF.oqzsinDzZ2R8SaM6N2kte', 'OPERATOR', 1),
        ('user', 'Juan', 'Pérez', 'user@test.com', '$2a$10$0ZYC0pyxjVHgZJs0YP1VteiT3ofHmPF.oqzsinDzZ2R8SaM6N2kte', 'USER', NULL),
        ('user2', 'Valentina', 'López', 'user2@test.com', '$2a$10$0ZYC0pyxjVHgZJs0YP1VteiT3ofHmPF.oqzsinDzZ2R8SaM6N2kte', 'USER', NULL),
        ('user3', 'Sebastián', 'Rodríguez', 'user3@test.com', '$2a$10$0ZYC0pyxjVHgZJs0YP1VteiT3ofHmPF.oqzsinDzZ2R8SaM6N2kte', 'USER', NULL),
        ('manager', 'Sebastián', 'Rodríguez', 'managbooklynow@yopmail.com', '$2a$10$0ZYC0pyxjVHgZJs0YP1VteiT3ofHmPF.oqzsinDzZ2R8SaM6N2kte', 'MANAGER', 1);
EOSQL

# Seed company_db
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "company_db" <<-'EOSQL'
    CREATE TABLE company (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(255) NOT NULL UNIQUE,
        description VARCHAR(255),
        address VARCHAR(255),
        phone VARCHAR(255),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE company_services (
        id BIGSERIAL PRIMARY KEY,
        company_id BIGINT NOT NULL REFERENCES company(id),
        name VARCHAR(255) NOT NULL,
        description VARCHAR(255),
        duration_minutes INTEGER NOT NULL,
        price DECIMAL NOT NULL,
        is_active BOOLEAN NOT NULL DEFAULT true,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    INSERT INTO company (name, description, address, phone) VALUES
        ('Barbería Clásica', 'La mejor barbería de la ciudad', 'Av. Corrientes 1234, CABA', '+54 11 1234-5678'),
        ('Spa Relax', 'Centro de bienestar y relajación premium', 'Av. Santa Fe 5678, CABA', '+54 11 8765-4321'),
        ('Clínica Dental Sonrisa', 'Atención odontológica integral', 'Av. Callao 890, CABA', '+54 11 2233-4455'),
        ('Centro de Yoga', 'Clases de yoga y meditación', 'Av. del Libertador 3456, CABA', '+54 11 6677-8899'),
        ('Fisioterapia Movimiento', 'Rehabilitación y fisioterapia', 'Av. Rivadavia 7890, CABA', '+54 11 9988-7766');

    INSERT INTO company_services (company_id, name, description, duration_minutes, price, is_active) VALUES
        (1, 'Corte de Cabello', 'Corte clásico o moderno', 30, 5000.00, true),
        (1, 'Arreglo de Barba', 'Perfilado y recorte de barba', 20, 3000.00, true),
        (1, 'Corte + Barba', 'Combo completo', 45, 7000.00, true),
        (1, 'Coloración', 'Coloración completa de cabello', 60, 10000.00, false),
        (1, 'Tratamiento de Keratina', 'Keratina profesional', 120, 18000.00, true),
        (2, 'Masaje Relajante', 'Masaje corporal completo', 60, 15000.00, true),
        (2, 'Facial Express', 'Limpieza facial profunda', 30, 8000.00, true),
        (2, 'Masaje Deportivo', 'Masaje de recuperación muscular', 45, 12000.00, true),
        (2, 'Aromaterapia', 'Sesión de aromaterapia con aceites esenciales', 60, 14000.00, true),
        (2, 'Exfoliación Corporal', 'Tratamiento de exfoliación de cuerpo completo', 50, 11000.00, true),
        (3, 'Limpieza Dental', 'Profilaxis y pulido dental', 45, 9000.00, true),
        (3, 'Blanqueamiento Dental', 'Blanqueamiento profesional', 60, 25000.00, true),
        (3, 'Revisión General', 'Revisión y diagnóstico dental', 30, 6000.00, true),
        (3, 'Consulta de Ortodoncia', 'Evaluación para brackets o alineadores', 40, 8000.00, true),
        (3, 'Extracción Simple', 'Extracción de pieza dental', 30, 12000.00, true),
        (4, 'Clase de Yoga', 'Clase grupal de yoga', 60, 4000.00, true),
        (4, 'Sesión Privada de Yoga', 'Clase personalizada uno a uno', 60, 9000.00, true),
        (4, 'Meditación Guiada', 'Sesión de meditación y mindfulness', 45, 5000.00, true),
        (4, 'Yoga Prenatal', 'Clase especial para embarazadas', 60, 5500.00, true),
        (5, 'Sesión de Fisioterapia', 'Fisioterapia y rehabilitación', 50, 8000.00, true),
        (5, 'Masaje Terapéutico', 'Masaje para afecciones musculares', 40, 10000.00, true),
        (5, 'Electroterapia', 'Tratamiento con corrientes terapéuticas', 30, 7000.00, true),
        (5, 'Evaluación Postural', 'Análisis y corrección postural', 60, 9500.00, true),
        (5, 'Pilates Terapéutico', 'Pilates orientado a la rehabilitación', 50, 8500.00, true);
EOSQL

# Seed booking_db
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "booking_db" <<-'EOSQL'
    CREATE TABLE booking (
        id SERIAL PRIMARY KEY,
        user_id INTEGER NOT NULL,
        service_id INTEGER NOT NULL,
        company_id INTEGER NOT NULL,
        start_time TIMESTAMP NOT NULL,
        end_time TIMESTAMP NOT NULL,
        price NUMERIC(10, 2) NOT NULL,
        status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE booking_config (
        id SERIAL PRIMARY KEY,
        company_id INTEGER NOT NULL UNIQUE,
        gap_minutes INTEGER NOT NULL DEFAULT 0
    );

    INSERT INTO booking_config (company_id, gap_minutes) VALUES
        (1, 15),
        (2, 30),
        (3, 10),
        (4, 0),
        (5, 10);

    INSERT INTO booking (user_id, service_id, company_id, start_time, end_time, price, status) VALUES
        -- Enero 2026
        (4, 1, 1, '2026-01-03 09:00:00', '2026-01-03 09:30:00', 5000.00, 'COMPLETED'),
        (5, 2, 1, '2026-01-05 10:00:00', '2026-01-05 10:20:00', 3000.00, 'COMPLETED'),
        (6, 3, 1, '2026-01-07 11:00:00', '2026-01-07 11:45:00', 7000.00, 'COMPLETED'),
        (4, 5, 1, '2026-01-10 10:00:00', '2026-01-10 12:00:00', 18000.00, 'COMPLETED'),
        (5, 1, 1, '2026-01-12 09:00:00', '2026-01-12 09:30:00', 5000.00, 'CANCELLED'),
        (6, 2, 1, '2026-01-14 10:00:00', '2026-01-14 10:20:00', 3000.00, 'COMPLETED'),
        (4, 3, 1, '2026-01-17 11:00:00', '2026-01-17 11:45:00', 7000.00, 'COMPLETED'),
        (5, 1, 1, '2026-01-20 09:00:00', '2026-01-20 09:30:00', 5000.00, 'COMPLETED'),
        (6, 5, 1, '2026-01-22 10:00:00', '2026-01-22 12:00:00', 18000.00, 'CANCELLED'),
        (4, 3, 1, '2026-01-25 11:00:00', '2026-01-25 11:45:00', 7000.00, 'COMPLETED'),
        -- Febrero 2026
        (5, 1, 1, '2026-02-02 09:00:00', '2026-02-02 09:30:00', 5000.00, 'COMPLETED'),
        (6, 2, 1, '2026-02-04 10:00:00', '2026-02-04 10:20:00', 3000.00, 'COMPLETED'),
        (4, 5, 1, '2026-02-06 10:00:00', '2026-02-06 12:00:00', 18000.00, 'COMPLETED'),
        (5, 3, 1, '2026-02-09 11:00:00', '2026-02-09 11:45:00', 7000.00, 'CANCELLED'),
        (6, 1, 1, '2026-02-11 09:00:00', '2026-02-11 09:30:00', 5000.00, 'COMPLETED'),
        (4, 2, 1, '2026-02-13 10:00:00', '2026-02-13 10:20:00', 3000.00, 'COMPLETED'),
        (5, 3, 1, '2026-02-17 11:00:00', '2026-02-17 11:45:00', 7000.00, 'COMPLETED'),
        (6, 5, 1, '2026-02-19 10:00:00', '2026-02-19 12:00:00', 18000.00, 'COMPLETED'),
        (4, 1, 1, '2026-02-21 09:00:00', '2026-02-21 09:30:00', 5000.00, 'CANCELLED'),
        (5, 3, 1, '2026-02-24 11:00:00', '2026-02-24 11:45:00', 7000.00, 'COMPLETED'),
        -- Marzo 2026
        (6, 1, 1, '2026-03-01 09:00:00', '2026-03-01 09:30:00', 5000.00, 'COMPLETED'),
        (4, 2, 1, '2026-03-01 10:00:00', '2026-03-01 10:20:00', 3000.00, 'COMPLETED'),
        (5, 3, 1, '2026-03-03 11:00:00', '2026-03-03 11:45:00', 7000.00, 'COMPLETED'),
        (6, 1, 1, '2026-03-05 09:00:00', '2026-03-05 09:30:00', 5000.00, 'CANCELLED'),
        (4, 5, 1, '2026-03-07 10:00:00', '2026-03-07 12:00:00', 18000.00, 'COMPLETED'),
        (5, 1, 1, '2026-03-10 09:00:00', '2026-03-10 09:30:00', 5000.00, 'COMPLETED'),
        (6, 3, 1, '2026-03-12 11:00:00', '2026-03-12 11:45:00', 7000.00, 'COMPLETED'),
        (4, 2, 1, '2026-03-15 10:00:00', '2026-03-15 10:20:00', 3000.00, 'COMPLETED'),
        (5, 1, 1, '2026-03-18 09:00:00', '2026-03-18 09:30:00', 5000.00, 'CANCELLED'),
        (6, 5, 1, '2026-03-20 10:00:00', '2026-03-20 12:00:00', 18000.00, 'COMPLETED'),
        (4, 3, 1, '2026-03-22 11:00:00', '2026-03-22 11:45:00', 7000.00, 'COMPLETED'),
        (5, 1, 1, '2026-03-25 09:00:00', '2026-03-25 09:30:00', 5000.00, 'PENDING'),
        (6, 2, 1, '2026-03-27 10:00:00', '2026-03-27 10:20:00', 3000.00, 'PENDING'),
        -- Abril 2026
        (4, 1, 1, '2026-04-01 09:00:00', '2026-04-01 09:30:00', 5000.00, 'COMPLETED'),
        (5, 3, 1, '2026-04-01 10:00:00', '2026-04-01 10:45:00', 7000.00, 'COMPLETED'),
        (6, 2, 1, '2026-04-02 09:00:00', '2026-04-02 09:20:00', 3000.00, 'COMPLETED'),
        (4, 5, 1, '2026-04-02 10:00:00', '2026-04-02 12:00:00', 18000.00, 'COMPLETED'),
        (5, 1, 1, '2026-04-03 09:00:00', '2026-04-03 09:30:00', 5000.00, 'COMPLETED'),
        (6, 3, 1, '2026-04-03 11:00:00', '2026-04-03 11:45:00', 7000.00, 'CANCELLED'),
        (4, 2, 1, '2026-04-04 10:00:00', '2026-04-04 10:20:00', 3000.00, 'COMPLETED'),
        (5, 5, 1, '2026-04-05 10:00:00', '2026-04-05 12:00:00', 18000.00, 'COMPLETED'),
        (6, 1, 1, '2026-04-05 09:00:00', '2026-04-05 09:30:00', 5000.00, 'COMPLETED'),
        (4, 3, 1, '2026-04-07 11:00:00', '2026-04-07 11:45:00', 7000.00, 'COMPLETED'),
        (5, 2, 1, '2026-04-07 09:00:00', '2026-04-07 09:20:00', 3000.00, 'COMPLETED'),
        (6, 1, 1, '2026-04-08 09:00:00', '2026-04-08 09:30:00', 5000.00, 'COMPLETED'),
        (4, 5, 1, '2026-04-09 10:00:00', '2026-04-09 12:00:00', 18000.00, 'CANCELLED'),
        (5, 3, 1, '2026-04-09 11:00:00', '2026-04-09 11:45:00', 7000.00, 'COMPLETED'),
        (6, 2, 1, '2026-04-10 09:00:00', '2026-04-10 09:20:00', 3000.00, 'COMPLETED'),
        (4, 1, 1, '2026-04-11 09:00:00', '2026-04-11 09:30:00', 5000.00, 'COMPLETED'),
        (5, 3, 1, '2026-04-12 11:00:00', '2026-04-12 11:45:00', 7000.00, 'COMPLETED'),
        (6, 5, 1, '2026-04-14 10:00:00', '2026-04-14 12:00:00', 18000.00, 'COMPLETED'),
        (4, 2, 1, '2026-04-14 09:00:00', '2026-04-14 09:20:00', 3000.00, 'COMPLETED'),
        (5, 1, 1, '2026-04-15 09:00:00', '2026-04-15 09:30:00', 5000.00, 'COMPLETED');
EOSQL

# Seed notification_db
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "notification_db" <<-'EOSQL'
    CREATE TABLE notification (
        id SERIAL PRIMARY KEY,
        user_id INTEGER NOT NULL,
        type VARCHAR(50) NOT NULL,
        message TEXT NOT NULL,
        is_read BOOLEAN NOT NULL DEFAULT false,
        booking_id INTEGER,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    CREATE INDEX idx_notification_user_id ON notification(user_id);
    CREATE INDEX idx_notification_user_read ON notification(user_id, is_read);
EOSQL
