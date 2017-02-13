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

package uk.os.vt.parsers;

import com.vividsolutions.jts.geom.Geometry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import no.ecc.vectortile.VectorTileDecoder;
import uk.os.vt.Entry;

public class VtParserElectronicChartCentre implements VtParser {

  @Override
  public List<Geometry> parse(Entry entry) throws IOException {
    final List<Geometry> result = new ArrayList<>();

    final List<VectorTileDecoder.Feature> features = getFeatures(entry.getVector());
    for (final VectorTileDecoder.Feature feature : features) {
      final Geometry geometry = feature.getGeometry();
      geometry.setUserData(feature.getAttributes());
      result.add(geometry);
    }
    return result;
  }

  /**
   * Get features.
   *
   * @param vector the bytes
   * @return the list of features
   * @throws IOException thrown if reading fails
   */
  public static List<VectorTileDecoder.Feature> getFeatures(byte[] vector) throws IOException {
    final VectorTileDecoder vtd = new VectorTileDecoder();
    final VectorTileDecoder.FeatureIterable erm = vtd.decode(vector);

    final List<VectorTileDecoder.Feature> features = new ArrayList<>();

    final Iterator<VectorTileDecoder.Feature> it = erm.iterator();
    while (it.hasNext()) {
      final VectorTileDecoder.Feature feature = it.next();
      features.add(feature);
      // System.out.println("Feature Type: " + feature.getGeometry().getClass().toString());
    }
    return features;
  }
}
