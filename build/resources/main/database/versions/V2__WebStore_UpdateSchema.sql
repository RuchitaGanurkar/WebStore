-- V2__WebStore_UpdateSchema.sql (Corrected Version)
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
    ('Meat & Poultry', 'Meat & Poultry', 'system', 'system'),
    ('Seafood', 'Seafood', 'system', 'system'),
    ('Bakery & Baked Goods', 'Bakery & Baked Goods', 'system', 'system'),
    ('Beverages', 'Beverages', 'system', 'system')
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
    ('Pear', 'Pear', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),
            ('Peach', 'Peach', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),
            ('Plum', 'Plum', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),
            ('Cherry', 'Cherry', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),
            ('Pomegranate', 'Pomegranate', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),
            ('Guava', 'Guava', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),
            ('Lychee', 'Lychee', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),
            ('Apricot', 'Apricot', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),
            ('Fig', 'Fig', (SELECT category_id FROM web_store.category WHERE category_name = 'Fresh Fruits'), 'system', 'system'),


    -- Grains & Cereals
    ('Rice', 'Premium Rice', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),

     ('Wheat', 'Organic Wheat',(SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Barley', 'Pearl Barley', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Oats', 'Rolled Oats', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Quinoa', 'Organic Quinoa', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Corn', 'Corn', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Millet', 'Millet', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Sorghum', 'Sorghum', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Rye', 'Rye', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Buckwheat', 'Buckwheat', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Amaranth', 'Amaranth', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Teff', 'Teff', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Spelt', 'Spelt', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Farro', 'Farro', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Fonio', 'Fonio', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Wild Rice', 'Wild Rice', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Polenta', 'Polenta', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Couscous', 'Couscous', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Semolina', 'Semolina', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),
            ('Bulgar Wheat', 'Bulgar Wheat', (SELECT category_id FROM web_store.category WHERE category_name = 'Grains & Cereals'), 'system', 'system'),

    -- Pulses & Legumes
    ('Lentils', 'Red Lentils', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
    ('Chickpeas', 'Dried Chickpeas', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
    ('Black Beans', 'Organic Black Beans', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
    ('Kidney Beans', 'Red Kidney Beans', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
    ('Green Gram', 'Moong Dal', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),

            ('Black-eyed Peas', 'Black-eyed Peas', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
            ('Pinto Beans', 'Pinto Beans', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
            ('Navy Beans', 'Navy Beans',(SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
            ('Soybeans', 'Soybeans', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
            ('Mung Beans', 'Mung Beans',(SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
            ('Adzuki Beans', 'Adzuki Beans',(SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
            ('Fava Beans', 'Fava Beans', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
            ('Lima Beans', 'Lima Beans', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
            ('Split Peas', 'Split Peas', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
            ('Horse Gram', 'Horse Gram', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
            ('Broad Beans', 'Broad Beans', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
            ('Red Lentils', 'Red Lentils',(SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
            ('Yellow Split Peas', 'Yellow Split Peas', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
            ('Green Lentils', 'Green Lentils', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),
            ('White Beans', 'White Beans', (SELECT category_id FROM web_store.category WHERE category_name = 'Pulses & Legumes'), 'system', 'system'),

    -- Dairy Products
    ('Milk', 'Fresh Milk 1L', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
    ('Cheddar Cheese', 'Aged Cheddar Cheese', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
    ('Yogurt', 'Greek Yogurt', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
    ('Butter', 'Salted Butter', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
    ('Paneer', 'Fresh Paneer', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),

            ('Ghee', 'Ghee', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
            ('Mozzarella Cheese', 'Mozzarella Cheese', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
            ('Buttermilk', 'Buttermilk', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
            ('Condensed Milk', 'Condensed Milk', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
            ('Evaporated Milk', 'Evaporated Milk', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
            ('Sour Cream', 'Sour Cream', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
            ('Cottage Cheese', 'Cottage Cheese', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
            ('Ricotta Cheese', 'Ricotta Cheese', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
            ('Blue Cheese', 'Blue Cheese', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
            ('Parmesan Cheese', 'Parmesan Cheese', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
            ('Goat Cheese', 'Goat Cheese', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
            ('Ice Cream', 'Ice Cream', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'), 'system', 'system'),
            ('Milk Powder', 'Milk Powder', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'),'system', 'system'),
            ('Whipped Cream', 'Whipped Cream', (SELECT category_id FROM web_store.category WHERE category_name = 'Dairy Products'),'system', 'system'),


    -- Meat & Poultry
    ('Chicken Breast', 'Fresh Chicken Breast', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'), 'system', 'system'),
    ('Ground Beef', 'Lean Ground Beef', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'), 'system', 'system'),
    ('Lamb Chops', 'Fresh Lamb Chops', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'), 'system', 'system'),



        ('Chicken Thigh', 'Chicken Thigh', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'),'system', 'system'),
        ('Whole Chicken', 'Whole Chicken', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'), 'system', 'system'),
        ('Beef Steak', 'Beef Steak', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'), 'system', 'system'),
        ('Pork Chops', 'Pork Chops', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'), 'system', 'system'),
        ('Pork Ribs', 'Pork Ribs', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'),  'system', 'system'),
        ('Lamb Leg', 'Lamb Leg', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'),  'system', 'system'),
        ('Turkey Breast', 'Turkey Breast', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'),  'system', 'system'),
        ('Duck Breast', 'Duck Breast', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'), 'system', 'system'),
        ('Goat Meat', 'Goat Meat', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'),  'system', 'system'),
        ('Veal Cutlets', 'Veal Cutlets', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'), 'system', 'system'),
        ('Bacon', 'Bacon', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'),  'system', 'system'),
        ('Sausages', 'Sausages', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'), 'system', 'system'),
        ('Ham', 'Ham', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'),  'system', 'system'),
        ('Salami', 'Salami', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'),  'system', 'system'),
        ('Meatballs', 'Meatballs', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'), 'system', 'system'),
        ('Minced Meat', 'Minced Meat', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'),  'system', 'system'),
        ('Chicken Wings', 'Chicken Wings', (SELECT category_id FROM web_store.category WHERE category_name = 'Meat & Poultry'), 'system', 'system'),

    -- Seafood
    ('Salmon Fillet', 'Atlantic Salmon Fillet', (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
    ('Shrimp', 'Large Shrimp', (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
    ('Tuna Steak', 'Fresh Tuna Steak', (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),


            ('Prawns', 'Prawns',  (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
            ('Crab Meat', 'Crab Meat',  (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
            ('Lobster Tail', 'Lobster Tail',  (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
            ('Clams', 'Clams',  (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
            ('Mussels', 'Mussels',  (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
            ('Oysters', 'Oysters',  (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
            ('Squid', 'Squid',  (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
            ('Octopus', 'Octopus',  (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
            ('Anchovies', 'Anchovies',  (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
            ('Sardines', 'Sardines',  (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
            ('Cod Fillet', 'Cod Fillet',  (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
            ('Haddock', 'Haddock',  (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
            ('Mackerel', 'Mackerel',  (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
            ('Tilapia', 'Tilapia',  (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
            ('Sea Bass', 'Sea Bass',  (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
            ('Catfish', 'Catfish',  (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),
            ('Halibut', 'Halibut',  (SELECT category_id FROM web_store.category WHERE category_name = 'Seafood'), 'system', 'system'),

    -- Bakery & Baked Goods
         ('White Bread', 'Fresh White Bread', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Croissant', 'Butter Croissant', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Bagels', 'Everything Bagels', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Whole Wheat Bread', 'Whole Wheat Bread', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Multigrain Bread', 'Multigrain Bread', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Baguette', 'Baguette', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Muffins', 'Muffins', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Donuts', 'Donuts', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Pita Bread', 'Pita Bread', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Naan', 'Naan', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Tortilla', 'Tortilla', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Sourdough Bread', 'Sourdough Bread', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Brioche', 'Brioche', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Focaccia', 'Focaccia', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Ciabatta', 'Ciabatta', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('English Muffins', 'English Muffins', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Cakes', 'Cakes', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Pastries', 'Pastries', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Cookies', 'Cookies', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),
        ('Brownies', 'Brownies', (SELECT category_id FROM web_store.category WHERE category_name = 'Bakery & Baked Goods'), 'system', 'system'),

    -- Beverages
    ('Orange Juice', 'Fresh Orange Juice', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Green Tea', 'Organic Green Tea', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Coffee', 'Premium Coffee Beans', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Mineral Water', 'Mineral Water', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Sparkling Water', 'Sparkling Water', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Apple Juice', 'Apple Juice', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Grape Juice', 'Grape Juice', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Carrot Juice', 'Carrot Juice', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Tomato Juice', 'Tomato Juice', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Lemonade', 'Lemonade', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Iced Tea', 'Iced Tea', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Black Tea', 'Black Tea', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Espresso', 'Espresso', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Latte', 'Latte', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Cappuccino', 'Cappuccino', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Hot Chocolate', 'Hot Chocolate', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Milkshake', 'Milkshake', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Smoothies', 'Smoothies', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Energy Drinks', 'Energy Drinks', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system'),
    ('Soft Drinks', 'Soft Drinks', (SELECT category_id FROM web_store.category WHERE category_name = 'Beverages'), 'system', 'system')

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
        WHEN 'Pear' THEN 279
        WHEN 'Peach' THEN 299
        WHEN 'Plum' THEN 289
        WHEN 'Cherry' THEN 649
        WHEN 'Pomegranate' THEN 399
        WHEN 'Guava' THEN 249
        WHEN 'Lychee' THEN 479
        WHEN 'Apricot' THEN 499
        WHEN 'Fig' THEN 549

        -- Grains
        WHEN 'Rice' THEN 299
        WHEN 'Wheat' THEN 249
        WHEN 'Barley' THEN 279
        WHEN 'Oats' THEN 399
        WHEN 'Quinoa' THEN 699
        WHEN 'Corn' THEN 249
        WHEN 'Millet' THEN 279
        WHEN 'Sorghum' THEN 269
        WHEN 'Rye' THEN 299
        WHEN 'Buckwheat' THEN 349
        WHEN 'Amaranth' THEN 389
        WHEN 'Teff' THEN 399
        WHEN 'Spelt' THEN 369
        WHEN 'Farro' THEN 399
        WHEN 'Fonio' THEN 419
        WHEN 'Wild Rice' THEN 599
        WHEN 'Polenta' THEN 249
        WHEN 'Couscous' THEN 299
        WHEN 'Semolina' THEN 199
        WHEN 'Bulgar Wheat' THEN 279

        -- Pulses
        WHEN 'Lentils' THEN 249
        WHEN 'Chickpeas' THEN 279
        WHEN 'Black Beans' THEN 329
        WHEN 'Kidney Beans' THEN 299
        WHEN 'Green Gram' THEN 259
        WHEN 'Black-eyed Peas' THEN 229
        WHEN 'Pinto Beans' THEN 269
        WHEN 'Navy Beans' THEN 289
        WHEN 'Soybeans' THEN 239
        WHEN 'Mung Beans' THEN 249
        WHEN 'Adzuki Beans' THEN 279
        WHEN 'Fava Beans' THEN 319
        WHEN 'Lima Beans' THEN 289
        WHEN 'Split Peas' THEN 229
        WHEN 'Horse Gram' THEN 199
        WHEN 'Broad Beans' THEN 299
        WHEN 'Red Lentils' THEN 249
        WHEN 'Yellow Split Peas' THEN 219
        WHEN 'Green Lentils' THEN 239
        WHEN 'White Beans' THEN 249

        -- Dairy
        WHEN 'Milk' THEN 199
        WHEN 'Cheddar Cheese' THEN 599
        WHEN 'Yogurt' THEN 249
        WHEN 'Butter' THEN 449
        WHEN 'Paneer' THEN 699
        WHEN 'Ghee' THEN 799
        WHEN 'Mozzarella Cheese' THEN 599
        WHEN 'Buttermilk' THEN 199
        WHEN 'Condensed Milk' THEN 399
        WHEN 'Evaporated Milk' THEN 389
        WHEN 'Sour Cream' THEN 299
        WHEN 'Cottage Cheese' THEN 399
        WHEN 'Ricotta Cheese' THEN 429
        WHEN 'Blue Cheese' THEN 699
        WHEN 'Parmesan Cheese' THEN 749
        WHEN 'Goat Cheese' THEN 799
        WHEN 'Ice Cream' THEN 599
        WHEN 'Milk Powder' THEN 459
        WHEN 'Whipped Cream' THEN 379

        -- Meat
        WHEN 'Chicken Breast' THEN 899
        WHEN 'Ground Beef' THEN 799
        WHEN 'Lamb Chops' THEN 1499
        WHEN 'Chicken Thigh' THEN 699
        WHEN 'Whole Chicken' THEN 999
        WHEN 'Beef Steak' THEN 1299
        WHEN 'Pork Chops' THEN 1199
        WHEN 'Pork Ribs' THEN 1099
        WHEN 'Lamb Leg' THEN 1399
      WHEN 'Turkey Breast' THEN 1299
      WHEN 'Duck Breast' THEN 1399
      WHEN 'Goat Meat' THEN 1199
      WHEN 'Veal Cutlets' THEN 1499
      WHEN 'Bacon' THEN 899
      WHEN 'Sausages' THEN 799
      WHEN 'Ham' THEN 899
      WHEN 'Salami' THEN 949
      WHEN 'Meatballs' THEN 699
      WHEN 'Minced Meat' THEN 749
      WHEN 'Chicken Wings' THEN 599

      -- Seafood
      WHEN 'Salmon Fillet' THEN 1999
      WHEN 'Shrimp' THEN 1299
      WHEN 'Tuna Steak' THEN 1799
      WHEN 'Prawns' THEN 1199
      WHEN 'Crab Meat' THEN 1499
      WHEN 'Lobster Tail' THEN 1999
      WHEN 'Clams' THEN 899
      WHEN 'Mussels' THEN 799
      WHEN 'Oysters' THEN 1099
      WHEN 'Squid' THEN 899
      WHEN 'Octopus' THEN 999
      WHEN 'Anchovies' THEN 699
      WHEN 'Sardines' THEN 499
      WHEN 'Cod Fillet' THEN 1499
      WHEN 'Haddock' THEN 1399
      WHEN 'Mackerel' THEN 1199
      WHEN 'Tilapia' THEN 999
      WHEN 'Sea Bass' THEN 1399
      WHEN 'Catfish' THEN 1099
      WHEN 'Halibut' THEN 1699

      -- Bakery
      WHEN 'White Bread' THEN 299
      WHEN 'Croissant' THEN 199
      WHEN 'Bagels' THEN 349
      WHEN 'Whole Wheat Bread' THEN 329
      WHEN 'Multigrain Bread' THEN 349
      WHEN 'Baguette' THEN 299
      WHEN 'Muffins' THEN 349
      WHEN 'Donuts' THEN 249
      WHEN 'Pita Bread' THEN 229
      WHEN 'Naan' THEN 249
      WHEN 'Tortilla' THEN 199
      WHEN 'Sourdough Bread' THEN 329
      WHEN 'Brioche' THEN 349
      WHEN 'Focaccia' THEN 359
      WHEN 'Ciabatta' THEN 339
      WHEN 'English Muffins' THEN 289
      WHEN 'Cakes' THEN 699
      WHEN 'Pastries' THEN 399
      WHEN 'Cookies' THEN 299
      WHEN 'Brownies' THEN 349

      -- Beverages
      WHEN 'Orange Juice' THEN 399
      WHEN 'Green Tea' THEN 599
      WHEN 'Coffee' THEN 899
      WHEN 'Mineral Water' THEN 199
      WHEN 'Sparkling Water' THEN 249
      WHEN 'Apple Juice' THEN 399
      WHEN 'Grape Juice' THEN 429
      WHEN 'Carrot Juice' THEN 389
      WHEN 'Tomato Juice' THEN 359
      WHEN 'Lemonade' THEN 299
      WHEN 'Iced Tea' THEN 319
      WHEN 'Black Tea' THEN 349
      WHEN 'Espresso' THEN 449
      WHEN 'Latte' THEN 479
      WHEN 'Cappuccino' THEN 499
      WHEN 'Hot Chocolate' THEN 399
      WHEN 'Milkshake' THEN 399
      WHEN 'Smoothies' THEN 499
      WHEN 'Energy Drinks' THEN 349
      WHEN 'Soft Drinks' THEN 299
      ELSE 199
END,
'system', 'system'
FROM web_store.product p
WHERE NOT EXISTS (
    SELECT 1 FROM web_store.product_price pp
    WHERE pp.product_id = p.product_id AND pp.currency_id = 1
);