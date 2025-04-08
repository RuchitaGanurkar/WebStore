-- Create Sequences
CREATE SEQUENCE IF NOT EXISTS seq_catalogue_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_category_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_product_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_product_price_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_currency_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_user_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_catalogue_category_id START WITH 1 INCREMENT BY 1;

-- Create User Table
CREATE TABLE IF NOT EXISTS users (
    user_id INT NOT NULL DEFAULT nextval('seq_user_id') PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    full_name VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create Currency Table
CREATE TABLE IF NOT EXISTS currency (
    currency_id INT NOT NULL DEFAULT nextval('seq_currency_id') PRIMARY KEY,
    currency_code VARCHAR(5) NOT NULL,
    currency_name VARCHAR(10) NOT NULL,
    currency_symbol VARCHAR(5) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50)
);

-- Create Catalogue Table
CREATE TABLE IF NOT EXISTS catalogue (
    catalogue_id INT NOT NULL DEFAULT nextval('seq_catalogue_id') PRIMARY KEY,
    catalogue_name VARCHAR(100) NOT NULL,
    catalogue_description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50)
);

-- Create Category Table
CREATE TABLE IF NOT EXISTS category (
    category_id INT NOT NULL DEFAULT nextval('seq_category_id') PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL,
    category_description VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50)
);

-- Create Product Table
CREATE TABLE IF NOT EXISTS product (
    product_id INT NOT NULL DEFAULT nextval('seq_product_id') PRIMARY KEY,
    product_name VARCHAR(50) NOT NULL,
    product_description VARCHAR(100),
    category_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id) REFERENCES category (category_id),
    CONSTRAINT uk_product_name UNIQUE (product_name)
);

-- Create Product_Price Table
CREATE TABLE IF NOT EXISTS product_price (
    product_price_id INT NOT NULL DEFAULT nextval('seq_product_price_id') PRIMARY KEY,
    product_id INT NOT NULL,
    currency_id INT NOT NULL,
    price_amount BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    CONSTRAINT fk_product_price_product
        FOREIGN KEY (product_id) REFERENCES product (product_id) ON DELETE CASCADE,
    CONSTRAINT fk_product_price_currency
        FOREIGN KEY (currency_id) REFERENCES currency (currency_id)
);

-- Create Catalogue_Category Table
CREATE TABLE IF NOT EXISTS catalogue_category (
    catalogue_id INT NOT NULL,
    category_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    PRIMARY KEY (catalogue_id, category_id),
    CONSTRAINT fk_catalogue_category_catalogue
        FOREIGN KEY (catalogue_id) REFERENCES catalogue (catalogue_id) ON DELETE CASCADE,
    CONSTRAINT fk_catalogue_category_category
        FOREIGN KEY (category_id) REFERENCES category (category_id) ON DELETE CASCADE
);

-- Insert test data for Currency
INSERT INTO currency (currency_code, currency_name, currency_symbol, created_by, updated_by)
VALUES
    ('USD', 'US Dollar', '$', 'system', 'system'),
    ('EUR', 'Euro', '€', 'system', 'system'),
    ('GBP', 'Pound', '£', 'system', 'system');

-- Insert test data for User
INSERT INTO users (username, email, full_name, role)
VALUES
    ('admin', 'admin@webstore.com', 'Admin User', 'ADMIN'),
    ('user1', 'user1@example.com', 'Test User', 'USER');

-- Insert test data for Catalogue
INSERT INTO catalogue (catalogue_name, catalogue_description, created_by, updated_by)
VALUES
    ('Summer Collection', 'Products for summer season', 'system', 'system'),
    ('Winter Collection', 'Products for winter season', 'system', 'system');

-- Insert test data for Category
INSERT INTO category (category_name, category_description, created_by, updated_by)
VALUES
    ('Electronics', 'Electronic devices and gadgets', 'system', 'system'),
    ('Clothing', 'Apparel and accessories', 'system', 'system'),
    ('Home', 'Home and kitchen items', 'system', 'system');

-- Link Categories to Catalogues
INSERT INTO catalogue_category (catalogue_id, category_id, created_by, updated_by)
VALUES
    (1, 1, 'system', 'system'),
    (1, 2, 'system', 'system'),
    (2, 2, 'system', 'system'),
    (2, 3, 'system', 'system');

-- Insert test data for Product
INSERT INTO product (product_name, product_description, category_id, created_by, updated_by)
VALUES
    ('Smartphone X', 'Latest smartphone model', 1, 'system', 'system'),
    ('Laptop Pro', 'High-performance laptop', 1, 'system', 'system'),
    ('T-shirt Basic', 'Cotton t-shirt', 2, 'system', 'system'),
    ('Coffee Maker', 'Automatic coffee maker', 3, 'system', 'system');

-- Insert test data for Product_Price
INSERT INTO product_price (product_id, currency_id, price_amount, created_by, updated_by)
VALUES
    (1, 1, 99900, 'system', 'system'),  -- Smartphone in USD
    (1, 2, 89900, 'system', 'system'),  -- Smartphone in EUR
    (2, 1, 129900, 'system', 'system'), -- Laptop in USD
    (3, 1, 1999, 'system', 'system'),   -- T-shirt in USD
    (4, 1, 5999, 'system', 'system');   -- Coffee Maker in USD