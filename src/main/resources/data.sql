-- CADMUS-CR-14 VulnShop seed data. Synthetic only - never load real data.

INSERT INTO app_user (username, password_hash, email, role, tenant, internal_risk_score, reset_code) VALUES
  ('alice',   '5f4dcc3b5aa765d61d8327deb882cf99', 'alice@example.invalid',   'USER',    'acme',    12, NULL),
  ('bob',     '5f4dcc3b5aa765d61d8327deb882cf99', 'bob@example.invalid',     'USER',    'globex',  47, NULL),
  ('carol',   '5f4dcc3b5aa765d61d8327deb882cf99', 'carol@example.invalid',   'SUPPORT', 'acme',     3, NULL),
  ('adminjoe','5f4dcc3b5aa765d61d8327deb882cf99', 'joe@example.invalid',     'ADMIN',   'acme',     1, NULL);

INSERT INTO product (name, description, price, category) VALUES
  ('Mechanical Keyboard', 'Tactile switches, <b>85%</b> layout', 89.90,  'peripherals'),
  ('27" Monitor',         'IPS panel, 1440p',                    239.00, 'displays'),
  ('USB-C Dock',          'Seven ports',                          74.50, 'peripherals'),
  ('Ergonomic Mouse',     'Vertical grip',                        45.00, 'peripherals'),
  ('Laptop Stand',        'Aluminium',                            32.00, 'accessories');

-- A01: ids start high and run sequentially, so a missing ownership check
--      becomes an enumerable export.
ALTER TABLE orders ALTER COLUMN id RESTART WITH 1000;

INSERT INTO orders (customer_username, tenant, product_name, quantity, unit_price, total, status, shipping_address) VALUES
  ('alice', 'acme',   'Mechanical Keyboard', 1, 89.90,  89.90,  'SHIPPED', '12 Alder Way, Springfield'),
  ('alice', 'acme',   '27" Monitor',         2, 239.00, 478.00, 'PLACED',  '12 Alder Way, Springfield'),
  ('bob',   'globex', 'USB-C Dock',          1, 74.50,  74.50,  'SHIPPED', '4 Beech Road, Shelbyville'),
  ('bob',   'globex', 'Ergonomic Mouse',     3, 45.00,  135.00, 'PLACED',  '4 Beech Road, Shelbyville'),
  ('carol', 'acme',   'Laptop Stand',        1, 32.00,  32.00,  'PLACED',  '9 Cedar Lane, Ogdenville');
