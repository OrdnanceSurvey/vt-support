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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JTS model of a Mapbox Vector Tile.
 */
public class JtsMvt {

  /**
   * Map layers by name.
   */
  private final Map<String, JtsLayer> layersByName;

  /**
   * Create an empty MVT.
   */
  public JtsMvt() {
    this(Collections.<JtsLayer>emptyList());
  }

  /**
   * Create MVT with single layer.
   *
   * @param layer single MVT layer
   */
  public JtsMvt(JtsLayer layer) {
    this(Collections.singletonList(layer));
  }

  /**
   * Create MVT with the provided layers.
   *
   * @param layers multiple MVT layers
   */
  public JtsMvt(JtsLayer... layers) {
    this(new ArrayList<>(Arrays.asList(layers)));
  }

  /**
   * Create a MVT with the provided layers.
   *
   * @param layers multiple MVT layers
   */
  public JtsMvt(Collection<JtsLayer> layers) {

    // Linked hash map to preserve ordering
    layersByName = new LinkedHashMap<>(layers.size());

    for (JtsLayer nextLayer : layers) {
      layersByName.put(nextLayer.getName(), nextLayer);
    }
  }

  /**
   * Get the layer by the given name.
   *
   * @param name layer name
   * @return layer with matching name, or null if none exists
   */
  public JtsLayer getLayer(String name) {
    return layersByName.get(name);
  }

  /**
   * Get all layers within the vector tile mapped by name.
   *
   * @return mapping of layer name to layer
   */
  public Map<String, JtsLayer> getLayersByName() {
    return layersByName;
  }

  /**
   * Get get all layers within the vector tile.
   *
   * @return insertion-ordered collection of layers
   */
  public Collection<JtsLayer> getLayers() {
    return layersByName.values();
  }

  @Override
  public String toString() {
    return "JtsMvt{"
        + "layersByName=" + layersByName
        + "}";
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    JtsMvt jtsMvt = (JtsMvt) object;

    return layersByName.equals(jtsMvt.layersByName);
  }

  @Override
  public int hashCode() {
    return layersByName.hashCode();
  }
}
