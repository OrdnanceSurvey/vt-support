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

package uk.os.vt;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import rx.Single;

public class TileLayerTest {

  @Test
  public void testSimple() {
    String attribution = "Ordnance Survey";
    int maxZoom = 19;
    int minZoom = 5;
    String name = "Ladybirds";

    Storage mockedStorage = getMockedStorage();

    TileLayer.Builder builder = new TileLayer.Builder()
        .setAttribution(attribution)
        .setMaxZoom(maxZoom)
        .setMinZoom(minZoom)
        .setName(name)
        .setStorage(mockedStorage);

    builder.build();
  }

  private Storage getMockedStorage() {
    Storage mockedStorage = mock(Storage.class);
    when(mockedStorage.generateDefault())
        .then(invocation -> Single.just(new Metadata.Builder().build()));
    return mockedStorage;
  }
}
