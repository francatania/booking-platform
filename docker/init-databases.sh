#!/bin/bash
set -e

# Create databases
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE auth_db;
    CREATE DATABASE company_db;
    CREATE DATABASE booking_db;
EOSQL

# Seed auth_db
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "auth_db" <<-'EOSQL'
    CREATE TABLE users (
        id BIGSERIAL PRIMARY KEY,
        username VARCHAR(255) NOT NULL UNIQUE,
        email VARCHAR(255) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        role VARCHAR(50) NOT NULL,
        company_id BIGINT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    INSERT INTO users (username, email, password_hash, role, company_id) VALUES
        ('superadmin', 'superadmin@test.com', '$2a$10$0ZYC0pyxjVHgZJs0YP1VteiT3ofHmPF.oqzsinDzZ2R8SaM6N2kte', 'SUPER_ADMIN', NULL),
        ('admin', 'admin@test.com', '$2a$10$0ZYC0pyxjVHgZJs0YP1VteiT3ofHmPF.oqzsinDzZ2R8SaM6N2kte', 'ADMIN', 1),
        ('user', 'user@test.com', '$2a$10$0ZYC0pyxjVHgZJs0YP1VteiT3ofHmPF.oqzsinDzZ2R8SaM6N2kte', 'USER', NULL);
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
        ('Barber Shop', 'The best barber shop in town', 'Av. Corrientes 1234, CABA', '+54 11 1234-5678'),
        ('Spa Relax', 'Premium spa and wellness center', 'Av. Santa Fe 5678, CABA', '+54 11 8765-4321');

    INSERT INTO company_services (company_id, name, description, duration_minutes, price, is_active) VALUES
        (1, 'Corte de pelo', 'Corte clasico o moderno', 30, 5000.00, true),
        (1, 'Barba', 'Recorte y perfilado de barba', 20, 3000.00, true),
        (1, 'Corte + Barba', 'Combo completo', 45, 7000.00, true),
        (1, 'Tintura', 'Coloracion completa', 60, 10000.00, false),
        (2, 'Masaje relajante', 'Masaje de cuerpo completo', 60, 15000.00, true),
        (2, 'Facial express', 'Limpieza facial profunda', 30, 8000.00, true);
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
        (2, 30);

    INSERT INTO booking (user_id, service_id, company_id, start_time, end_time, status) VALUES
        (3, 1, 1, '2025-07-01 10:00:00', '2025-07-01 10:30:00', 'PENDING'),
        (3, 5, 2, '2025-07-01 14:00:00', '2025-07-01 15:00:00', 'PENDING'),
        (3, 2, 1, '2025-06-15 11:00:00', '2025-06-15 11:20:00', 'CANCELLED');
EOSQL
