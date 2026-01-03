-- Create person and employee tables and link customers to persons
CREATE TABLE person (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,
    password_iterations INTEGER NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(30) DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE employee (
    person_id BIGINT PRIMARY KEY REFERENCES person(id) ON DELETE CASCADE,
    employee_number VARCHAR(50),
    department VARCHAR(100)
);

ALTER TABLE customer
    ADD COLUMN person_id BIGINT;

-- Create person records for existing customers (default password: customerpass)
INSERT INTO person (
    email,
    first_name,
    last_name,
    password_hash,
    password_salt,
    password_iterations,
    role,
    status,
    created_at
)
SELECT
    COALESCE(c.email, 'customer_' || c.id || '@example.invalid'),
    COALESCE(c.first_name, 'Unknown'),
    COALESCE(c.last_name, 'Customer'),
    'mj7tZEmA0SWbyOU4nhhfMJhS6DqeCfgrdE3XFRYhSfQ=',
    'hhPGWXgt9QjjZy0o68q4xA==',
    120000,
    'CUSTOMER',
    COALESCE(c.status, 'ACTIVE'),
    COALESCE(c.created_at, now())
FROM customer c
WHERE NOT EXISTS (
    SELECT 1 FROM person p WHERE p.email = COALESCE(c.email, 'customer_' || c.id || '@example.invalid')
);

UPDATE customer c
SET person_id = p.id
FROM person p
WHERE p.email = COALESCE(c.email, 'customer_' || c.id || '@example.invalid')
  AND c.person_id IS NULL;

ALTER TABLE customer
    ALTER COLUMN person_id SET NOT NULL;

ALTER TABLE customer
    ADD CONSTRAINT uq_customer_person UNIQUE (person_id),
    ADD CONSTRAINT fk_customer_person FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE CASCADE;

DROP INDEX IF EXISTS idx_customer_name;

ALTER TABLE customer
    DROP COLUMN first_name,
    DROP COLUMN last_name,
    DROP COLUMN email;
