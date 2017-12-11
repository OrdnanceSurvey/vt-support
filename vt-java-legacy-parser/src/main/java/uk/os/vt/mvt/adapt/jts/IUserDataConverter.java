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

import uk.os.vt.mvt.VectorTile;
import uk.os.vt.mvt.build.MvtLayerProps;

/**
 * Processes a user data object, converts to MVT feature tags.
 */
public interface IUserDataConverter {

  /**
   * <p>Convert user data to MVT tags. The supplied user data may be null. Implementation
   * should update layerProps and optionally set the feature id.</p>
   * <p>SIDE EFFECT: The implementation may add tags to featureBuilder, modify layerProps, modify
   * userData.</p>
   *
   * @param userData       user object may contain values in any format; may be null
   * @param layerProps     properties global to the layer the feature belongs to
   * @param featureBuilder may be modified to contain additional tags
   */
  void addTags(Object userData, MvtLayerProps layerProps,
               VectorTile.Tile.Feature.Builder featureBuilder);
}
