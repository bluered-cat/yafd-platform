-- Seed rider accounts for demo/testing
INSERT INTO accounts (firebase_uid, email, name, phone, role, vehicle_type, is_available, created_at, updated_at)
SELECT 'rider-001', 'rider1@yafd.com', 'Ahmad Rider', '+6591234001', 'RIDER', 'MOTORCYCLE', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE firebase_uid = 'rider-001');

INSERT INTO accounts (firebase_uid, email, name, phone, role, vehicle_type, is_available, created_at, updated_at)
SELECT 'rider-002', 'rider2@yafd.com', 'Benny Cyclist', '+6591234002', 'RIDER', 'BICYCLE', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE firebase_uid = 'rider-002');

INSERT INTO accounts (firebase_uid, email, name, phone, role, vehicle_type, is_available, created_at, updated_at)
SELECT 'rider-003', 'rider3@yafd.com', 'Charlie Driver', '+6591234003', 'RIDER', 'CAR', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE firebase_uid = 'rider-003');

INSERT INTO accounts (firebase_uid, email, name, phone, role, vehicle_type, is_available, created_at, updated_at)
SELECT 'rider-004', 'rider4@yafd.com', 'Devi Express', '+6591234004', 'RIDER', 'MOTORCYCLE', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE firebase_uid = 'rider-004');

INSERT INTO accounts (firebase_uid, email, name, phone, role, vehicle_type, is_available, created_at, updated_at)
SELECT 'rider-005', 'rider5@yafd.com', 'Eddie Swift', '+6591234005', 'RIDER', 'MOTORCYCLE', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE firebase_uid = 'rider-005');

-- Seed a staff account
INSERT INTO accounts (firebase_uid, email, name, phone, role, created_at, updated_at)
SELECT 'staff-001', 'admin@yafd.com', 'YAFD Admin', '+6590000001', 'STAFF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE firebase_uid = 'staff-001');
