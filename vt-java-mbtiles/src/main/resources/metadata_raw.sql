--
-- Copyright (C) 2016 Ordnance Survey
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

SELECT everything.min_zoom AS min_zoom, everything.max_zoom AS max_zoom, max_zoom.minx AS max_zoom_minx, max_zoom.maxx AS max_zoom_maxx, max_zoom.miny AS max_zoom_miny, max_zoom.maxy AS max_zoom_maxy
FROM
(SELECT MIN(zoom_level) AS min_zoom, MAX(zoom_level) AS max_zoom FROM tiles) AS everything,
(SELECT MIN(tile_column) AS minx, MIN(tile_row) AS miny, MAX(tile_column) AS maxx, MAX(tile_row) AS maxy FROM tiles WHERE zoom_level = (SELECT MAX(zoom_level) FROM tiles)) AS max_zoom