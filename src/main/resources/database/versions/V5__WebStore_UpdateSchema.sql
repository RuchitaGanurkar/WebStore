-- V5__WebStore_UpdateSchema.sql
-- Add old_status_id and new_status_id to cart_history
-- Add product_price_id to cart_product_history

-- 1. Alter Cart_History: introduce old_status_id and new_status_id
ALTER TABLE web_store.cart_history
ADD COLUMN old_status_id INT,
ADD COLUMN new_status_id INT;

-- Add foreign key constraints to cart_status
ALTER TABLE web_store.cart_history
ADD CONSTRAINT fk_cart_history_old_status
    FOREIGN KEY (old_status_id) REFERENCES web_store.cart_status(status_id),
ADD CONSTRAINT fk_cart_history_new_status
    FOREIGN KEY (new_status_id) REFERENCES web_store.cart_status(status_id);

-- 2. Alter Cart_Product_History: introduce product_price_id
ALTER TABLE web_store.cart_product_history
ADD COLUMN product_price_id INT;

-- Add foreign key constraint to product_price
ALTER TABLE web_store.cart_product_history
ADD CONSTRAINT fk_cart_product_history_price
    FOREIGN KEY (product_price_id) REFERENCES web_store.product_price(product_price_id);

-- 3. Create indexes for performance
CREATE INDEX idx_cart_history_old_status ON web_store.cart_history(old_status_id);
CREATE INDEX idx_cart_history_new_status ON web_store.cart_history(new_status_id);
CREATE INDEX idx_cart_product_history_price ON web_store.cart_product_history(product_price_id);

-- 4. Optional: backfill sample data for existing records (set new_status as current cart.status_id, old_status NULL)
UPDATE web_store.cart_history ch
SET new_status_id = c.status_id
FROM web_store.cart c
WHERE ch.cart_id = c.cart_id
  AND ch.new_status_id IS NULL;

-- Backfill product_price_id for existing cart_product_history using current price
UPDATE web_store.cart_product_history cph
SET product_price_id = (
    SELECT pp.product_price_id
    FROM web_store.product_price pp
    WHERE pp.product_id = cph.product_id
    ORDER BY pp.created_at DESC
    LIMIT 1
)
WHERE cph.product_price_id IS NULL;
