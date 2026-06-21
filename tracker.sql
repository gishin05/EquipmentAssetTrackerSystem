-- ============================================================
-- tracker.sql
-- Schema definition for tracker.db (SQLite)
-- Equipment Asset Tracker System
-- ============================================================

-- Enable foreign key enforcement (SQLite has this OFF by default)
PRAGMA foreign_keys = ON;

-- ============================================================
-- 1. CATEGORIES
-- ============================================================
CREATE TABLE IF NOT EXISTS categories (
    category_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    category_name TEXT    NOT NULL UNIQUE
);

-- ============================================================
-- 2. USERS
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id          INTEGER PRIMARY KEY AUTOINCREMENT,
    username         TEXT    NOT NULL UNIQUE,
    password_hash    TEXT    NOT NULL,
    user_role        TEXT    NOT NULL,          -- ADMIN | BORROWER
    email            TEXT,
    full_name        TEXT,
    theme_preference TEXT
);

-- ============================================================
-- 3. EQUIPMENT
-- ============================================================
CREATE TABLE IF NOT EXISTS equipment (
    equipment_id             INTEGER PRIMARY KEY AUTOINCREMENT,
    equipment_name           TEXT,
    serial_number            TEXT    NOT NULL UNIQUE,
    category_id              INTEGER,
    technical_specifications TEXT,
    storage_location         TEXT,
    purchase_cost            REAL,
    purchase_date            TEXT,
    equipment_status         TEXT,              -- AVAILABLE | BORROWED | IN_MAINTENANCE
    assigned_to              TEXT,
    FOREIGN KEY (category_id) REFERENCES categories (category_id)
);

-- ============================================================
-- 4. BOOKINGS
-- ============================================================
CREATE TABLE IF NOT EXISTS bookings (
    booking_id             INTEGER PRIMARY KEY AUTOINCREMENT,
    equipment_id           INTEGER,
    borrower_id            INTEGER,
    admin_id               INTEGER,
    start_datetime         TEXT,
    expected_return_datetime TEXT,
    actual_return_datetime TEXT,
    purpose_description    TEXT,
    booking_status         TEXT,                -- PENDING | APPROVED | REJECTED | RETURNED
    approval_status        TEXT,
    rejection_reason       TEXT,
    returned_condition     TEXT,
    borrowing_price        REAL,
    FOREIGN KEY (equipment_id) REFERENCES equipment (equipment_id),
    FOREIGN KEY (borrower_id)  REFERENCES users (user_id),
    FOREIGN KEY (admin_id)     REFERENCES users (user_id)
);

-- ============================================================
-- 5. MAINTENANCE LOGS
-- ============================================================
CREATE TABLE IF NOT EXISTS maintenance_logs (
    log_id              INTEGER PRIMARY KEY AUTOINCREMENT,
    equipment_id        INTEGER,
    defect_description  TEXT,
    parts_cost          REAL,
    technician_details  TEXT,
    start_date          TEXT,
    completion_date     TEXT,
    repair_status       TEXT,
    FOREIGN KEY (equipment_id) REFERENCES equipment (equipment_id)
);

-- ============================================================
-- 6. AUDIT LOGS
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_logs (
    audit_id         INTEGER PRIMARY KEY AUTOINCREMENT,
    action_type      TEXT,
    affected_table   TEXT,
    record_id        INTEGER,
    action_timestamp TEXT,
    user_id          INTEGER,
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);

-- ============================================================
-- 7. CHANGE LOGS
-- ============================================================
CREATE TABLE IF NOT EXISTS change_logs (
    change_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    table_name       TEXT    NOT NULL,
    record_id        INTEGER,
    record_name      TEXT,
    field_name       TEXT,
    old_value        TEXT,
    new_value        TEXT,
    change_timestamp TEXT,
    user_id          INTEGER,
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);

-- ============================================================
-- INDEXES  (improve lookup performance)
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_equipment_serial     ON equipment (serial_number);
CREATE INDEX IF NOT EXISTS idx_equipment_category   ON equipment (category_id);
CREATE INDEX IF NOT EXISTS idx_equipment_status     ON equipment (equipment_status);

CREATE INDEX IF NOT EXISTS idx_bookings_equipment   ON bookings (equipment_id);
CREATE INDEX IF NOT EXISTS idx_bookings_borrower    ON bookings (borrower_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status      ON bookings (booking_status);

CREATE INDEX IF NOT EXISTS idx_maintenance_equip    ON maintenance_logs (equipment_id);

CREATE INDEX IF NOT EXISTS idx_audit_user           ON audit_logs (user_id);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp      ON audit_logs (action_timestamp);

CREATE INDEX IF NOT EXISTS idx_changelog_table      ON change_logs (table_name);
CREATE INDEX IF NOT EXISTS idx_changelog_user       ON change_logs (user_id);

-- ============================================================
-- SEED DATA  — Default admin account
-- Password: "Admin"  (SHA-256 hash)
-- ============================================================
INSERT OR IGNORE INTO users (username, password_hash, user_role)
VALUES (
    'Admin',
    'c1c224b03cd9bc7b6a86d77f5dace40191766c485cd55dc48caf9ac873335d6f',
    'ADMIN'
);
