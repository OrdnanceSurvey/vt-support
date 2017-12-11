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

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;

/**
 * <p>Round each coordinate value to an integer.</p>
 * <p>Mapbox vector tiles have fixed precision. This filter can be useful for reducing precision to
 * the extent of a MVT.</p>
 */
public final class RoundingFilter implements CoordinateSequenceFilter {

  public static final RoundingFilter INSTANCE = new RoundingFilter();

  private RoundingFilter() { }

  @Override
  public void filter(CoordinateSequence seq, int index) {
    seq.setOrdinate(index, 0, Math.round(seq.getOrdinate(index, 0)));
    seq.setOrdinate(index, 1, Math.round(seq.getOrdinate(index, 1)));
  }

  @Override
  public boolean isDone() {
    return false;
  }

  @Override
  public boolean isGeometryChanged() {
    return true;
  }
}
