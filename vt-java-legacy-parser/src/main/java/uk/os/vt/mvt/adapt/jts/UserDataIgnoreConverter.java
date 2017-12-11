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
 * Ignores user data, does not take any action.
 *
 * @see IUserDataConverter
 */
public final class UserDataIgnoreConverter implements IUserDataConverter {
  @Override
  public void addTags(Object userData, MvtLayerProps layerProps,
                      VectorTile.Tile.Feature.Builder featureBuilder) {
  }
}
