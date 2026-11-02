-- Test data: one player and one map with two regions.

INSERT INTO users (user_id, username, created_at) VALUES (1, 'player-one', '2026-11-01 00:00:00');

INSERT INTO map_data (map_id, owner_id, map_name, image_ref, status, version, created_at)
VALUES (1, 1, 'Europe', 'europe.png', 'PUBLISHED', 1, '2026-11-01 00:00:00');

INSERT INTO regions (region_id, map_id, name, polygon) VALUES (1, 1, 'France', '[]');
INSERT INTO regions (region_id, map_id, name, polygon) VALUES (2, 1, 'Germany', '[]');
