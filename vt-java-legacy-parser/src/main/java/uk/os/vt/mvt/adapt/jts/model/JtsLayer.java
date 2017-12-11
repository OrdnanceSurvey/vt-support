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

package uk.os.vt.mvt.adapt.jts.model;

import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>JTS model of a Mapbox Vector Tile (MVT) layer.</p>
 * <p>A layer contains a subset of all geographic geometries in the tile.</p>
 */
public class JtsLayer {

  private final String name;
  private final Collection<Geometry> geometries;

  /**
   * Create an empty JTS layer.
   *
   * @param name layer name
   * @throws IllegalArgumentException when {@code name} is null
   */
  public JtsLayer(String name) {
    this(name, new ArrayList<Geometry>(0));
  }

  /**
   * Create a JTS layer with geometries.
   *
   * @param name       layer name
   * @param geometries layer geometries
   * @throws IllegalArgumentException when {@code name} or {@code geometries} are null
   */
  public JtsLayer(String name, Collection<Geometry> geometries) {
    validate(name, geometries);
    this.name = name;
    this.geometries = geometries;
  }

  /**
   * Get a read-only collection of geometry.
   *
   * @return unmodifiable collection of geometry.
   */
  public Collection<Geometry> getGeometries() {
    return geometries;
  }

  /**
   * Get the layer name.
   *
   * @return name of the layer
   */
  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    JtsLayer layer = (JtsLayer) object;

    if ((name != null && !name.equals(layer.name)) || (name == null && layer.name != null)) {
      return false;
    }

    if (geometries != null) {
      return geometries.equals(layer.geometries);
    } else {
      return layer.geometries == null;
    }
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (geometries != null ? geometries.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Layer{"
        + "name='" + name + '\''
        + ", geometries=" + geometries
        + '}';
  }

  /**
   * Validate the JtsLayer.
   *
   * @param name       mvt layer name
   * @param geometries geometries in the tile
   * @throws IllegalArgumentException when {@code name} or {@code geometries} are null
   */
  private static void validate(String name, Collection<Geometry> geometries) {
    if (name == null) {
      throw new IllegalArgumentException("layer name is null");
    }
    if (geometries == null) {
      throw new IllegalArgumentException("geometry collection is null");
    }
  }
}

