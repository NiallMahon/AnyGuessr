
-- CT5106 Assignment 4: clickable polygon gameplay fixture.
-- For local development and demonstration only.
--
-- Creates a separate playable map without modifying existing maps.
-- Uses the same point-array format as MapController.
-- Can be rerun without duplicating the named map or regions.
--
-- Prerequisite: db/init/004_map_dimensions.sql applied.
-- Prerequisite: at least one user exists (003_identity_users.sql
-- creates a starter user on a fresh database).
START TRANSACTION;

INSERT INTO map_data (
    owner_id,
    map_name,
    image_ref,
    status,
    version,
    created_at,
    width,
    height
)
SELECT
    u.user_id,
    'CT5106 Clickable Polygon Demo',
    '',
    'PUBLISHED',
    1,
    CURRENT_TIMESTAMP,
    800,
    500
FROM users u
WHERE NOT EXISTS (
    SELECT 1
    FROM map_data
    WHERE map_name = 'CT5106 Clickable Polygon Demo'
      AND deleted_at IS NULL
)
ORDER BY u.user_id
LIMIT 1;

SET @demo_map_id = (
    SELECT map_id
    FROM map_data
    WHERE map_name = 'CT5106 Clickable Polygon Demo'
      AND deleted_at IS NULL
    ORDER BY map_id DESC
    LIMIT 1
);

-- Three separate polygons, each with four vertices.

INSERT INTO regions (map_id, name, polygon)
SELECT
    @demo_map_id,
    'North Zone',
    '[{"x":50,"y":80},{"x":240,"y":80},{"x":240,"y":360},{"x":50,"y":360}]'
WHERE @demo_map_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM regions
      WHERE map_id = @demo_map_id AND name = 'North Zone'
  );

INSERT INTO regions (map_id, name, polygon)
SELECT
    @demo_map_id,
    'Central Zone',
    '[{"x":300,"y":80},{"x":500,"y":80},{"x":500,"y":360},{"x":300,"y":360}]'
WHERE @demo_map_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM regions
      WHERE map_id = @demo_map_id AND name = 'Central Zone'
  );

INSERT INTO regions (map_id, name, polygon)
SELECT
    @demo_map_id,
    'South Zone',
    '[{"x":560,"y":80},{"x":750,"y":80},{"x":750,"y":360},{"x":560,"y":360}]'
WHERE @demo_map_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM regions
      WHERE map_id = @demo_map_id AND name = 'South Zone'
  );

COMMIT;

SELECT
    m.map_id,
    m.map_name,
    m.width,
    m.height,
    COUNT(r.region_id) AS region_count
FROM map_data m
LEFT JOIN regions r ON r.map_id = m.map_id
WHERE m.map_id = @demo_map_id
GROUP BY m.map_id, m.map_name, m.width, m.height;
