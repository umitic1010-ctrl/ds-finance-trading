-- Seed example customer and employee identities
WITH inserted_customer_person AS (
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
    VALUES (
        'example_c@banking.de',
        'Example',
        'Customer',
        'mj7tZEmA0SWbyOU4nhhfMJhS6DqeCfgrdE3XFRYhSfQ=',
        'hhPGWXgt9QjjZy0o68q4xA==',
        120000,
        'CUSTOMER',
        'ACTIVE',
        now()
    )
    ON CONFLICT (email) DO NOTHING
    RETURNING id
), customer_person AS (
    SELECT id FROM inserted_customer_person
    UNION ALL
    SELECT id FROM person WHERE email = 'example_c@banking.de'
)
INSERT INTO customer (
    person_id,
    customer_number,
    address,
    city,
    country,
    postal_code,
    phone_number,
    status,
    created_at
)
SELECT
    id,
    'CUST-EX-1',
    'Example Street 1',
    'Vienna',
    'Austria',
    '1010',
    '+43-1-555-0000',
    'ACTIVE',
    now()
FROM customer_person
ON CONFLICT (customer_number) DO NOTHING;

WITH inserted_employee_person AS (
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
    VALUES (
        'example_e@banking.de',
        'Example',
        'Employee',
        '4SfZguJKXwHehJX/HC9wzDBntCgb57D/ORzTkb4cvtI=',
        'sJ5w2wPDbdD3fsH0UejKQA==',
        120000,
        'EMPLOYEE',
        'ACTIVE',
        now()
    )
    ON CONFLICT (email) DO NOTHING
    RETURNING id
), employee_person AS (
    SELECT id FROM inserted_employee_person
    UNION ALL
    SELECT id FROM person WHERE email = 'example_e@banking.de'
)
INSERT INTO employee (
    person_id,
    employee_number,
    department
)
SELECT
    id,
    'EMP-001',
    'Trading'
FROM employee_person
ON CONFLICT (person_id) DO NOTHING;
