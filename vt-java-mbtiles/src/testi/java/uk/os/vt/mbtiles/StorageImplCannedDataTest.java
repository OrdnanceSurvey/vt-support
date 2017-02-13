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

package uk.os.vt.mbtiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.observers.TestSubscriber;

import uk.os.vt.Entry;
import uk.os.vt.Metadata;
import uk.os.vt.Storage;

public class StorageImplCannedDataTest {

  private static final int GENEROUS_TIMEOUT_IN_SECONDS = 5;
  private static final Logger LOG =
      LoggerFactory.getLogger(StorageImplCannedDataTest.class.getSimpleName());
  private static final String SINGLE_ZOOM_LEVEL_MBTILE =
      "Boundary-line-historic-counties_regionz5.mbtiles";
  private static final String MULTIPLE_ZOOM_LEVEL_MBTILE =
      "Boundary-line-historic-counties_regionz0-4.mbtiles";

  @Test
  public void testMetadata() throws IOException {
    final StorageImpl storage = getStorage(SINGLE_ZOOM_LEVEL_MBTILE);
    final Metadata metadata = storage.generateDefault().toBlocking().value();
    assertNotNull(metadata);

    final int expectedMaxZoom = 5;
    final int expectedMinZoom = 5;
    final int actualMaxZoom = metadata.getMaxZoom();
    final int actualMinZoom = metadata.getMinZoom();

    assertEquals(expectedMaxZoom, actualMaxZoom);
    assertEquals(expectedMinZoom, actualMinZoom);

    final String expectedAttribution =
        "<a href=\"https://www.ordnancesurvey.co.uk/business-and-government/licensing/using-creating-data-with-os-products/os-opendata.html\" target=\"_blank\">© Ordnance Survey</a>";
    final String actualAttribution = metadata.getAttribution();
    assertEquals(expectedAttribution, actualAttribution);

    final double actualNorth = metadata.getBoundsNorth();
    final double actualEast = metadata.getBoundsEast();
    final double actualSouth = metadata.getBoundsSouth();
    final double actualWest = metadata.getBoundsWest();

    final double expectedNorth = 60.860766;
    final double expectedEast = 1.768912;
    final double expectedSouth = 49.864636;
    final double expectedWest = -8.650007;

    final double delta = 0.000001;
    assertEquals(expectedNorth, actualNorth, delta);
    assertEquals(expectedEast, actualEast, delta);
    assertEquals(expectedSouth, actualSouth, delta);
    assertEquals(expectedWest, actualWest, delta);
  }

  @Test
  public void testSingleZoomLevelTotalEntries() throws IOException {
    verifyTotalEntries(3, getStorage(SINGLE_ZOOM_LEVEL_MBTILE));
  }

  @Test
  public void testSingleZoomLevelTotalEntriesWhenRequestingSpecificZoom() throws IOException {
    final StorageImpl storage = getStorage(SINGLE_ZOOM_LEVEL_MBTILE);

    final int z3 = 5;
    final int z3ExpectedTotal = 3;
    verifyTotalEntries(z3, z3ExpectedTotal, storage);
  }

  @Test
  public void testSingleZoomLevelTotalEntriesForNonExistentZoom() throws IOException {
    final StorageImpl storage = getStorage(SINGLE_ZOOM_LEVEL_MBTILE);

    final int z6 = 6;
    final int z6ExpectedTotal = 0;
    verifyTotalEntries(z6, z6ExpectedTotal, storage);
  }

  @Test
  public void testSingleZoomLevelMaxZoom() throws IOException {
    final StorageImpl storage = getStorage(SINGLE_ZOOM_LEVEL_MBTILE);
    verifyMaxZoomLevel(5, storage);
  }

  @Test
  public void testSingleZoomLevelMinZoom() throws IOException {
    final StorageImpl storage = getStorage(SINGLE_ZOOM_LEVEL_MBTILE);
    verifyMinZoomLevel(5, storage);
  }

  @Test
  public void testMultipleZoomLevelTotalEntries() throws IOException {
    verifyTotalEntries(10, getStorage(MULTIPLE_ZOOM_LEVEL_MBTILE));
  }

  @Test
  public void testMultipleZoomLevelTotalEntriesAtSpecificZoomLevels() throws IOException {
    final StorageImpl storage = getStorage(MULTIPLE_ZOOM_LEVEL_MBTILE);

    final int z0 = 0;
    final int z0ExpectedTotal = 1;
    verifyTotalEntries(z0, z0ExpectedTotal, storage);

    final int z1 = 1;
    final int z1ExpectedTotal = 2;
    verifyTotalEntries(z1, z1ExpectedTotal, storage);

    final int z2 = 2;
    final int z2ExpectedTotal = 2;
    verifyTotalEntries(z2, z2ExpectedTotal, storage);

    final int z3 = 3;
    final int z3ExpectedTotal = 2;
    verifyTotalEntries(z3, z3ExpectedTotal, storage);

    final int z4 = 4;
    final int z4ExpectedTotal = 3;
    verifyTotalEntries(z4, z4ExpectedTotal, storage);
  }

  @Test
  public void testMultipleZoomLevelTotalEntriesForNonExistentZoom() throws IOException {
    final StorageImpl storage = getStorage(MULTIPLE_ZOOM_LEVEL_MBTILE);

    final int z6 = 6;
    final int z6ExpectedTotal = 0;
    verifyTotalEntries(z6, z6ExpectedTotal, storage);
  }

  @Test
  public void testMultipleZoomLevelMaxZoom() throws IOException {
    final StorageImpl storage = getStorage(MULTIPLE_ZOOM_LEVEL_MBTILE);
    verifyMaxZoomLevel(4, storage);
  }

  @Test
  public void testMultipleZoomLevelMinZoom() throws IOException {
    final StorageImpl storage = getStorage(MULTIPLE_ZOOM_LEVEL_MBTILE);
    verifyMinZoomLevel(0, storage);
  }

  private static File getFile(String file) {
    final URL result = StorageImplCannedDataTest.class.getClassLoader().getResource(file);

    final boolean isProblemGettingResource = result == null && file != null;
    if (isProblemGettingResource) {
      throw new IllegalStateException("problem getting resource: " + file);
    }

    try {
      final Path path = Paths.get(result.toURI());
      return path.toFile();
    } catch (final Exception ex) {
      LOG.error("cannot return resource: " + file);
      return null;
    }
  }

  private static StorageImpl getStorage(String testMbTile) throws IOException {
    final File file = getFile(testMbTile);
    return new StorageImpl.Builder(file).build();
  }

  private void verifyMaxZoomLevel(int expectedMaxZoomLevel, Storage storage) {
    final TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
    storage.getMaxZoomLevel().subscribe(testSubscriber);
    testSubscriber.awaitTerminalEvent(GENEROUS_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
    testSubscriber.assertValue(expectedMaxZoomLevel);
  }

  private void verifyMinZoomLevel(int expectedMaxZoomLevel, Storage storage) {
    final TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
    storage.getMinZoomLevel().subscribe(testSubscriber);
    testSubscriber.awaitTerminalEvent(GENEROUS_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
    testSubscriber.assertValue(expectedMaxZoomLevel);
  }

  private static void verifyTotalEntries(int expectedCount, Storage storage) {
    final List<Entry> items =
        storage.getEntries().take(5, TimeUnit.SECONDS).toList().toBlocking().first();

    final int actualCount = items.size();
    assertEquals(expectedCount, actualCount);
  }

  private static void verifyTotalEntries(int zoomLevel, int expectedCount, Storage storage) {
    final List<Entry> items =
        storage.getEntries(zoomLevel).take(5, TimeUnit.SECONDS).toList().toBlocking().first();

    final int actualCount = items.size();
    assertEquals(expectedCount, actualCount);
  }
}
