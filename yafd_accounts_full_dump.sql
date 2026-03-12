-- =============================================
-- FULL YAFD SQL DUMP (Tables 1–6)
-- Includes DROP TABLE IF EXISTS statements
-- docker exec -i yafd-postgres psql -U yafd -d yafd_accounts < yafd_accounts_full_dump.sql
-- =============================================

-- 1️⃣ Drop tables first (clean slate)
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS menu CASCADE;
DROP TABLE IF EXISTS addresses CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;

-- =============================================
-- 2️⃣ accounts table
-- =============================================
CREATE TABLE accounts (
    id SERIAL PRIMARY KEY,
    firebase_uid VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL, -- user, rider, admin
    vehicle_type VARCHAR(50),
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO accounts (firebase_uid, name, email, phone, role, vehicle_type, is_available)
VALUES
('firebase_uid_1', 'Alice Tan', 'alice@example.com', '91234567', 'user', NULL, TRUE),
('firebase_uid_2', 'Bob Lim', 'bob@example.com', '92345678', 'user', NULL, TRUE),
('firebase_uid_3', 'Charlie Ng', 'charlie@example.com', '93456789', 'user', NULL, TRUE),
('firebase_uid_4', 'David Wong', 'david@example.com', '94567890', 'rider', 'motorbike', TRUE),
('firebase_uid_5', 'Eva Lee', 'eva@example.com', '95678901', 'admin', NULL, TRUE);

-- =============================================
-- 3️⃣ addresses table
-- =============================================
CREATE TABLE addresses (
    id SERIAL PRIMARY KEY,
    account_id INT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    street VARCHAR(255),
    city VARCHAR(100),
    postal_code VARCHAR(20)
);

INSERT INTO addresses (account_id, street, city, postal_code)
VALUES
(1, '123 Main St', 'Singapore', '123456'),
(1, '456 Second Ave', 'Singapore', '234567'),
(2, '789 Third Blvd', 'Singapore', '345678'),
(3, '321 Fourth Rd', 'Singapore', '456789'),
(4, '654 Fifth Ln', 'Singapore', '567890');

-- =============================================
-- 4️⃣ menu table
-- =============================================
CREATE TABLE menu (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(6,2) NOT NULL,
    category VARCHAR(50),
    available BOOLEAN DEFAULT TRUE,
    image_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO menu (name, description, price, category, available, image_url)
VALUES
('Nasi Lemak', 'Coconut rice with sambal, egg, and anchovies', 5.50, 'Rice', TRUE, 'C:/Users/jblam/yafd-platform/frontend/images/nasilemak.jpg'),
('Chicken Rice', 'Hainanese chicken with fragrant rice', 4.50, 'Rice', TRUE, 'C:/Users/jblam/yafd-platform/frontend/images/chickenrice.jpg'),
('Laksa', 'Spicy coconut laksa noodle soup with shrimp', 6.00, 'Noodles', TRUE, 'C:/Users/jblam/yafd-platform/frontend/images/laksa.jpg'),
('Char Kway Teow', 'Stir-fried flat noodles with egg and cockles', 5.00, 'Noodles', TRUE, 'C:/Users/jblam/yafd-platform/frontend/images/char-kway-teow.jpg'),
('Fish and Chips', 'Deep-fried fish with fries', 7.00, 'Western', TRUE, 'C:/Users/jblam/yafd-platform/frontend/images/fish-chips.jpg');

-- =============================================
-- 5️⃣ orders table
-- =============================================
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    account_id INT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    address_id INT REFERENCES addresses(id) ON DELETE SET NULL,
    total_price NUMERIC(8,2),
    status VARCHAR(50) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO orders (account_id, address_id, total_price, status)
VALUES
(1, 1, 10.50, 'pending'),
(2, 3, 7.50, 'confirmed');

-- =============================================
-- 6️⃣ order_items table
-- =============================================
CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_id INT NOT NULL REFERENCES menu(id) ON DELETE CASCADE,
    quantity INT DEFAULT 1,
    price NUMERIC(6,2)
);

INSERT INTO order_items (order_id, menu_id, quantity, price)
VALUES
(1, 1, 1, 5.50),
(1, 2, 1, 5.00),
(2, 3, 1, 6.00),
(2, 5, 1, 1.50);

-- =============================================
-- 7️⃣ payments table
-- =============================================
CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    amount NUMERIC(8,2),
    method VARCHAR(50),
    status VARCHAR(50) DEFAULT 'paid',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO payments (order_id, amount, method)
VALUES
(1, 10.50, 'Cash'),
(2, 7.50, 'Credit Card');