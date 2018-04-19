/*
 * Copyright (C) 2017 Weather Decision Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.os.vt.mvt.adapt.jts;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

/**
 * Filter {@link Polygon} and {@link MultiPolygon} by area or
 * {@link LineString} and {@link MultiLineString} by length.
 *
 * @see IGeometryFilter
 */
public final class GeomMinSizeFilter implements IGeometryFilter {

  /**
   * Minimum area.
   */
  private final double minArea;

  /**
   * Minimum length.
   */
  private final double minLength;

  /**
   * GeomMinSizeFilter.
   * @param minArea   minimum area required for a {@link Polygon} or {@link MultiPolygon}
   * @param minLength minimum length required for a {@link LineString} or {@link MultiLineString}
   */
  public GeomMinSizeFilter(double minArea, double minLength) {
    if (minArea < 0.0d) {
      throw new IllegalArgumentException("minArea must be >= 0");
    }
    if (minLength < 0.0d) {
      throw new IllegalArgumentException("minLength must be >= 0");
    }

    this.minArea = minArea;
    this.minLength = minLength;
  }

  @Override
  public boolean accept(Geometry geometry) {
    boolean accept = true;

    if ((geometry instanceof Polygon || geometry instanceof MultiPolygon)
        && geometry.getArea() < minArea) {
      accept = false;

    } else if ((geometry instanceof LineString || geometry instanceof MultiLineString)
        && geometry.getLength() < minLength) {
      accept = false;
    }

    return accept;
  }
}
