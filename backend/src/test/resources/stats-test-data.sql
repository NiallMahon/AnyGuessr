-- Test data: two players and one map with three regions, after clearing old rows.

DELETE FROM guesses;
DELETE FROM user_games;
DELETE FROM regions;
DELETE FROM map_data;
DELETE FROM users;

INSERT INTO users (user_id, username, created_at)
VALUES (1, 'player-one', '2026-11-01 00:00:00'),
       (2, 'player-two', '2026-11-01 00:00:00');

INSERT INTO map_data (map_id, owner_id, map_name, image_ref, status, version, created_at)
VALUES (1, 1, 'Europe', 'europe.png', 'PUBLISHED', 1, '2026-11-01 00:00:00');

INSERT INTO regions (region_id, map_id, name, polygon)
VALUES (1, 1, 'France', '[]'),
       (2, 1, 'Germany', '[]'),
       (3, 1, 'Spain', '[]');
