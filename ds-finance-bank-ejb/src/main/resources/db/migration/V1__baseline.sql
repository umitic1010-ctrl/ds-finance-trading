-- V1 Baseline schema for DS Finance Bank
CREATE TABLE customer (
    id BIGSERIAL PRIMARY KEY,
    customer_number VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    city VARCHAR(120),
    country VARCHAR(120),
    postal_code VARCHAR(40),
    email VARCHAR(100),
    phone_number VARCHAR(50),
    status VARCHAR(30) DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE bank_volume (
    id BIGSERIAL PRIMARY KEY,
    available_volume NUMERIC(18,2) NOT NULL,
    initial_volume NUMERIC(18,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE depot_position (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE,
    stock_symbol VARCHAR(20) NOT NULL,
    stock_name VARCHAR(255),
    quantity INTEGER NOT NULL,
    CONSTRAINT uq_depot_customer_symbol UNIQUE (customer_id, stock_symbol)
);

