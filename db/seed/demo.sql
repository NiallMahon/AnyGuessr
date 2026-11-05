-- Demo data for a new database: the starter player, the World map and the three regions the quiz buttons use.

INSERT INTO users (user_id, username, created_at)
VALUES (1, 'CillianShine', CURRENT_TIMESTAMP);

INSERT INTO map_data (map_id, owner_id, map_name, image_ref, status, version, created_at)
VALUES (1, 1, 'World', 'world.png', 'PUBLISHED', 1, CURRENT_TIMESTAMP);

INSERT INTO regions (region_id, map_id, name, polygon)
VALUES (1, 1, 'Region 1', '[]'),
       (2, 1, 'Region 2', '[]'),
       (3, 1, 'Region 3', '[]');
