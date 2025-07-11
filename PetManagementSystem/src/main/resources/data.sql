-- Sample addresses
INSERT INTO address (city, type, address_name, number,created_by, created_time, last_updated_by, last_update_time) VALUES
('Paris', 'road', 'Antoine Lavoisier', '10','admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('London', 'street', 'Baker', '221B','admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('New York', 'avenue', '5th', '100','admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('London', 'ABC', '5th', '500','admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('London', 'XYZ', '10th', '600','admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP);

-- Sample users
INSERT INTO "user" (name, first_name, address_id, age, gender, deceased,created_by, created_time, last_updated_by, last_update_time) VALUES
('Vasani', 'Samir', 1, 30, 'MALE', false,'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('Vasani', 'Ekta', 1, 25, 'FEMALE', false,'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('Modi', 'Narendra', 2, 60, 'MALE', false,'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('Trump', 'Donald', 3, 50, 'MALE', false,'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('Kohli', 'Virat', 4, 50, 'MALE', false,'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('Lisa', 'Fernades',5, 20, 'FEMALE', false,'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP);

-- Sample pets
INSERT INTO pet (name, age, type, deceased,created_by, created_time, last_updated_by, last_update_time) VALUES
('Fido', 3, 'dog', false,'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('Whiskers', 2, 'cat', false,'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('Slither', 1, 'snake', false,'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP);

-- Sample ownerships
INSERT INTO user_pet (user_id, pet_id) VALUES
(1, 1),
(2, 1),
(3, 2),
(4, 3);

