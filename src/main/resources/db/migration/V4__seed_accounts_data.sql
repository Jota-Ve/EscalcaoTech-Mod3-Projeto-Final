-- Accounts referenced by the seeded transfers in V2, plus a couple of extra test accounts
INSERT INTO accounts (id, account_number, owner_name) VALUES
    (1, '00001-1', 'Alice Johnson'),
    (2, '00002-2', 'Bob Smith'),
    (3, '00003-3', 'Carol Davis'),
    (4, '00004-4', 'David Lee'),
    (5, '11111-1', 'Test Source Account'),
    (6, '22222-2', 'Test Destination Account');

ALTER TABLE accounts ALTER COLUMN id RESTART WITH 7;
