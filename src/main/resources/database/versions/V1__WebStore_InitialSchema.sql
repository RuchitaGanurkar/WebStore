-- Create Schema (place this at the beginning)
CREATE SCHEMA IF NOT EXISTS web_store;

-- Create Sequences
CREATE SEQUENCE IF NOT EXISTS web_store.seq_catalogue_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_category_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_product_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_product_price_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_currency_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_user_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS web_store.seq_catalogue_category_id START WITH 1 INCREMENT BY 1;

-- Create User Table
CREATE TABLE IF NOT EXISTS web_store.users (
    user_id INT NOT NULL DEFAULT nextval('web_store.seq_user_id') PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    full_name VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_username UNIQUE (username),
    CONSTRAINT uk_email UNIQUE (email)
);

-- Create Currency Table
CREATE TABLE IF NOT EXISTS web_store.currency (
    currency_id INT NOT NULL DEFAULT nextval('web_store.seq_currency_id') PRIMARY KEY,
    currency_code VARCHAR(5) NOT NULL,
    currency_name VARCHAR(50) NOT NULL,
    currency_symbol VARCHAR(5) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    CONSTRAINT uk_currency_code UNIQUE (currency_code)
);

-- Create Catalogue Table
CREATE TABLE IF NOT EXISTS web_store.catalogue (
    catalogue_id INT NOT NULL DEFAULT nextval('web_store.seq_catalogue_id') PRIMARY KEY,
    catalogue_name VARCHAR(100) NOT NULL,
    catalogue_description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    CONSTRAINT uk_catalogue_name UNIQUE (catalogue_name)
);

-- Create Category Table
CREATE TABLE IF NOT EXISTS web_store.category (
    category_id INT NOT NULL DEFAULT nextval('web_store.seq_category_id') PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL,
    category_description VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    CONSTRAINT uk_category_name UNIQUE (category_name)
);

-- Create Product Table
CREATE TABLE IF NOT EXISTS web_store.product (
    product_id INT NOT NULL DEFAULT nextval('web_store.seq_product_id') PRIMARY KEY,
    product_name VARCHAR(50) NOT NULL,
    product_description VARCHAR(100),
    category_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id) REFERENCES web_store.category (category_id),
    CONSTRAINT uk_product_name UNIQUE (product_name)
);

-- Create Product_Price Table
CREATE TABLE IF NOT EXISTS web_store.product_price (
    product_price_id INT NOT NULL DEFAULT nextval('web_store.seq_product_price_id') PRIMARY KEY,
    product_id INT NOT NULL,
    currency_id INT NOT NULL,
    price_amount NUMERIC(38, 0) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    CONSTRAINT fk_product_price_product
        FOREIGN KEY (product_id) REFERENCES web_store.product (product_id) ON DELETE CASCADE,
    CONSTRAINT fk_product_price_currency
        FOREIGN KEY (currency_id) REFERENCES web_store.currency (currency_id),
    CONSTRAINT uk_product_currency UNIQUE (product_id, currency_id)
);

-- Create Catalogue_Category Table
CREATE TABLE IF NOT EXISTS web_store.catalogue_category (
    catalogue_category_id INT NOT NULL DEFAULT nextval('web_store.seq_catalogue_category_id') PRIMARY KEY,
    catalogue_id INT NOT NULL,
    category_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    CONSTRAINT fk_catalogue_category_catalogue
        FOREIGN KEY (catalogue_id) REFERENCES web_store.catalogue (catalogue_id) ON DELETE CASCADE,
    CONSTRAINT fk_catalogue_category_category
        FOREIGN KEY (category_id) REFERENCES web_store.category (category_id) ON DELETE CASCADE,
    CONSTRAINT uq_catalogue_category UNIQUE (catalogue_id , category_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_product_category ON web_store.product(category_id);
CREATE INDEX idx_product_price_product ON web_store.product_price(product_id);
CREATE INDEX idx_product_price_currency ON web_store.product_price(currency_id);
CREATE INDEX idx_catalogue_category_catalogue ON web_store.catalogue_category(catalogue_id);
CREATE INDEX idx_catalogue_category_category ON web_store.catalogue_category(category_id);


-- Insert test data for Currency
--INSERT INTO web_store.currency (currency_code, currency_name, currency_symbol, created_by, updated_by)
--VALUES
--    ('USD', 'US Dollar', '$', 'system', 'system'),
--    ('EUR', 'Euro', '€', 'system', 'system'),
--    ('GBP', 'Pound', '£', 'system', 'system');
--
---- Insert test data for User
--INSERT INTO web_store.users (username, email, full_name, role)
--VALUES
--    ('admin', 'admin@webstore.com', 'Admin User', 'ADMIN'),
--    ('user1', 'user1@example.com', 'Test User', 'USER'),
--    ('system', 'system@example.com', 'System User', 'SYSTEM');
--
---- Insert test data for Catalogue
--INSERT INTO web_store.catalogue (catalogue_name, catalogue_description, created_by, updated_by)
--VALUES
--    ('Summer Collection', 'Products for summer season', 'system', 'system'),
--    ('Winter Collection', 'Products for winter season', 'system', 'system');
--
-- --Insert test data for Category
--INSERT INTO web_store.category (category_name, category_description, created_by, updated_by)
--VALUES
--    ('Electronics', 'Electronic devices and gadgets', 'system', 'system'),
--    ('Clothing', 'Apparel and accessories', 'system', 'system'),
--    ('Home', 'Home and kitchen items', 'system', 'system');
--
-- --Link Categories to Catalogues
--INSERT INTO web_store.catalogue_category (catalogue_id, category_id, created_by, updated_by)
--VALUES
--    (1, 1, 'system', 'system'),
--    (1, 2, 'system', 'system'),
--    (2, 2, 'system', 'system'),
--    (2, 3, 'system', 'system');
--
---- Insert test data for Product
--INSERT INTO web_store.product (product_name, product_description, category_id, created_by, updated_by)
--VALUES
--    ('Smartphone X', 'Latest smartphone model', 1, 'system', 'system'),
--    ('Laptop Pro', 'High-performance laptop', 1, 'system', 'system'),
--    ('T-shirt Basic', 'Cotton t-shirt', 2, 'system', 'system'),
--    ('Coffee Maker', 'Automatic coffee maker', 3, 'system', 'system');
--
---- Insert test data for Product_Price
--INSERT INTO web_store.product_price (product_id, currency_id, price_amount, created_by, updated_by)
--VALUES
--    (1, 1, 99900, 'system', 'system'),  -- Smartphone in USD
--    (1, 2, 89900, 'system', 'system'),  -- Smartphone in EUR
--    (2, 1, 129900, 'system', 'system'), -- Laptop in USD
--    (3, 1, 1999, 'system', 'system'),   -- T-shirt in USD
--    (4, 1, 5999, 'system', 'system');   -- Coffee Maker in USD