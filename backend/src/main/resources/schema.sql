-- ============================================================
--  Hospital Management System - Database Schema
--  Database: hospital_mgmt
--  Run this script once to initialise the schema and seed data
-- ============================================================

CREATE DATABASE IF NOT EXISTS hospital_mgmt CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hospital_mgmt;

-- ── Users ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    full_name  VARCHAR(100) NOT NULL,
    role       ENUM('ADMIN','DOCTOR','RECEPTIONIST') NOT NULL,
    email      VARCHAR(100),
    phone      VARCHAR(20),
    active     BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ── Patients ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS patients (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id       VARCHAR(20) NOT NULL UNIQUE,
    full_name        VARCHAR(100) NOT NULL,
    age              INT NOT NULL,
    gender           ENUM('MALE','FEMALE','OTHER') NOT NULL,
    contact          VARCHAR(20) NOT NULL,
    blood_group      VARCHAR(5),
    address          TEXT,
    admission_date   DATE NOT NULL,
    ward             VARCHAR(50),
    room_number      VARCHAR(20),
    assigned_doctor_id BIGINT,
    status           ENUM('ADMITTED','DISCHARGED','OUTPATIENT') DEFAULT 'ADMITTED',
    emergency_notes  TEXT,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_doctor_id) REFERENCES users(id)
);

-- ── Appointments ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS appointments (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id     BIGINT NOT NULL,
    doctor_id      BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    department     VARCHAR(100),
    reason         TEXT,
    notes          TEXT,
    status         ENUM('SCHEDULED','COMPLETED','CANCELLED') DEFAULT 'SCHEDULED',
    created_by     BIGINT,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (doctor_id) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- ── Inventory ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS inventory (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_name  VARCHAR(150) NOT NULL,
    batch_number   VARCHAR(50),
    quantity       INT NOT NULL DEFAULT 0,
    unit           VARCHAR(30),
    supplier       VARCHAR(100),
    expiry_date    DATE,
    reorder_level  INT DEFAULT 10,
    purchase_price DECIMAL(10,2),
    selling_price  DECIMAL(10,2),
    category       VARCHAR(50),
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ── AI Logs ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ai_logs (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    feature      ENUM('CASE_TWIN','XRAY_ANALYSIS') NOT NULL,
    patient_id   BIGINT,
    performed_by BIGINT,
    input_summary TEXT,
    result       LONGTEXT,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (performed_by) REFERENCES users(id)
);

-- ── Seed: default users (password = 'password' bcrypt encoded) ──
-- BCrypt of 'password': $2a$12$Ht0L7ZlWuHVGdpDhJWk1JuYB0Iz2WFkAdNjYV0ATfbTOEBB6.KA2.
INSERT IGNORE INTO users (username, password, full_name, role, email) VALUES
('admin',       '$2a$12$Ht0L7ZlWuHVGdpDhJWk1JuYB0Iz2WFkAdNjYV0ATfbTOEBB6.KA2.', 'System Admin',      'ADMIN',        'admin@hospital.com'),
('dr.smith',    '$2a$12$Ht0L7ZlWuHVGdpDhJWk1JuYB0Iz2WFkAdNjYV0ATfbTOEBB6.KA2.', 'Dr. John Smith',    'DOCTOR',       'smith@hospital.com'),
('receptionist','$2a$12$Ht0L7ZlWuHVGdpDhJWk1JuYB0Iz2WFkAdNjYV0ATfbTOEBB6.KA2.', 'Sarah Johnson',     'RECEPTIONIST', 'sarah@hospital.com');

-- ── Seed: sample inventory ───────────────────────────────────
INSERT IGNORE INTO inventory (medicine_name, batch_number, quantity, unit, supplier, expiry_date, reorder_level, purchase_price, selling_price, category) VALUES
('Paracetamol 500mg', 'B001', 500, 'Tablets', 'PharmaCo',    '2026-12-31', 50, 0.50, 1.20, 'Analgesic'),
('Amoxicillin 250mg', 'B002', 200, 'Capsules','MediSupply',  '2025-10-31', 30, 1.20, 2.50, 'Antibiotic'),
('Ibuprofen 400mg',   'B003', 350, 'Tablets', 'PharmaCo',    '2026-06-30', 40, 0.80, 1.80, 'Analgesic'),
('Normal Saline 1L',  'B004', 100, 'Bottles', 'SalinePlus',  '2025-08-31', 20, 3.00, 6.50, 'IV Fluid'),
('Metformin 500mg',   'B005', 150, 'Tablets', 'DiaCare',     '2026-03-31', 25, 0.60, 1.40, 'Antidiabetic');
