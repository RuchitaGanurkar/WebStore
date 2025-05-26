CREATE SCHEMA IF NOT EXISTS web_store;

-- Create Sequences
CREATE SEQUENCE IF NOT EXISTS web_store.seq_catalogue_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_category_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_product_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_product_price_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_currency_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_user_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_catalogue_category_id START WITH 1 INCREMENT BY 1;

-- Ensure currency_name column is large enough
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'web_store'
          AND table_name = 'currency'
          AND column_name = 'currency_name'
          AND character_maximum_length < 50
    ) THEN
        ALTER TABLE web_store.currency
        ALTER COLUMN currency_name TYPE VARCHAR(50);
    END IF;
END $$;

-- Index creation wrapped in DO $$ blocks to ensure idempotency
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_product_category') THEN
        CREATE INDEX idx_product_category ON web_store.product(category_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_product_price_product') THEN
        CREATE INDEX idx_product_price_product ON web_store.product_price(product_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_product_price_currency') THEN
        CREATE INDEX idx_product_price_currency ON web_store.product_price(currency_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_catalogue_category_catalogue') THEN
        CREATE INDEX idx_catalogue_category_catalogue ON web_store.catalogue_category(catalogue_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_catalogue_category_category') THEN
        CREATE INDEX idx_catalogue_category_category ON web_store.catalogue_category(category_id);
    END IF;
END $$;

-- Insert Currency only if not exists
INSERT INTO web_store.currency (currency_code, currency_name, currency_symbol, created_by, updated_by)
SELECT c.*
FROM (VALUES
    ('USD', 'US Dollar', '$', 'system', 'system'),
    ('EUR', 'Euro', '€', 'system', 'system'),
    ('GBP', 'Pound Sterling', '£', 'system', 'system'),
    ('INR', 'Indian Rupee', '₹', 'system', 'system'),
    ('JPY', 'Japanese Yen', '¥', 'system', 'system'),
    ('AUD', 'Australian Dollar', 'A$', 'system', 'system'),
    ('CAD', 'Canadian Dollar', 'C$', 'system', 'system'),
    ('CHF', 'Swiss Franc', 'CHF', 'system', 'system'),
    ('CNY', 'Chinese Yuan', '¥', 'system', 'system'),
    ('AED', 'UAE Dirham', 'د.إ', 'system', 'system')
) AS c(currency_code, currency_name, currency_symbol, created_by, updated_by)
WHERE NOT EXISTS (
    SELECT 1 FROM web_store.currency cu WHERE cu.currency_code = c.currency_code
);

-- Insert Users if not exists
INSERT INTO web_store.users (username, email, full_name, role)
SELECT u.*
FROM (VALUES
    ('admin', 'admin@webstore.com', 'Admin User', 'ADMIN'),
    ('user1', 'user1@example.com', 'Test User', 'USER'),
    ('system', 'system@example.com', 'System User', 'SYSTEM')
) AS u(username, email, full_name, role)
WHERE NOT EXISTS (
    SELECT 1 FROM web_store.users us WHERE us.username = u.username
);

-- Insert Catalogue if not exists
INSERT INTO web_store.catalogue (catalogue_name, catalogue_description, created_by, updated_by)
SELECT 'Default Catalogue', 'Primary catalogue for all active products', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM web_store.catalogue WHERE catalogue_name = 'Default Catalogue'
);

-- Insert Categories
INSERT INTO web_store.category (category_name, category_description, created_by, updated_by)
SELECT c.*
FROM (VALUES
    ('Fresh Vegetables', 'Fresh Vegetables', 'system', 'system'),
    ('Fresh Fruits', 'Fresh Fruits', 'system', 'system'),
    ('Grains & Cereals', 'Grains & Cereals', 'system', 'system'),
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

-- Insert Catalogue-Category Mapping
INSERT INTO web_store.catalogue_category (catalogue_id, category_id, created_by, updated_by)
SELECT 1, c.category_id, 'system', 'system'
FROM web_store.category c
WHERE NOT EXISTS (
    SELECT 1 FROM web_store.catalogue_category cc
    WHERE cc.catalogue_id = 1 AND cc.category_id = c.category_id
);

-- Sample Product Insert (more can be added similarly)
INSERT INTO web_store.product (product_name, product_description, category_id, created_by, updated_by)
SELECT p.*
FROM (VALUES
    -- Fresh Vegetables (category_id = 1)
    ('Carrot', 'Carrot', 1, 'system', 'system'),
    ('Spinach', 'Spinach', 1, 'system', 'system'),
    ('Broccoli', 'Broccoli', 1, 'system', 'system'),
    ('Cauliflower', 'Cauliflower', 1, 'system', 'system'),
    ('Cabbage', 'Cabbage', 1, 'system', 'system'),
    ('Tomato', 'Tomato', 1, 'system', 'system'),
    ('Potato', 'Potato', 1, 'system', 'system'),
    ('Onion', 'Onion', 1, 'system', 'system'),
    ('Garlic', 'Garlic', 1, 'system', 'system'),
    ('Bell Pepper', 'Bell Pepper', 1, 'system', 'system'),
    ('Cucumber', 'Cucumber', 1, 'system', 'system'),
    ('Zucchini', 'Zucchini', 1, 'system', 'system'),
    ('Eggplant', 'Eggplant', 1, 'system', 'system'),
    ('Lettuce', 'Lettuce', 1, 'system', 'system'),
    ('Beetroot', 'Beetroot', 1, 'system', 'system'),
    ('Radish', 'Radish', 1, 'system', 'system'),
    ('Green Beans', 'Green Beans', 1, 'system', 'system'),
    ('Peas', 'Peas', 1, 'system', 'system'),
    ('Sweet Corn', 'Sweet Corn', 1, 'system', 'system'),
    ('Pumpkin', 'Pumpkin', 1, 'system', 'system'),

    -- Fresh Fruits (category_id = 2)
    ('Apple', 'Apple', 2, 'system', 'system'),
    ('Banana', 'Banana', 2, 'system', 'system'),
    ('Orange', 'Orange', 2, 'system', 'system'),
    ('Mango', 'Mango', 2, 'system', 'system'),
    ('Pineapple', 'Pineapple', 2, 'system', 'system'),
    ('Grapes', 'Grapes', 2, 'system', 'system'),
    ('Strawberry', 'Strawberry', 2, 'system', 'system'),
    ('Blueberry', 'Blueberry', 2, 'system', 'system'),
    ('Watermelon', 'Watermelon', 2, 'system', 'system'),
    ('Papaya', 'Papaya', 2, 'system', 'system'),
    ('Kiwi', 'Kiwi', 2, 'system', 'system'),
    ('Pear', 'Pear', 2, 'system', 'system'),
    ('Peach', 'Peach', 2, 'system', 'system'),
    ('Plum', 'Plum', 2, 'system', 'system'),
    ('Cherry', 'Cherry', 2, 'system', 'system'),
    ('Pomegranate', 'Pomegranate', 2, 'system', 'system'),
    ('Guava', 'Guava', 2, 'system', 'system'),
    ('Lychee', 'Lychee', 2, 'system', 'system'),
    ('Apricot', 'Apricot', 2, 'system', 'system'),
    ('Fig', 'Fig', 2, 'system', 'system'),

    -- Grains & Cereals (category_id = 3)
    ('Rice', 'Rice', 3, 'system', 'system'),
    ('Wheat', 'Wheat', 3, 'system', 'system'),
    ('Barley', 'Barley', 3, 'system', 'system'),
    ('Oats', 'Oats', 3, 'system', 'system'),
    ('Corn', 'Corn', 3, 'system', 'system'),
    ('Millet', 'Millet', 3, 'system', 'system'),
    ('Quinoa', 'Quinoa', 3, 'system', 'system'),
    ('Sorghum', 'Sorghum', 3, 'system', 'system'),
    ('Rye', 'Rye', 3, 'system', 'system'),
    ('Buckwheat', 'Buckwheat', 3, 'system', 'system'),
    ('Amaranth', 'Amaranth', 3, 'system', 'system'),
    ('Teff', 'Teff', 3, 'system', 'system'),
    ('Spelt', 'Spelt', 3, 'system', 'system'),
    ('Farro', 'Farro', 3, 'system', 'system'),
    ('Fonio', 'Fonio', 3, 'system', 'system'),
    ('Wild Rice', 'Wild Rice', 3, 'system', 'system'),
    ('Polenta', 'Polenta', 3, 'system', 'system'),
    ('Couscous', 'Couscous', 3, 'system', 'system'),
    ('Semolina', 'Semolina', 3, 'system', 'system'),
    ('Bulgar Wheat', 'Bulgar Wheat', 3, 'system', 'system'),

    -- Pulses & Legumes (category_id = 4)
    ('Lentils', 'Lentils', 4, 'system', 'system'),
    ('Chickpeas', 'Chickpeas', 4, 'system', 'system'),
    ('Black Beans', 'Black Beans', 4, 'system', 'system'),
    ('Kidney Beans', 'Kidney Beans', 4, 'system', 'system'),
    ('Green Gram', 'Green Gram', 4, 'system', 'system'),
    ('Black-eyed Peas', 'Black-eyed Peas', 4, 'system', 'system'),
    ('Pinto Beans', 'Pinto Beans', 4, 'system', 'system'),
    ('Navy Beans', 'Navy Beans', 4, 'system', 'system'),
    ('Soybeans', 'Soybeans', 4, 'system', 'system'),
    ('Mung Beans', 'Mung Beans', 4, 'system', 'system'),
    ('Adzuki Beans', 'Adzuki Beans', 4, 'system', 'system'),
    ('Fava Beans', 'Fava Beans', 4, 'system', 'system'),
    ('Lima Beans', 'Lima Beans', 4, 'system', 'system'),
    ('Split Peas', 'Split Peas', 4, 'system', 'system'),
    ('Horse Gram', 'Horse Gram', 4, 'system', 'system'),
    ('Broad Beans', 'Broad Beans', 4, 'system', 'system'),
    ('Red Lentils', 'Red Lentils', 4, 'system', 'system'),
    ('Yellow Split Peas', 'Yellow Split Peas', 4, 'system', 'system'),
    ('Green Lentils', 'Green Lentils', 4, 'system', 'system'),
    ('White Beans', 'White Beans', 4, 'system', 'system'),

    -- Dairy Products (category_id = 5)
    ('Milk', 'Milk', 5, 'system', 'system'),
    ('Cheddar Cheese', 'Cheddar Cheese', 5, 'system', 'system'),
    ('Mozzarella Cheese', 'Mozzarella Cheese', 5, 'system', 'system'),
    ('Yogurt', 'Yogurt', 5, 'system', 'system'),
    ('Butter', 'Butter', 5, 'system', 'system'),
    ('Cream', 'Cream', 5, 'system', 'system'),
    ('Ghee', 'Ghee', 5, 'system', 'system'),
    ('Paneer', 'Paneer', 5, 'system', 'system'),
    ('Buttermilk', 'Buttermilk', 5, 'system', 'system'),
    ('Condensed Milk', 'Condensed Milk', 5, 'system', 'system'),
    ('Evaporated Milk', 'Evaporated Milk', 5, 'system', 'system'),
    ('Sour Cream', 'Sour Cream', 5, 'system', 'system'),
    ('Cottage Cheese', 'Cottage Cheese', 5, 'system', 'system'),
    ('Ricotta Cheese', 'Ricotta Cheese', 5, 'system', 'system'),
    ('Blue Cheese', 'Blue Cheese', 5, 'system', 'system'),
    ('Parmesan Cheese', 'Parmesan Cheese', 5, 'system', 'system'),
    ('Goat Cheese', 'Goat Cheese', 5, 'system', 'system'),
    ('Ice Cream', 'Ice Cream', 5, 'system', 'system'),
    ('Milk Powder', 'Milk Powder', 5, 'system', 'system'),
    ('Whipped Cream', 'Whipped Cream', 5, 'system', 'system'),

    ('Chicken Breast', 'Chicken Breast', 6, 'system', 'system'),
    ('Chicken Thigh', 'Chicken Thigh', 6, 'system', 'system'),
    ('Whole Chicken', 'Whole Chicken', 6, 'system', 'system'),
    ('Beef Steak', 'Beef Steak', 6, 'system', 'system'),
    ('Ground Beef', 'Ground Beef', 6, 'system', 'system'),
    ('Pork Chops', 'Pork Chops', 6, 'system', 'system'),
    ('Pork Ribs', 'Pork Ribs', 6, 'system', 'system'),
    ('Lamb Chops', 'Lamb Chops', 6, 'system', 'system'),
    ('Lamb Leg', 'Lamb Leg', 6, 'system', 'system'),
    ('Turkey Breast', 'Turkey Breast', 6, 'system', 'system'),
    ('Duck Breast', 'Duck Breast', 6, 'system', 'system'),
    ('Goat Meat', 'Goat Meat', 6, 'system', 'system'),
    ('Veal Cutlets', 'Veal Cutlets', 6, 'system', 'system'),
    ('Bacon', 'Bacon', 6, 'system', 'system'),
    ('Sausages', 'Sausages', 6, 'system', 'system'),
    ('Ham', 'Ham', 6, 'system', 'system'),
    ('Salami', 'Salami', 6, 'system', 'system'),
    ('Meatballs', 'Meatballs', 6, 'system', 'system'),
    ('Minced Meat', 'Minced Meat', 6, 'system', 'system'),
    ('Chicken Wings', 'Chicken Wings', 6, 'system', 'system'),

    -- Seafood (category_id = 7)
    ('Salmon Fillet', 'Salmon Fillet', 7, 'system', 'system'),
    ('Tuna Steak', 'Tuna Steak', 7, 'system', 'system'),
    ('Shrimp', 'Shrimp', 7, 'system', 'system'),
    ('Prawns', 'Prawns', 7, 'system', 'system'),
    ('Crab Meat', 'Crab Meat', 7, 'system', 'system'),
    ('Lobster Tail', 'Lobster Tail', 7, 'system', 'system'),
    ('Clams', 'Clams', 7, 'system', 'system'),
    ('Mussels', 'Mussels', 7, 'system', 'system'),
    ('Oysters', 'Oysters', 7, 'system', 'system'),
    ('Squid', 'Squid', 7, 'system', 'system'),
    ('Octopus', 'Octopus', 7, 'system', 'system'),
    ('Anchovies', 'Anchovies', 7, 'system', 'system'),
    ('Sardines', 'Sardines', 7, 'system', 'system'),
    ('Cod Fillet', 'Cod Fillet', 7, 'system', 'system'),
    ('Haddock', 'Haddock', 7, 'system', 'system'),
    ('Mackerel', 'Mackerel', 7, 'system', 'system'),
    ('Tilapia', 'Tilapia', 7, 'system', 'system'),
    ('Sea Bass', 'Sea Bass', 7, 'system', 'system'),
    ('Catfish', 'Catfish', 7, 'system', 'system'),
    ('Halibut', 'Halibut', 7, 'system', 'system'),

    -- Bakery & Baked Goods (category_id = 8)
    ('White Bread', 'White Bread', 8, 'system', 'system'),
    ('Whole Wheat Bread', 'Whole Wheat Bread', 8, 'system', 'system'),
    ('Multigrain Bread', 'Multigrain Bread', 8, 'system', 'system'),
    ('Baguette', 'Baguette', 8, 'system', 'system'),
    ('Croissant', 'Croissant', 8, 'system', 'system'),
    ('Muffins', 'Muffins', 8, 'system', 'system'),
    ('Donuts', 'Donuts', 8, 'system', 'system'),
    ('Bagels', 'Bagels', 8, 'system', 'system'),
    ('Pita Bread', 'Pita Bread', 8, 'system', 'system'),
    ('Naan', 'Naan', 8, 'system', 'system'),
    ('Tortilla', 'Tortilla', 8, 'system', 'system'),
    ('Sourdough Bread', 'Sourdough Bread', 8, 'system', 'system'),
    ('Brioche', 'Brioche', 8, 'system', 'system'),
    ('Focaccia', 'Focaccia', 8, 'system', 'system'),
    ('Ciabatta', 'Ciabatta', 8, 'system', 'system'),
    ('English Muffins', 'English Muffins', 8, 'system', 'system'),
    ('Cakes', 'Cakes', 8, 'system', 'system'),
    ('Pastries', 'Pastries', 8, 'system', 'system'),
    ('Cookies', 'Cookies', 8, 'system', 'system'),
    ('Brownies', 'Brownies', 8, 'system', 'system'),

    -- Beverages (category_id = 9)
    ('Mineral Water', 'Mineral Water', 9, 'system', 'system'),
    ('Sparkling Water', 'Sparkling Water', 9, 'system', 'system'),
    ('Orange Juice', 'Orange Juice', 9, 'system', 'system'),
    ('Apple Juice', 'Apple Juice', 9, 'system', 'system'),
    ('Grape Juice', 'Grape Juice', 9, 'system', 'system'),
    ('Carrot Juice', 'Carrot Juice', 9, 'system', 'system'),
    ('Tomato Juice', 'Tomato Juice', 9, 'system', 'system'),
    ('Lemonade', 'Lemonade', 9, 'system', 'system'),
    ('Iced Tea', 'Iced Tea', 9, 'system', 'system'),
    ('Green Tea', 'Green Tea', 9, 'system', 'system'),
    ('Black Tea', 'Black Tea', 9, 'system', 'system'),
    ('Coffee', 'Coffee', 9, 'system', 'system'),
    ('Espresso', 'Espresso', 9, 'system', 'system'),
    ('Latte', 'Latte', 9, 'system', 'system'),
    ('Cappuccino', 'Cappuccino', 9, 'system', 'system'),
    ('Hot Chocolate', 'Hot Chocolate', 9, 'system', 'system'),
    ('Milkshake', 'Milkshake', 9, 'system', 'system'),
    ('Smoothies', 'Smoothies', 9, 'system', 'system'),
    ('Energy Drinks', 'Energy Drinks', 9, 'system', 'system'),
    ('Soft Drinks', 'Soft Drinks', 9, 'system', 'system')
) AS p(product_name, product_description, category_id, created_by, updated_by)
WHERE NOT EXISTS (
    SELECT 1 FROM web_store.product pr WHERE pr.product_name = p.product_name
);

INSERT INTO web_store.product_price (product_id, currency_id, price_amount, created_by, updated_by)
VALUES
    -- Fresh Vegetables (IDs: 1–20)
    (1, 1, 199, 'system', 'system'),   -- Carrot ($1.99)
    (2, 1, 149, 'system', 'system'),   -- Spinach ($1.49)
    (3, 1, 299, 'system', 'system'),   -- Broccoli ($2.99)
    (4, 1, 249, 'system', 'system'),   -- Cauliflower ($2.49)
    (5, 1, 179, 'system', 'system'),   -- Cabbage ($1.79)
    (6, 1, 199, 'system', 'system'),   -- Tomato ($1.99)
    (7, 1, 149, 'system', 'system'),   -- Potato ($1.49)
    (8, 1, 129, 'system', 'system'),   -- Onion ($1.29)
    (9, 1, 299, 'system', 'system'),   -- Garlic ($2.99)
    (10, 1, 249, 'system', 'system'),  -- Bell Pepper ($2.49)
    (11, 1, 199, 'system', 'system'),  -- Cucumber ($1.99)
    (12, 1, 299, 'system', 'system'),  -- Zucchini ($2.99)
    (13, 1, 249, 'system', 'system'),  -- Eggplant ($2.49)
    (14, 1, 199, 'system', 'system'),  -- Lettuce ($1.99)
    (15, 1, 299, 'system', 'system'),  -- Beetroot ($2.99)
    (16, 1, 199, 'system', 'system'),  -- Radish ($1.99)
    (17, 1, 299, 'system', 'system'),  -- Green Beans ($2.99)
    (18, 1, 249, 'system', 'system'),  -- Peas ($2.49)
    (19, 1, 199, 'system', 'system'),  -- Sweet Corn ($1.99)
    (20, 1, 399, 'system', 'system'),  -- Pumpkin ($3.99)

    -- Fresh Fruits (IDs: 21–40)
    (21, 1, 299, 'system', 'system'),  -- Apple
    (22, 1, 149, 'system', 'system'),  -- Banana
    (23, 1, 249, 'system', 'system'),  -- Orange
    (24, 1, 399, 'system', 'system'),  -- Mango
    (25, 1, 499, 'system', 'system'),  -- Pineapple
    (26, 1, 349, 'system', 'system'),  -- Grapes
    (27, 1, 599, 'system', 'system'),  -- Strawberry
    (28, 1, 699, 'system', 'system'),  -- Blueberry
    (29, 1, 499, 'system', 'system'),  -- Watermelon
    (30, 1, 399, 'system', 'system'),  -- Papaya
    (31, 1, 599, 'system', 'system'),  -- Kiwi
    (32, 1, 299, 'system', 'system'),  -- Pear
    (33, 1, 299, 'system', 'system'),  -- Peach
    (34, 1, 299, 'system', 'system'),  -- Plum
    (35, 1, 699, 'system', 'system'),  -- Cherry
    (36, 1, 499, 'system', 'system'),  -- Pomegranate
    (37, 1, 299, 'system', 'system'),  -- Guava
    (38, 1, 599, 'system', 'system'),  -- Lychee
    (39, 1, 499, 'system', 'system'),  -- Apricot
    (40, 1, 599, 'system', 'system'),

    ( 41, 1, 299, 'system', 'system' ),
    ( 42, 1, 249, 'system', 'system' ),
    ( 43, 1, 279, 'system', 'system' ),
    ( 44, 1, 299, 'system', 'system' ),
    ( 45, 1, 229, 'system', 'system' ),
    ( 46, 1, 259, 'system', 'system' ),
    ( 47, 1, 499, 'system', 'system' ),
    ( 48, 1, 269, 'system', 'system' ),
    ( 49, 1, 289, 'system', 'system' ),
    ( 50, 1, 399, 'system', 'system' ),
    ( 51, 1, 459, 'system', 'system' ),
    ( 52, 1, 499, 'system', 'system' ),
    ( 53, 1, 359, 'system', 'system' ),
    ( 54, 1, 429, 'system', 'system' ),
    ( 55, 1, 599, 'system', 'system' ),
    ( 56, 1, 699, 'system', 'system' ),
    ( 57, 1, 349, 'system', 'system' ),
    ( 58, 1, 329, 'system', 'system' ),
    ( 59, 1, 289, 'system', 'system' ),
    ( 60, 1, 319, 'system', 'system' ),
    ( 61, 1, 249, 'system', 'system' ),
    ( 62, 1, 239, 'system', 'system' ),
    ( 63, 1, 259, 'system', 'system' ),
    ( 64, 1, 279, 'system', 'system' ),
    ( 65, 1, 269, 'system', 'system' ),
    ( 66, 1, 249, 'system', 'system' ),
    ( 67, 1, 259, 'system', 'system' ),
    ( 68, 1, 269, 'system', 'system' ),
    ( 69, 1, 289, 'system', 'system' ),
    ( 70, 1, 279, 'system', 'system' ),
    ( 71, 1, 299, 'system', 'system' ),
    ( 72, 1, 319, 'system', 'system' ),
    ( 73, 1, 289, 'system', 'system' ),
    ( 74, 1, 259, 'system', 'system' ),
    ( 75, 1, 309, 'system', 'system' ),
    ( 76, 1, 339, 'system', 'system' ),
    ( 77, 1, 299, 'system', 'system' ),
    ( 78, 1, 279, 'system', 'system' ),
    ( 79, 1, 289, 'system', 'system' ),
    ( 80, 1, 269, 'system', 'system' ),
    ( 81, 1, 199, 'system', 'system' ),
    ( 82, 1, 399, 'system', 'system' ),
    ( 83, 1, 429, 'system', 'system' ),
    ( 84, 1, 249, 'system', 'system' ),
    ( 85, 1, 349, 'system', 'system' ),
    ( 86, 1, 399, 'system', 'system' ),
    ( 87, 1, 599, 'system', 'system' ),
    ( 88, 1, 379, 'system', 'system' ),
    ( 89, 1, 229, 'system', 'system' ),
    ( 90, 1, 299, 'system', 'system' ),
    ( 91, 1, 329, 'system', 'system' ),
    ( 92, 1, 249, 'system', 'system' ),
    ( 93, 1, 359, 'system', 'system' ),
    ( 94, 1, 379, 'system', 'system' ),
    ( 95, 1, 499, 'system', 'system' ),
    ( 96, 1, 549, 'system', 'system' ),
    ( 97, 1, 499, 'system', 'system' ),
    ( 98, 1, 399, 'system', 'system' ),
    ( 99, 1, 449, 'system', 'system' ),
    ( 100, 1, 299, 'system', 'system' ),
    ( 101, 1, 599, 'system', 'system' ),
    ( 102, 1, 499, 'system', 'system' ),
    ( 103, 1, 899, 'system', 'system' ),
    ( 104, 1, 1399, 'system', 'system' ),
    ( 105, 1, 1099, 'system', 'system' ),
    ( 106, 1, 999, 'system', 'system' ),
    ( 107, 1, 1199, 'system', 'system' ),
    ( 108, 1, 1499, 'system', 'system' ),
    ( 109, 1, 1399, 'system', 'system' ),
    ( 110, 1, 1099, 'system', 'system' ),
    ( 111, 1, 1299, 'system', 'system' ),
    ( 112, 1, 1499, 'system', 'system' ),
    ( 113, 1, 1599, 'system', 'system' ),
    ( 114, 1, 899, 'system', 'system' ),
    ( 115, 1, 699, 'system', 'system' ),
    ( 116, 1, 899, 'system', 'system' ),
    ( 117, 1, 999, 'system', 'system' ),
    ( 118, 1, 699, 'system', 'system' ),
    ( 119, 1, 899, 'system', 'system' ),
    ( 120, 1, 599, 'system', 'system' ),
    ( 121, 1, 1499, 'system', 'system' ),
    ( 122, 1, 1399, 'system', 'system' ),
    ( 123, 1, 1099, 'system', 'system' ),
    ( 124, 1, 1199, 'system', 'system' ),
    ( 125, 1, 1599, 'system', 'system' ),
    ( 126, 1, 1999, 'system', 'system' ),
    ( 127, 1, 899, 'system', 'system' ),
    ( 128, 1, 799, 'system', 'system' ),
    ( 129, 1, 999, 'system', 'system' ),
    ( 130, 1, 899, 'system', 'system' ),
    ( 131, 1, 1199, 'system', 'system' ),
    ( 132, 1, 499, 'system', 'system' ),
    ( 133, 1, 599, 'system', 'system' ),
    ( 134, 1, 1399, 'system', 'system' ),
    ( 135, 1, 1299, 'system', 'system' ),
    ( 136, 1, 999, 'system', 'system' ),
    ( 137, 1, 899, 'system', 'system' ),
    ( 138, 1, 1499, 'system', 'system' ),
    ( 139, 1, 1099, 'system', 'system' ),
    ( 140, 1, 1799, 'system', 'system' ),
    ( 141, 1, 199, 'system', 'system' ),
    ( 142, 1, 229, 'system', 'system' ),
    ( 143, 1, 249, 'system', 'system' ),
    ( 144, 1, 299, 'system', 'system' ),
    ( 145, 1, 199, 'system', 'system' ),
    ( 146, 1, 249, 'system', 'system' ),
    ( 147, 1, 149, 'system', 'system' ),
    ( 148, 1, 199, 'system', 'system' ),
    ( 149, 1, 229, 'system', 'system' ),
    ( 150, 1, 199, 'system', 'system' ),
    ( 151, 1, 189, 'system', 'system' ),
    ( 152, 1, 279, 'system', 'system' ),
    ( 153, 1, 299, 'system', 'system' ),
    ( 154, 1, 319, 'system', 'system' ),
    ( 155, 1, 289, 'system', 'system' ),
    ( 156, 1, 199, 'system', 'system' ),
    ( 157, 1, 599, 'system', 'system' ),
    ( 158, 1, 399, 'system', 'system' ),
    ( 159, 1, 299, 'system', 'system' ),
    ( 160, 1, 349, 'system', 'system' ),
    ( 161, 1, 99, 'system', 'system' ),
    ( 162, 1, 129, 'system', 'system' ),
    ( 163, 1, 199, 'system', 'system' ),
    ( 164, 1, 199, 'system', 'system' ),
    ( 165, 1, 229, 'system', 'system' ),
    ( 166, 1, 199, 'system', 'system' ),
    ( 167, 1, 189, 'system', 'system' ),
    ( 168, 1, 149, 'system', 'system' ),
    ( 169, 1, 199, 'system', 'system' ),
    ( 170, 1, 249, 'system', 'system' ),
    ( 171, 1, 229, 'system', 'system' ),
    ( 172, 1, 299, 'system', 'system' ),
    ( 173, 1, 349, 'system', 'system' ),
    ( 174, 1, 399, 'system', 'system' ),
    ( 175, 1, 399, 'system', 'system' ),
    ( 176, 1, 299, 'system', 'system' ),
    ( 177, 1, 349, 'system', 'system' ),
    ( 178, 1, 399, 'system', 'system' ),
    ( 179, 1, 299, 'system', 'system' ),
    ( 180, 1, 199, 'system', 'system' );
