/*
 * Copyright (C) 2016 Ordnance Survey
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

package uk.os.vt.fluent;

import java.util.ArrayList;
import java.util.Collection;
import org.locationtech.jts.geom.Geometry;
import uk.os.vt.Metadata;

/**
 * This is a layer is intended for WGS84 geometries.
 */
public class FluentLayer {

  private final Collection<Geometry> geometries;
  private Metadata.Layer metadata;

  public FluentLayer(String name) {
    this(new Metadata.Layer.Builder().setId(name).build(), new ArrayList<>());
  }

  public FluentLayer(Metadata.Layer metadata, Collection<Geometry> geometries) {
    this.metadata = metadata;
    this.geometries = geometries;
  }

  public Metadata.Layer getMetadata() {
    return metadata;
  }

  public FluentLayer addAttribute(String attribute, String description) {
    metadata = new Metadata.Layer.Builder(metadata).addField(attribute, description).build();
    return this;
  }

  public FluentLayer setMaxZoom(int maxZoom) {
    metadata = new Metadata.Layer.Builder(metadata).setMaxZoom(maxZoom).build();
    return this;
  }

  public FluentLayer setMinZoom(int minZoom) {
    metadata = new Metadata.Layer.Builder(metadata).setMinZoom(minZoom).build();
    return this;
  }

  public FluentLayer add(Geometry geometry) {
    geometries.add(geometry);
    return this;
  }

  public FluentLayer addAll(Collection<Geometry> geometries) {
    this.geometries.addAll(geometries);
    return this;
  }

  public Collection<Geometry> getGeometries() {
    return new ArrayList<>(geometries);
  }

  public String getName() {
    return metadata.getId();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    FluentLayer that = (FluentLayer) obj;

    if (geometries != null ? !geometries.equals(that.geometries) : that.geometries != null) {
      return false;
    }
    return metadata != null ? metadata.equals(that.metadata) : that.metadata == null;
  }

  @Override
  public int hashCode() {
    int result = geometries != null ? geometries.hashCode() : 0;
    result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "FluentLayer{"
        + "geometries=" + geometries
        + ", metadata=" + metadata
        + '}';
  }
}
