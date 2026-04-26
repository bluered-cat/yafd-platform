-- Seed rider accounts for demo/testing
-- ON CONFLICT DO NOTHING safely skips the insert if firebase_uid already exists
INSERT INTO accounts (firebase_uid, email, name, phone, role, vehicle_type, is_available, created_at, updated_at)
VALUES ('rider-001', 'rider1@yafd.com', 'Ahmad Rider', '+6591234001', 'RIDER', 'MOTORCYCLE', true, NOW(), NOW())
ON CONFLICT (firebase_uid) DO NOTHING;

INSERT INTO accounts (firebase_uid, email, name, phone, role, vehicle_type, is_available, created_at, updated_at)
VALUES ('rider-002', 'rider2@yafd.com', 'Benny Cyclist', '+6591234002', 'RIDER', 'BICYCLE', true, NOW(), NOW())
ON CONFLICT (firebase_uid) DO NOTHING;

INSERT INTO accounts (firebase_uid, email, name, phone, role, vehicle_type, is_available, created_at, updated_at)
VALUES ('rider-003', 'rider3@yafd.com', 'Charlie Driver', '+6591234003', 'RIDER', 'CAR', true, NOW(), NOW())
ON CONFLICT (firebase_uid) DO NOTHING;

INSERT INTO accounts (firebase_uid, email, name, phone, role, vehicle_type, is_available, created_at, updated_at)
VALUES ('rider-004', 'rider4@yafd.com', 'Devi Express', '+6591234004', 'RIDER', 'MOTORCYCLE', true, NOW(), NOW())
ON CONFLICT (firebase_uid) DO NOTHING;

INSERT INTO accounts (firebase_uid, email, name, phone, role, vehicle_type, is_available, created_at, updated_at)
VALUES ('rider-005', 'rider5@yafd.com', 'Eddie Swift', '+6591234005', 'RIDER', 'MOTORCYCLE', true, NOW(), NOW())
ON CONFLICT (firebase_uid) DO NOTHING;

-- Seed a staff account
INSERT INTO accounts (firebase_uid, email, name, phone, role, created_at, updated_at)
VALUES ('staff-001', 'admin@yafd.com', 'YAFD Admin', '+6590000001', 'STAFF', NOW(), NOW())
ON CONFLICT (firebase_uid) DO NOTHING;

-- Re-sync the ID sequence to prevent duplicate key errors after seed inserts
SELECT setval('accounts_id_seq', (SELECT MAX(id) FROM accounts));
