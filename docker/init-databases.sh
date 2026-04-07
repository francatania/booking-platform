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
        ('admin', 'Admin', 'User', 'admin@test.com', '$2a$10$0ZYC0pyxjVHgZJs0YP1VteiT3ofHmPF.oqzsinDzZ2R8SaM6N2kte', 'ADMIN', 1),
        ('operator1', 'Carlos', 'Gomez', 'operator@test.com', '$2a$10$0ZYC0pyxjVHgZJs0YP1VteiT3ofHmPF.oqzsinDzZ2R8SaM6N2kte', 'OPERATOR', 1),
        ('user', 'John', 'Doe', 'user@test.com', '$2a$10$0ZYC0pyxjVHgZJs0YP1VteiT3ofHmPF.oqzsinDzZ2R8SaM6N2kte', 'USER', NULL),
        ('user2', 'Jane', 'Smith', 'user2@test.com', '$2a$10$0ZYC0pyxjVHgZJs0YP1VteiT3ofHmPF.oqzsinDzZ2R8SaM6N2kte', 'USER', NULL),
        ('user3', 'Michael', 'Brown', 'user3@test.com', '$2a$10$0ZYC0pyxjVHgZJs0YP1VteiT3ofHmPF.oqzsinDzZ2R8SaM6N2kte', 'USER', NULL);
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
        ('Classic Barber Shop', 'The best barber shop in town', '1234 Corrientes Ave, CABA', '+54 11 1234-5678'),
        ('Relax Spa', 'Premium spa and wellness center', '5678 Santa Fe Ave, CABA', '+54 11 8765-4321'),
        ('Smile Dental Clinic', 'Comprehensive dental care', '890 Callao Ave, CABA', '+54 11 2233-4455'),
        ('Yoga Center', 'Yoga and meditation classes', '3456 Libertador Ave, CABA', '+54 11 6677-8899'),
        ('Movement Physiotherapy', 'Rehabilitation and physiotherapy', '7890 Rivadavia Ave, CABA', '+54 11 9988-7766');

    INSERT INTO company_services (company_id, name, description, duration_minutes, price, is_active) VALUES
        (1, 'Haircut', 'Classic or modern haircut', 30, 5000.00, true),
        (1, 'Beard Trim', 'Beard shaping and trimming', 20, 3000.00, true),
        (1, 'Haircut + Beard', 'Full combo', 45, 7000.00, true),
        (1, 'Hair Coloring', 'Full hair coloring', 60, 10000.00, false),
        (1, 'Keratin Treatment', 'Professional keratin treatment', 120, 18000.00, true),
        (2, 'Relaxing Massage', 'Full body relaxing massage', 60, 15000.00, true),
        (2, 'Express Facial', 'Deep facial cleansing', 30, 8000.00, true),
        (2, 'Sports Massage', 'Muscle recovery massage', 45, 12000.00, true),
        (2, 'Aromatherapy', 'Aromatherapy session with essential oils', 60, 14000.00, true),
        (2, 'Body Scrub', 'Full body exfoliation treatment', 50, 11000.00, true),
        (3, 'Dental Cleaning', 'Prophylaxis and dental polish', 45, 9000.00, true),
        (3, 'Teeth Whitening', 'Professional teeth whitening', 60, 25000.00, true),
        (3, 'General Checkup', 'Dental revision and diagnosis', 30, 6000.00, true),
        (3, 'Orthodontics Consult', 'Evaluation for braces or aligners', 40, 8000.00, true),
        (3, 'Simple Extraction', 'Tooth extraction', 30, 12000.00, true),
        (4, 'Yoga Class', 'Group yoga class', 60, 4000.00, true),
        (4, 'Private Yoga Session', 'Personalized one-on-one class', 60, 9000.00, true),
        (4, 'Guided Meditation', 'Meditation and mindfulness session', 45, 5000.00, true),
        (4, 'Prenatal Yoga', 'Special class for pregnant women', 60, 5500.00, true),
        (5, 'Physiotherapy Session', 'Physiotherapy and rehabilitation', 50, 8000.00, true),
        (5, 'Therapeutic Massage', 'Massage for muscular conditions', 40, 10000.00, true),
        (5, 'Electrotherapy', 'Treatment with therapeutic currents', 30, 7000.00, true),
        (5, 'Postural Assessment', 'Postural analysis and correction', 60, 9500.00, true),
        (5, 'Therapeutic Pilates', 'Pilates focused on rehabilitation', 50, 8500.00, true);
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
        -- January 2026
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
        -- February 2026
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
        -- March 2026
        (6, 1, 1, '2026-03-01 09:00:00', '2026-03-01 09:30:00', 5000.00, 'COMPLETED'),
        (4, 2, 1, '2026-03-01 10:00:00', '2026-03-01 10:20:00', 3000.00, 'COMPLETED'),
        (5, 3, 1, '2026-03-03 11:00:00', '2026-03-03 11:45:00', 7000.00, 'COMPLETED'),
        (6, 1, 1, '2026-03-05 09:00:00', '2026-03-05 09:30:00', 5000.00, 'CANCELLED'),
        (4, 5, 1, '2026-03-07 10:00:00', '2026-03-07 12:00:00', 18000.00, 'CONFIRMED'),
        (5, 1, 1, '2026-03-10 09:00:00', '2026-03-10 09:30:00', 5000.00, 'COMPLETED'),
        (6, 3, 1, '2026-03-12 11:00:00', '2026-03-12 11:45:00', 7000.00, 'COMPLETED'),
        (4, 2, 1, '2026-03-15 10:00:00', '2026-03-15 10:20:00', 3000.00, 'CONFIRMED'),
        (5, 1, 1, '2026-03-18 09:00:00', '2026-03-18 09:30:00', 5000.00, 'CANCELLED'),
        (6, 5, 1, '2026-03-20 10:00:00', '2026-03-20 12:00:00', 18000.00, 'COMPLETED'),
        (4, 3, 1, '2026-03-22 11:00:00', '2026-03-22 11:45:00', 7000.00, 'COMPLETED'),
        (5, 1, 1, '2026-03-25 09:00:00', '2026-03-25 09:30:00', 5000.00, 'PENDING'),
        (6, 2, 1, '2026-03-27 10:00:00', '2026-03-27 10:20:00', 3000.00, 'PENDING');
EOSQL

# Seed notification_db
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "notification_db" <<-'EOSQL'
    CREATE TABLE notification (
        id SERIAL PRIMARY KEY,
        user_id INTEGER NOT NULL,
        type VARCHAR(50) NOT NULL,
        title VARCHAR(255) NOT NULL,
        message TEXT NOT NULL,
        is_read BOOLEAN NOT NULL DEFAULT false,
        booking_id INTEGER,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    CREATE INDEX idx_notification_user_id ON notification(user_id);
    CREATE INDEX idx_notification_user_read ON notification(user_id, is_read);
EOSQL
