-- USERS
INSERT INTO users (user_name, email, token, password)
VALUES ('User One', 'user1@test.com', gen_random_uuid(), 'password123');

INSERT INTO users (user_name, email, token, password)
VALUES ('User Two', 'user2@test.com', gen_random_uuid(), 'password456');

INSERT INTO users (user_name, email, token, password)
VALUES ('User Three', 'user3@test.com', 'e3b2c1a0-d4ef-4cba-bc1a-2b3c4d5e6f7a', 'password789');


-- LOCATIONS
INSERT INTO locations (id_loc, name, type, parent_id) VALUES
(1, 'Portugal', 'COUNTRY', NULL),

(2, 'Lisboa', 'DISTRICT', 1),
(3, 'Marvila', 'LOCALITY', 2),

-- Lisboa estrutura
(5, 'Lisboa Município', 'MUNICIPALITY', 2),
(6, 'Alcântara', 'LOCALITY', 5),
(7, 'Belém', 'LOCALITY', 5),
(8, 'Parque das Nações', 'LOCALITY', 5),

-- Marvila estrutura
(9, 'Marvila Município', 'MUNICIPALITY', 2),
(10, 'Braço de Prata', 'LOCALITY', 9),
(11, 'Beato', 'LOCALITY', 9),

-- Porto
(12, 'Porto Município', 'MUNICIPALITY', 1),
(13, 'Cedofeita', 'LOCALITY', 12),
(14, 'Foz do Douro', 'LOCALITY', 12),
(15, 'Boavista', 'LOCALITY', 12);


-- HOUSES
INSERT INTO houses (title, location_id, area_sq_mt, price_per_night, description, owner_id) VALUES
('House One', 6, 100, 50.0, 'Description One', 1),
('House Two', 7, 200, 75.0, 'Description Two', 2),
('House Three', 8, 120, 60.0, 'Description Three', 1),
('House Four', 10, 90, 45.0, 'Description Four', 2),
('House Five', 11, 150, 80.0, 'Description Five', 1),
('House Six', 13, 110, 55.0, 'Description Six', 2),
('House Seven', 14, 130, 70.0, 'Description Seven', 1),
('House Eight', 15, 95, 50.0, 'Description Eight', 2),
('House Nine', 13, 160, 90.0, 'Description Nine', 1),
('House Ten', 14, 140, 85.0, 'Description Ten', 2),
('House Eleven', 15, 180, 120.0, 'Description Eleven', 1),
('House Twelve', 6, 200, 150.0, 'Description Twelve', 2);


-- BOOKINGS
INSERT INTO bookings (house_id, user_id, start_date, end_date) VALUES
(1, 1, '2024-01-10', '2024-01-15'),
(2, 2, '2024-02-01', '2024-02-05'),
(3, 1, '2024-03-10', '2024-03-12'),
(4, 2, '2024-04-01', '2024-04-03'),
(5, 1, '2024-05-05', '2024-05-10'),
(6, 2, '2024-06-01', '2024-06-04'),
(7, 1, '2024-07-10', '2024-07-15'),
(8, 2, '2024-08-01', '2024-08-02'),
(9, 1, '2024-09-01', '2024-09-07'),
(10, 2, '2024-10-10', '2024-10-12'),
(11, 1, '2024-11-01', '2024-11-05'),
(1, 2, '2023-12-20', '2023-12-25'),
(1, 1, '2024-01-01', '2024-01-03'),
(1, 2, '2024-01-05', '2024-01-08'),
(1, 1, '2024-01-20', '2024-01-22'),
(1, 2, '2024-01-25', '2024-01-28'),
(1, 1, '2024-02-01', '2024-02-03'),
(1, 2, '2024-02-05', '2024-02-07'),
(1, 1, '2024-02-10', '2024-02-12'),
(1, 2, '2024-02-15', '2024-02-18'),
(1, 1, '2024-02-20', '2024-02-22'),
(12, 2, '2024-12-20', '2024-12-25');