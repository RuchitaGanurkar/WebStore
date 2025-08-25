-- V4__WebStore_Order_CreateScript.sql
-- This migration creates order-related tables and fixes existing table relationships
-- Follows the pattern of other status tables with ID-based approach

-- Create Sequences for Order tables
CREATE SEQUENCE IF NOT EXISTS web_store.seq_order_status_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_order_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_order_history_id START WITH 1 INCREMENT BY 1;

-- FIX: Add missing cart_id foreign key reference in cart_product table
-- This should have been there originally but was missing
ALTER TABLE web_store.cart_product
ADD CONSTRAINT fk_cart_product_cart_missing
    FOREIGN KEY (cart_id) REFERENCES web_store.cart (cart_id) ON DELETE CASCADE;

-- Create Order_Status Table (following the pattern of other status tables with ID + name)
CREATE TABLE IF NOT EXISTS web_store.order_status (
    status_id INT NOT NULL DEFAULT nextval('web_store.seq_order_status_id') PRIMARY KEY,
    status_name VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_order_status_name UNIQUE (status_name)
);

-- Create Orders Table
CREATE TABLE IF NOT EXISTS web_store.orders (
    order_id BIGINT NOT NULL DEFAULT nextval('web_store.seq_order_id') PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    status_id INT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    CONSTRAINT fk_order_cart
        FOREIGN KEY (cart_id) REFERENCES web_store.cart (cart_id),
    CONSTRAINT fk_order_status
        FOREIGN KEY (status_id) REFERENCES web_store.order_status (status_id)
);

-- Create Order_History Table
CREATE TABLE IF NOT EXISTS web_store.order_history (
    order_history_id BIGINT NOT NULL DEFAULT nextval('web_store.seq_order_history_id') PRIMARY KEY,
    order_id BIGINT NOT NULL,
    old_status_id INT,
    new_status_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    CONSTRAINT fk_order_history_order
        FOREIGN KEY (order_id) REFERENCES web_store.orders (order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_history_old_status
        FOREIGN KEY (old_status_id) REFERENCES web_store.order_status (status_id),
    CONSTRAINT fk_order_history_new_status
        FOREIGN KEY (new_status_id) REFERENCES web_store.order_status (status_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_orders_cart ON web_store.orders(cart_id);
CREATE INDEX idx_orders_status ON web_store.orders(status_id);
CREATE INDEX idx_orders_created_at ON web_store.orders(created_at);
CREATE INDEX idx_order_history_order ON web_store.order_history(order_id);
CREATE INDEX idx_order_history_old_status ON web_store.order_history(old_status_id);
CREATE INDEX idx_order_history_new_status ON web_store.order_history(new_status_id);
CREATE INDEX idx_order_history_created_at ON web_store.order_history(created_at);

-- Insert Order Status master data (following enum pattern like cart_status and cart_product_status)
INSERT INTO web_store.order_status (status_name, description) VALUES
    ('PENDING', 'Order is pending confirmation'),
    ('CONFIRMED', 'Order has been confirmed'),
    ('SHIPPED', 'Order has been shipped'),
    ('DELIVERED', 'Order has been delivered'),
    ('CANCELLED', 'Order has been cancelled');

-- Insert sample Orders data based on existing carts
-- Calculate total_amount from cart products and their prices
INSERT INTO web_store.orders (cart_id, status_id, total_amount, created_by, updated_by)
SELECT
    c.cart_id,
    (SELECT status_id FROM web_store.order_status WHERE status_name = 'PENDING') as status_id,
    COALESCE(SUM(cp.quantity * pp.price_amount / 100.0), 0) as total_amount, -- Convert cents to dollars
    'system' as created_by,
    'system' as updated_by
FROM web_store.cart c
LEFT JOIN web_store.cart_product cp ON c.cart_id = cp.cart_id AND cp.status_id = 1 -- ADDED status
LEFT JOIN web_store.product_price pp ON cp.product_id = pp.product_id AND pp.currency_id = 1 -- USD
WHERE c.status_id IN (
    SELECT status_id FROM web_store.cart_status WHERE status_name IN ('CHECKED_OUT', 'PAID')
)
GROUP BY c.cart_id;

-- Insert Order History for the created orders (initial status entry)
INSERT INTO web_store.order_history (order_id, old_status_id, new_status_id, created_by, updated_by)
SELECT
    o.order_id,
    NULL as old_status_id, -- Initial status has no previous status
    o.status_id as new_status_id,
    'system' as created_by,
    'system' as updated_by
FROM web_store.orders o;

-- Update some orders to different statuses with history tracking
-- Update order 1 to CONFIRMED
UPDATE web_store.orders
SET status_id = (SELECT status_id FROM web_store.order_status WHERE status_name = 'CONFIRMED'),
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system'
WHERE order_id = 1;

INSERT INTO web_store.order_history (order_id, old_status_id, new_status_id, created_by, updated_by)
VALUES (1,
    (SELECT status_id FROM web_store.order_status WHERE status_name = 'PENDING'),
    (SELECT status_id FROM web_store.order_status WHERE status_name = 'CONFIRMED'),
    'system', 'system');

-- Update order 2 to SHIPPED (if exists)
UPDATE web_store.orders
SET status_id = (SELECT status_id FROM web_store.order_status WHERE status_name = 'SHIPPED'),
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system'
WHERE order_id = 2;

INSERT INTO web_store.order_history (order_id, old_status_id, new_status_id, created_by, updated_by)
SELECT 2,
    (SELECT status_id FROM web_store.order_status WHERE status_name = 'PENDING'),
    (SELECT status_id FROM web_store.order_status WHERE status_name = 'CONFIRMED'),
    'system', 'system'
WHERE EXISTS (SELECT 1 FROM web_store.orders WHERE order_id = 2)
UNION ALL
SELECT 2,
    (SELECT status_id FROM web_store.order_status WHERE status_name = 'CONFIRMED'),
    (SELECT status_id FROM web_store.order_status WHERE status_name = 'SHIPPED'),
    'system', 'system'
WHERE EXISTS (SELECT 1 FROM web_store.orders WHERE order_id = 2);

-- Add some additional sample orders for demonstration
INSERT INTO web_store.orders (cart_id, status_id, total_amount, created_by, updated_by)
SELECT * FROM (VALUES
    (1, (SELECT status_id FROM web_store.order_status WHERE status_name = 'DELIVERED'), 15.96, 'system', 'system'),
    (4, (SELECT status_id FROM web_store.order_status WHERE status_name = 'CANCELLED'), 58.97, 'system', 'system')
) AS new_orders(cart_id, status_id, total_amount, created_by, updated_by)
WHERE EXISTS (SELECT 1 FROM web_store.cart WHERE cart_id = new_orders.cart_id);

-- Add history for the additional orders
INSERT INTO web_store.order_history (order_id, old_status_id, new_status_id, created_by, updated_by)
SELECT
    o.order_id,
    NULL as old_status_id,
    o.status_id as new_status_id,
    'system' as created_by,
    'system' as updated_by
FROM web_store.orders o
WHERE o.order_id > 2; -- Only for the newly added orders

-- Add complete lifecycle history for DELIVERED order (if order_id 3 exists)
INSERT INTO web_store.order_history (order_id, old_status_id, new_status_id, created_by, updated_by)
SELECT * FROM (VALUES
    (3, (SELECT status_id FROM web_store.order_status WHERE status_name = 'PENDING'), (SELECT status_id FROM web_store.order_status WHERE status_name = 'CONFIRMED'), 'system', 'system'),
    (3, (SELECT status_id FROM web_store.order_status WHERE status_name = 'CONFIRMED'), (SELECT status_id FROM web_store.order_status WHERE status_name = 'SHIPPED'), 'system', 'system'),
    (3, (SELECT status_id FROM web_store.order_status WHERE status_name = 'SHIPPED'), (SELECT status_id FROM web_store.order_status WHERE status_name = 'DELIVERED'), 'system', 'system')
) AS history_data(order_id, old_status_id, new_status_id, created_by, updated_by)
WHERE EXISTS (SELECT 1 FROM web_store.orders WHERE order_id = 3 AND status_id = (SELECT status_id FROM web_store.order_status WHERE status_name = 'DELIVERED'));

-- Add history for CANCELLED order (if order_id 4 exists)
INSERT INTO web_store.order_history (order_id, old_status_id, new_status_id, created_by, updated_by)
SELECT 4,
    (SELECT status_id FROM web_store.order_status WHERE status_name = 'PENDING'),
    (SELECT status_id FROM web_store.order_status WHERE status_name = 'CANCELLED'),
    'system', 'system'
WHERE EXISTS (SELECT 1 FROM web_store.orders WHERE order_id = 4 AND status_id = (SELECT status_id FROM web_store.order_status WHERE status_name = 'CANCELLED'));