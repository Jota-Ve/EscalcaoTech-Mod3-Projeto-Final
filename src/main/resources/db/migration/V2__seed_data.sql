-- Test users
-- admin / admin123
-- user  / user123
INSERT INTO users (id, username, password, enabled) VALUES
    (1, 'admin', '$2a$10$GXsE83zSk2Vp06EqNUISU.W/wuDBz4LHCEzuCwO3ZdCd7J9LbICR2', TRUE),
    (2, 'user',  '$2a$10$Vyut4BuxhZt3nDYT5/kwA.Pq/Ptox12N50jU1MS.rfMj2uhWuct5u', TRUE);

INSERT INTO user_roles (user_id, role) VALUES
    (1, 'ROLE_ADMIN'),
    (1, 'ROLE_USER'),
    (2, 'ROLE_USER');

-- Sample transfers
INSERT INTO transfers (id, source_account, destination_account, amount, fee, scheduled_date, transfer_date, status, created_at) VALUES
    (1, '00001-1', '00002-2', 1000.00, 10.00, '2026-08-10 00:00:00', '2026-08-15 00:00:00', 'COMPLETED', '2026-08-15 09:00:00'),
    (2, '00001-1', '00003-3', 500.00, 41.00, '2026-08-15 00:00:00', '2026-08-27 00:00:00', 'SCHEDULED', '2026-08-15 09:05:00'),
    (3, '00004-4', '00002-2', 2500.00, 205.00, '2026-08-15 00:00:00', '2026-08-30 00:00:00', 'SCHEDULED', '2026-08-15 09:10:00'),
    (4, '00004-4', '00002-2', 1500.00, 123.00, '2026-08-10 00:00:00', '2026-08-30 00:00:00', 'CANCELLED', '2026-08-15 09:10:00');

-- Adjust auto-increment sequences after inserting explicit ids
ALTER TABLE users ALTER COLUMN id RESTART WITH 3;
ALTER TABLE transfers ALTER COLUMN id RESTART WITH 5;
