-- V2__WebStore_UpdateSchema.sql
-- This migration adds agricultural products and updates existing data
-- Removes all duplicates from V1

-- Add new currencies (only ones not in V1)
INSERT INTO web_store.currency (currency_code, currency_name, currency_symbol, created_by, updated_by)
SELECT c.*
FROM (VALUES
    ('INR', 'Rupee', '₹', 'system', 'system'),
    ('JPY', 'Yen', '¥', 'system', 'system'),
    ('AUD', 'Australian', 'A$', 'system', 'system'),
    ('CAD', 'Canadian', 'C$', 'system', 'system'),
    ('CHF', 'Swiss', 'CHF', 'system', 'system'),
    ('CNY', 'Yuan', '¥', 'system', 'system'),
    ('AED', 'Dirham', 'د.إ', 'system', 'system')
) AS c(currency_code, currency_name, currency_symbol, created_by, updated_by)
WHERE NOT EXISTS (
    SELECT 1 FROM web_store.currency cu WHERE cu.currency_code = c.currency_code
);

-- Add agricultural categories (replace existing sample categories)
-- First, remove existing products and their prices to avoid foreign key conflicts
DELETE FROM web_store.product_price WHERE product_id IN (
    SELECT product_id FROM web_store.product WHERE category_id IN (
        SELECT category_id FROM web_store.category WHERE category_name IN ('Electronics', 'Clothing', 'Home')
    )
);
DELETE FROM web_store.product WHERE category_id IN (
    SELECT category_id FROM web_store.category WHERE category_name IN ('Electronics', 'Clothing', 'Home')
);

-- Remove old catalogue-category mappings
DELETE FROM web_store.catalogue_category WHERE category_id IN (
    SELECT category_id FROM web_store.category WHERE category_name IN ('Electronics', 'Clothing', 'Home')
);

-- Update existing categories to agricultural ones
UPDATE web_store.category SET
    category_name = 'Fresh Vegetables',
    category_description = 'Fresh Vegetables',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system'
WHERE category_name = 'Electronics';

UPDATE web_store.category SET
    category_name = 'Fresh Fruits',
    category_description = 'Fresh Fruits',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system'
WHERE category_name = 'Clothing';

UPDATE web_store.category SET
    category_name = 'Grains & Cereals',
    category_description = 'Grains & Cereals',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system'
WHERE category_name = 'Home';

-- Add additional agricultural categories
INSERT INTO web_store.category (category_name, category_description, created_by, updated_by)
SELECT c.*
FROM (VALUES
    ('Pulses & Legumes', 'Pulses & Legumes', 'system', 'system'),
    ('Dairy Products', 'Dairy Products', 'system', 'system'),
    ('Meat & Poultry','Meat & Poultry','system', 'system'),
    ('Seafood','Seafood','system', 'system'),
    ('Bakery & Baked Goods','Bakery & Baked Goods','system', 'system'),
    ('Beverages','Beverages','system', 'system')
) AS c(category_name, category_description, created_by, updated_by)
WHERE NOT EXISTS (
    SELECT 1 FROM web_store.category cat WHERE cat.category_name = c.category_name
);

-- Link all categories to the main catalogue
INSERT INTO web_store.catalogue_category (catalogue_id, category_id, created_by, updated_by)
SELECT 1, c.category_id, 'system', 'system'
FROM web_store.category c
WHERE NOT EXISTS (
    SELECT 1 FROM web_store.catalogue_category cc
    WHERE cc.catalogue_id = 1 AND cc.category_id = c.category_id
);

-- Add agricultural products
INSERT INTO web_store.product (product_name, product_description, category_id, created_by, updated_by)
SELECT p.*
FROM (VALUES
    -- Fresh Vegetables (category_id will be resolved by name)
    ('Carrot', 'Fresh Carrot', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Vegetables'), 'system', 'system'),
    ('Spinach', 'Fresh Spinach', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Vegetables'), 'system', 'system'),
    ('Broccoli', 'Fresh Broccoli', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Vegetables'), 'system', 'system'),
    ('Cauliflower', 'Fresh Cauliflower', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Vegetables'), 'system', 'system'),
    ('Cabbage', 'Fresh Cabbage', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Vegetables'), 'system', 'system'),
    ('Tomato', 'Fresh Tomato', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Vegetables'), 'system', 'system'),
    ('Potato', 'Fresh Potato', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Vegetables'), 'system', 'system'),
    ('Onion', 'Fresh Onion', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Vegetables'), 'system', 'system'),
    ('Garlic', 'Fresh Garlic', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Vegetables'), 'system', 'system'),
    ('Bell Pepper', 'Fresh Bell Pepper', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Vegetables'), 'system', 'system'),
    ('Cucumber', 'Fresh Cucumber', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Vegetables'), 'system', 'system'),
    ('Zucchini', 'Fresh Zucchini', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Vegetables'), 'system', 'system'),
    ('Eggplant', 'Fresh Eggplant', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Vegetables'), 'system', 'system'),
    ('Lettuce', 'Fresh Lettuce', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Vegetables'), 'system', 'system'),
    ('Beetroot', 'Fresh Beetroot', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Vegetables'), 'system', 'system'),

    -- Fresh Fruits
    ('Apple', 'Fresh Apple', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),
    ('Banana', 'Fresh Banana', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),
    ('Orange', 'Fresh Orange', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),
    ('Mango', 'Fresh Mango', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),
    ('Pineapple', 'Fresh Pineapple', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),
    ('Grapes', 'Fresh Grapes', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),
    ('Strawberry', 'Fresh Strawberry', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),
    ('Watermelon', 'Fresh Watermelon', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),
    ('Papaya', 'Fresh Papaya', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),
    ('Kiwi', 'Fresh Kiwi', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),

    -- Grains & Cereals
    ('Rice', 'Premium Rice', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
    ('Wheat', 'Organic Wheat', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
    ('Barley', 'Pearl Barley', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
    ('Oats', 'Rolled Oats', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
    ('Quinoa', 'Organic Quinoa', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),

    -- Pulses & Legumes
    ('Lentils', 'Red Lentils', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
    ('Chickpeas', 'Dried Chickpeas', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
    ('Black Beans', 'Organic Black Beans', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
    ('Kidney Beans', 'Red Kidney Beans', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
    ('Green Gram', 'Moong Dal', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),

    -- Dairy Products
    ('Milk', 'Fresh Milk 1L', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
    ('Cheddar Cheese', 'Aged Cheddar Cheese', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
    ('Yogurt', 'Greek Yogurt', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
    ('Butter', 'Salted Butter', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
    ('Paneer', 'Fresh Paneer', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),

    -- Meat & Poultry
    ('Chicken Breast', 'Fresh Chicken Breast', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'), 'system', 'system'),
    ('Ground Beef', 'Lean Ground Beef', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'), 'system', 'system'),
    ('Lamb Chops', 'Fresh Lamb Chops', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'), 'system', 'system'),

    -- Seafood
    ('Salmon Fillet', 'Atlantic Salmon Fillet', (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
    ('Shrimp', 'Large Shrimp', (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
    ('Tuna Steak', 'Fresh Tuna Steak', (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),

    -- Bakery & Baked Goods
    ('White Bread', 'Fresh White Bread', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
    ('Croissant', 'Butter Croissant', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
    ('Bagels', 'Everything Bagels', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),

    -- Beverages
    ('Orange Juice', 'Fresh Orange Juice', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Green Tea', 'Organic Green Tea', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Coffee', 'Premium Coffee Beans', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system')
) AS p(product_name, product_description, category_id, created_by, updated_by)
WHERE NOT EXISTS (
    SELECT 1 FROM web_store.product pr WHERE pr.product_name = p.product_name
);

-- Add prices for the new products (USD prices in cents)
INSERT INTO web_store.product_price (product_id, currency_id, price_amount, created_by, updated_by)
SELECT p.product_id, 1, -- USD currency_id = 1
CASE p.product_name
    -- Vegetables (prices in cents)
    WHEN 'Carrot' THEN 199
    WHEN 'Spinach' THEN 249
    WHEN 'Broccoli' THEN 299
    WHEN 'Cauliflower' THEN 349
    WHEN 'Cabbage' THEN 179
    WHEN 'Tomato' THEN 299
    WHEN 'Potato' THEN 149
    WHEN 'Onion' THEN 129
    WHEN 'Garlic' THEN 399
    WHEN 'Bell Pepper' THEN 249
    WHEN 'Cucumber' THEN 199
    WHEN 'Zucchini' THEN 299
    WHEN 'Eggplant' THEN 279
    WHEN 'Lettuce' THEN 199
    WHEN 'Beetroot' THEN 229
    -- Fruits
    WHEN 'Apple' THEN 299
    WHEN 'Banana' THEN 149
    WHEN 'Orange' THEN 249
    WHEN 'Mango' THEN 399
    WHEN 'Pineapple' THEN 499
    WHEN 'Grapes' THEN 349
    WHEN 'Strawberry' THEN 599
    WHEN 'Watermelon' THEN 399
    WHEN 'Papaya' THEN 299
    WHEN 'Kiwi' THEN 599
    -- Grains
    WHEN 'Rice' THEN 299
    WHEN 'Wheat' THEN 249
    WHEN 'Barley' THEN 279
    WHEN 'Oats' THEN 399
    WHEN 'Quinoa' THEN 699
    -- Pulses
    WHEN 'Lentils' THEN 249
    WHEN 'Chickpeas' THEN 279
    WHEN 'Black Beans' THEN 329
    WHEN 'Kidney Beans' THEN 299
    WHEN 'Green Gram' THEN 259
    -- Dairy
    WHEN 'Milk' THEN 199
    WHEN 'Cheddar Cheese' THEN 599
    WHEN 'Yogurt' THEN 249
    WHEN 'Butter' THEN 449
    WHEN 'Paneer' THEN 699
    -- Meat
    WHEN 'Chicken Breast' THEN 899
    WHEN 'Ground Beef' THEN 799
    WHEN 'Lamb Chops' THEN 1499
    -- Seafood
    WHEN 'Salmon Fillet' THEN 1999
    WHEN 'Shrimp' THEN 1299
    WHEN 'Tuna Steak' THEN 1799
    -- Bakery
    WHEN 'White Bread' THEN 299
    WHEN 'Croissant' THEN 199
    WHEN 'Bagels' THEN 349
    -- Beverages
    WHEN 'Orange Juice' THEN 399
    WHEN 'Green Tea' THEN 599
    WHEN 'Coffee' THEN 899
    ELSE 199
END,
'system', 'system'
FROM web_store.product p
WHERE NOT EXISTS (
    SELECT 1 FROM web_store.product_price pp
    WHERE pp.product_id = p.product_id AND pp.currency_id = 1
);