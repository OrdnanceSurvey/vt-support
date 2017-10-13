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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import io.reactivex.Observable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import uk.os.vt.Entry;
import uk.os.vt.Metadata;
import uk.os.vt.Storage;

@RunWith(MockitoJUnitRunner.class)
public class DataSourceTest {

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  @Test(expected = IllegalStateException.class)
  public void testAddGeometryWithoutLayer() {
    Storage storage = getEmptyStorage();
    DataSource dataSource = new DataSource(storage);
    dataSource.add(createPoint("1", "First Point", new double[]{0, 0})).commit();
  }

  @Test
  public void testEmptyCommitDoesNotUseStorage() {
    Storage storage = mock(Storage.class);
    when(storage.getMetadata()).then(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        return Observable.just(new Metadata.Builder().build());
      }
    });
    DataSource dataSource = new DataSource(storage, new Metadata.Builder().build());
    dataSource
        .using("layer")
        .add(createPoint("1", "First Point", new double[]{0, 0}))
        .commit();

    verify(storage, Mockito.times(1)).putEntries(any());
    dataSource.commit();
    verify(storage, Mockito.times(1)).putEntries(any());
  }

  @Test
  public void testSingleEntry() {
    Storage storage = getEmptyStorage();

    Metadata metadata = new Metadata.Builder()
        .addLayer(new Metadata.Layer.Builder().setId("layer").setMinZoom(0).setMaxZoom(0).build())
        .build();
    DataSource dataSource = new DataSource(storage, metadata);
    dataSource
        .using("layer")
        .add(createPoint("1", "First Point", new double[]{0, 0}))
        .commit();

    // we only expect a single entry to be written to storage (one point on single global tile)
    final int expectedEntries = 1;

    verify(storage).putEntries(argThat(new ArgumentMatcher<Observable<Entry>>() {
      @Override
      public boolean matches(Observable<Entry> argument) {
        return expectedEntries == argument.count().blockingGet();
      }
    }));
  }

  static Point createPoint(String id, String name, double[] latlon) {
    return createPointXy(id, name, new double[]{latlon[1], latlon[0]});
  }

  static Point createPointXy(String id, String name, double[] coordinate) {
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put("id", id.hashCode());
    attributes.put("name", name);

    Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(coordinate[0], coordinate[1]));
    point.setUserData(attributes);
    return point;
  }

  private Storage getEmptyStorage() {
    Storage storage = mock(Storage.class);
    when(storage.getMetadata()).then(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        return Observable.just(new Metadata.Builder().build());
      }
    });
    when(storage.getEntry(anyInt(), anyInt(), anyInt())).thenReturn(Observable.empty());
    return storage;
  }

}
