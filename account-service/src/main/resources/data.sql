-- Seed rider accounts for demo/testing
-- ON CONFLICT DO NOTHING safely skips the insert if firebase_uid already exists
INSERT INTO accounts (firebase_uid, email, name, phone, role, vehicle_type, is_available, created_at, updated_at)
VALUES ('customer-001', 'customer1@yafd.com', 'Hungry Alex', '+6598760001', 'CUSTOMER', NOW(), NOW())
ON CONFLICT (firebase_uid) DO NOTHING;

INSERT INTO accounts (firebase_uid, email, name, phone, role, vehicle_type, is_available, created_at, updated_at)
VALUES ('rider-001', 'rider1@yafd.com', 'Adam Rider', '+6591234001', 'RIDER', 'MOTORCYCLE', true, NOW(), NOW())
    ON CONFLICT (firebase_uid) DO NOTHING;

-- Seed a staff account
INSERT INTO accounts (firebase_uid, email, name, phone, role, created_at, updated_at)
VALUES ('staff-001', 'admin@yafd.com', 'YAFD Admin', '+6590000001', 'STAFF', NOW(), NOW())
ON CONFLICT (firebase_uid) DO NOTHING;

-- Re-sync the ID sequence to prevent duplicate key errors after seed inserts
SELECT setval('accounts_id_seq', (SELECT MAX(id) FROM accounts));

-- Seed test accounts matching load-test Firebase UIDs
INSERT INTO accounts (firebase_uid, email, name, phone, role, created_at, updated_at)
VALUES ('4Eaq5b7GopZg6vl7p0MstWE53b82', 'user1@yafd.com', 'Test User One', '+6591110001', 'CUSTOMER', NOW(), NOW())
ON CONFLICT (firebase_uid) DO NOTHING;

INSERT INTO accounts (firebase_uid, email, name, phone, role, created_at, updated_at)
VALUES ('0ZYBK2HNDoSZV4EURSLcLbpLMvC2', 'user2@yafd.com', 'Test User Two', '+6591110002', 'CUSTOMER', NOW(), NOW())
ON CONFLICT (firebase_uid) DO NOTHING;

-- Seed addresses (IDs 1 and 2 — must match ADDRESS_IDS in load-tests/order-service.js)
INSERT INTO addresses (account_id, label, street, city, postal_code, is_default, created_at, updated_at)
SELECT id, 'Home', '123 Orchard Road', 'Singapore', '238858', true, NOW(), NOW()
FROM accounts WHERE firebase_uid = '4Eaq5b7GopZg6vl7p0MstWE53b82'
ON CONFLICT DO NOTHING;

INSERT INTO addresses (account_id, label, street, city, postal_code, is_default, created_at, updated_at)
SELECT id, 'Home', '456 Marina Bay', 'Singapore', '018956', true, NOW(), NOW()
FROM accounts WHERE firebase_uid = '0ZYBK2HNDoSZV4EURSLcLbpLMvC2'
ON CONFLICT DO NOTHING;
