-- MBTiles schema
-- Source: https://github.com/mapbox/mbtiles-spec/blob/master/1.2/spec.md
-- See also: https://github.com/mapbox/node-mbtiles/blob/master/lib/schema.sql

BEGIN;

CREATE TABLE metadata (name text, value text);
CREATE UNIQUE INDEX name on metadata (name);

CREATE TABLE tiles (zoom_level integer, tile_column integer, tile_row integer, tile_data blob);
CREATE UNIQUE INDEX tile_index on tiles (zoom_level, tile_column, tile_row);

COMMIT;
