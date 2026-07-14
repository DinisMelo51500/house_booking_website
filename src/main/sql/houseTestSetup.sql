INSERT INTO locations (name, type) VALUES ('Test Location 1', 'COUNTRY');
INSERT INTO locations (name, type, parent_id) VALUES ('Test Location 2', 'LOCALITY', 1);

INSERT INTO users (id_user, user_name, email, token, password)
VALUES (1, 'Test Owner 1', 'owner1@test.com', gen_random_uuid(), 'password123');

INSERT INTO users (id_user, user_name, email, token, password)
VALUES (2, 'Test Owner 2', 'owner2@test.com', gen_random_uuid(), 'password456');

SELECT setval('users_id_user_seq', 2);