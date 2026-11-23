
-- CT5106 AnyGuessr: add dimensions required by MapController.
--
-- New MySQL databases run this after 001, 002 and 003.
-- Existing databases must apply it once manually.
--
-- Defaults preserve compatibility with existing map records
-- and seed data. Newly saved maps provide their own dimensions.

ALTER TABLE map_data
    ADD COLUMN width INT NOT NULL DEFAULT 800,
    ADD COLUMN height INT NOT NULL DEFAULT 500;
