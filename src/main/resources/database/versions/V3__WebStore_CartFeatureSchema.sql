-- Create Sequences for Cart tables
CREATE SEQUENCE IF NOT EXISTS web_store.seq_cart_status_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_cart_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_cart_history_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_cart_product_status_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_cart_product_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_cart_product_history_id START WITH 1 INCREMENT BY 1;

-- Add phone_number column to existing users table
ALTER TABLE web_store.users
ADD COLUMN IF NOT EXISTS phone_number VARCHAR(15) UNIQUE;

-- Update existing users with sample phone numbers (for testing)
UPDATE web_store.users SET phone_number = '1234567890' WHERE username = 'admin' AND phone_number IS NULL;
UPDATE web_store.users SET phone_number = '9876543210' WHERE username = 'user1' AND phone_number IS NULL;
UPDATE web_store.users SET phone_number = '5555555555' WHERE username = 'system' AND phone_number IS NULL;

-- Make phone_number NOT NULL after updating existing records
ALTER TABLE web_store.users
ALTER COLUMN phone_number SET NOT NULL;

-- Create Cart_Status Table
CREATE TABLE IF NOT EXISTS web_store.cart_status (
    status_id INT NOT NULL DEFAULT nextval('web_store.seq_cart_status_id') PRIMARY KEY,
    status_name VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_cart_status_name UNIQUE (status_name)
);

-- Create Cart Table
CREATE TABLE IF NOT EXISTS web_store.cart (
    cart_id BIGINT NOT NULL DEFAULT nextval('web_store.seq_cart_id') PRIMARY KEY,
    phone_number VARCHAR(15) NOT NULL,
    catalogue_category_id INT NOT NULL,
    status_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_user_phone
        FOREIGN KEY (phone_number) REFERENCES web_store.users (phone_number),
    CONSTRAINT fk_cart_catalogue_category
        FOREIGN KEY (catalogue_category_id) REFERENCES web_store.catalogue_category (catalogue_category_id),
    CONSTRAINT fk_cart_status
        FOREIGN KEY (status_id) REFERENCES web_store.cart_status (status_id)
);

-- Create Cart_History Table
CREATE TABLE IF NOT EXISTS web_store.cart_history (
    cart_history_id BIGINT NOT NULL DEFAULT nextval('web_store.seq_cart_history_id') PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    CONSTRAINT fk_cart_history_cart
        FOREIGN KEY (cart_id) REFERENCES web_store.cart (cart_id) ON DELETE CASCADE
);

-- Create Cart_Product_Status Table
CREATE TABLE IF NOT EXISTS web_store.cart_product_status (
    status_id INT NOT NULL DEFAULT nextval('web_store.seq_cart_product_status_id') PRIMARY KEY,
    status_name VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_cart_product_status_name UNIQUE (status_name)
);

-- Create Cart_Product Table
CREATE TABLE IF NOT EXISTS web_store.cart_product (
    cart_product_id BIGINT NOT NULL DEFAULT nextval('web_store.seq_cart_product_id') PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    status_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_product_cart
        FOREIGN KEY (cart_id) REFERENCES web_store.cart (cart_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_product_status
        FOREIGN KEY (status_id) REFERENCES web_store.cart_product_status (status_id)
);

-- Create Cart_Product_History Table
CREATE TABLE IF NOT EXISTS web_store.cart_product_history (
    cart_product_history_id BIGINT NOT NULL DEFAULT nextval('web_store.seq_cart_product_history_id') PRIMARY KEY,
    cart_product_id BIGINT NOT NULL,
    product_id INT NOT NULL,
    old_quantity INT NOT NULL DEFAULT 1,
    new_quantity INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    CONSTRAINT fk_cart_product_history_cart_product
        FOREIGN KEY (cart_product_id) REFERENCES web_store.cart_product (cart_product_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_product_history_product
        FOREIGN KEY (product_id) REFERENCES web_store.product (product_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_cart_phone_number ON web_store.cart(phone_number);
CREATE INDEX idx_cart_catalogue_category ON web_store.cart(catalogue_category_id);
CREATE INDEX idx_cart_status ON web_store.cart(status_id);
CREATE INDEX idx_cart_history_cart ON web_store.cart_history(cart_id);
CREATE INDEX idx_cart_product_cart ON web_store.cart_product(cart_id);
CREATE INDEX idx_cart_product_status ON web_store.cart_product(status_id);
CREATE INDEX idx_cart_product_history_cart_product ON web_store.cart_product_history(cart_product_id);
CREATE INDEX idx_cart_product_history_product ON web_store.cart_product_history(product_id);

-- Insert Cart Status data
INSERT INTO web_store.cart_status (status_name) VALUES
    ('ACTIVE'),
    ('CHECKED_OUT'),
    ('PAID'),
    ('ARCHIVED');

-- Insert Cart Product Status data
INSERT INTO web_store.cart_product_status (status_name) VALUES
    ('ADDED'),
    ('REMOVED');

-- Insert sample Cart data based on existing users and catalogue_categories
-- Only insert if catalogue_category records exist
INSERT INTO web_store.cart (phone_number, catalogue_category_id, status_id)
SELECT
    phone_number,
    catalogue_category_id,
    status_id
FROM (
    SELECT
        '1234567890' as phone_number,
        (SELECT catalogue_category_id FROM web_store.catalogue_category ORDER BY catalogue_category_id LIMIT 1) as catalogue_category_id,
        (SELECT status_id FROM web_store.cart_status WHERE status_name = 'ACTIVE') as status_id
    UNION ALL
    SELECT
        '9876543210',
        (SELECT catalogue_category_id FROM web_store.catalogue_category ORDER BY catalogue_category_id LIMIT 1 OFFSET 1),
        (SELECT status_id FROM web_store.cart_status WHERE status_name = 'ACTIVE')
    UNION ALL
    SELECT
        '9876543210',
        (SELECT catalogue_category_id FROM web_store.catalogue_category ORDER BY catalogue_category_id LIMIT 1 OFFSET 2),
        (SELECT status_id FROM web_store.cart_status WHERE status_name = 'CHECKED_OUT')
) cart_data
WHERE catalogue_category_id IS NOT NULL;

-- Insert Cart History for the created carts
INSERT INTO web_store.cart_history (cart_id, created_by, updated_by)
SELECT
    c.cart_id,
    'ADMIN',
    'ADMIN'
FROM web_store.cart c;

-- Insert sample Cart Products (adding products to carts)
INSERT INTO web_store.cart_product (cart_id, status_id)
VALUES
    (1, 1), -- First cart, ADDED status
    (1, 1), -- First cart, ADDED status (second item)
    (2, 1), -- Second cart, ADDED status
    (2, 1), -- Second cart, ADDED status (second item)
    (3, 2); -- Third cart, REMOVED status

-- Insert Cart Product History for the cart products
-- Adding Fresh Vegetables to cart 1
INSERT INTO web_store.cart_product_history (cart_product_id, product_id, old_quantity, new_quantity, created_by, updated_by)
VALUES
    (1, (SELECT product_id FROM web_store.product WHERE product_name = 'Carrot' LIMIT 1), 0, 2, 'ADMIN', 'ADMIN'),
    (2, (SELECT product_id FROM web_store.product WHERE product_name = 'Spinach' LIMIT 1), 0, 1, 'ADMIN', 'ADMIN');

-- Adding Fresh Fruits to cart 2
INSERT INTO web_store.cart_product_history (cart_product_id, product_id, old_quantity, new_quantity, created_by, updated_by)
VALUES
    (3, (SELECT product_id FROM web_store.product WHERE product_name = 'Apple' LIMIT 1), 0, 3, 'ADMIN', 'ADMIN'),
    (4, (SELECT product_id FROM web_store.product WHERE product_name = 'Banana' LIMIT 1), 0, 5, 'ADMIN', 'ADMIN');

-- Adding and then removing Dairy Products from cart 3
INSERT INTO web_store.cart_product_history (cart_product_id, product_id, old_quantity, new_quantity, created_by, updated_by)
VALUES
    (5, (SELECT product_id FROM web_store.product WHERE product_name = 'Milk' LIMIT 1), 1, 0, 'ADMIN', 'ADMIN');

-- Add additional sample cart products for more comprehensive testing
INSERT INTO web_store.cart_product (cart_id, status_id)
SELECT
    1, -- First cart
    1  -- ADDED status
FROM generate_series(1, 3); -- Add 3 more items to cart 1

-- Get the new cart_product_ids for additional history entries
INSERT INTO web_store.cart_product_history (cart_product_id, product_id, old_quantity, new_quantity, created_by, updated_by)
VALUES
    (6, (SELECT product_id FROM web_store.product WHERE product_name = 'Broccoli' LIMIT 1), 0, 1, 'ADMIN', 'ADMIN'),
    (7, (SELECT product_id FROM web_store.product WHERE product_name = 'Tomato' LIMIT 1), 0, 4, 'ADMIN', 'ADMIN'),
    (8, (SELECT product_id FROM web_store.product WHERE product_name = 'Rice' LIMIT 1), 0, 2, 'ADMIN', 'ADMIN');

-- Add more sample carts for different users with various products
INSERT INTO web_store.cart (phone_number, catalogue_category_id, status_id)
SELECT
    phone_number,
    catalogue_category_id,
    status_id
FROM (
    SELECT
        '5555555555' as phone_number,
        (SELECT catalogue_category_id FROM web_store.catalogue_category ORDER BY catalogue_category_id LIMIT 1) as catalogue_category_id,
        (SELECT status_id FROM web_store.cart_status WHERE status_name = 'ACTIVE') as status_id
    UNION ALL
    SELECT
        '1234567890',
        (SELECT catalogue_category_id FROM web_store.catalogue_category ORDER BY catalogue_category_id LIMIT 1 OFFSET 1),
        (SELECT status_id FROM web_store.cart_status WHERE status_name = 'PAID')
) cart_data
WHERE catalogue_category_id IS NOT NULL;

-- Add history for new carts
INSERT INTO web_store.cart_history (cart_id, created_by, updated_by)
VALUES
    (4, 'ADMIN', 'ADMIN'),
    (5, 'ADMIN', 'ADMIN');

-- Add products to the new carts
INSERT INTO web_store.cart_product (cart_id, status_id)
VALUES
    (4, 1), -- system user cart, ADDED
    (4, 1), -- system user cart, ADDED
    (5, 1), -- admin second cart, ADDED
    (5, 1); -- admin second cart, ADDED

-- Add history for new cart products
INSERT INTO web_store.cart_product_history (cart_product_id, product_id, old_quantity, new_quantity, created_by, updated_by)
VALUES
    (9, (SELECT product_id FROM web_store.product WHERE product_name = 'Chicken Breast' LIMIT 1), 0, 1, 'ADMIN', 'ADMIN'),
    (10, (SELECT product_id FROM web_store.product WHERE product_name = 'Salmon Fillet' LIMIT 1), 0, 2, 'ADMIN', 'ADMIN'),
    (11, (SELECT product_id FROM web_store.product WHERE product_name = 'White Bread' LIMIT 1), 0, 2, 'ADMIN', 'ADMIN'),
    (12, (SELECT product_id FROM web_store.product WHERE product_name = 'Coffee' LIMIT 1), 0, 1, 'ADMIN', 'ADMIN');