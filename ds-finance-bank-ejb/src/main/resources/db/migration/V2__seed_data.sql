-- Seed initial bank volume and sample customers
INSERT INTO bank_volume (available_volume, initial_volume, created_at)
SELECT 1000000000.00, 1000000000.00, now()
WHERE NOT EXISTS (SELECT 1 FROM bank_volume);

INSERT INTO customer (customer_number, first_name, last_name, address, city, country, postal_code, email, phone_number, status, created_at)
VALUES
    ('CUST-0001', 'Anna', 'Muster', 'Hauptstrasse 1', 'Wien', 'Austria', '1010', 'anna.muster@example.com', '+43-1-555-0101', 'ACTIVE', now()),
    ('CUST-0002', 'Max', 'Beispiel', 'Ring 5', 'Graz', 'Austria', '8010', 'max.beispiel@example.com', '+43-316-555-0102', 'ACTIVE', now())
ON CONFLICT (customer_number) DO NOTHING;

