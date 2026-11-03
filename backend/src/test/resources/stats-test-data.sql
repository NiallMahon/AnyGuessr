-- Test data: one player and one map.
-- Old rows are cleared first, so every test starts from the same data.

DELETE FROM guesses;
DELETE FROM user_games;
DELETE FROM regions;
DELETE FROM map_data;
DELETE FROM users;

INSERT INTO users (user_id, username, created_at) VALUES (1, 'player-one', '2026-11-01 00:00:00');

INSERT INTO map_data (map_id, owner_id, map_name, image_ref, status, version, created_at)
VALUES (1, 1, 'Europe', 'europe.png', 'PUBLISHED', 1, '2026-11-01 00:00:00');
