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

package uk.os.vt.filesystem;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import rx.observers.TestSubscriber;

import uk.os.vt.Entry;
import uk.os.vt.Storage;

public class StorageImplCannedDataTest {

  private static final int GENEROUS_TIMEOUT_IN_SECONDS = 5;
  private static final String SINGLE_ZOOM_LEVEL_FILESYSTEM = "z5";
  private static final String MULTIPLE_ZOOM_LEVEL_FILESYSTEM = "z0to4";

  @Test
  public void testSingleZoomLevelTotalEntries() throws IOException {
    verifyTotalEntries(3, getStorage(SINGLE_ZOOM_LEVEL_FILESYSTEM));
  }

  @Test
  public void testSingleZoomLevelTotalEntriesWhenRequestingSpecificZoom() throws IOException {
    final StorageImpl storage = getStorage(SINGLE_ZOOM_LEVEL_FILESYSTEM);

    final int z3 = 5;
    final int z3ExpectedTotal = 3;
    verifyTotalEntries(z3, z3ExpectedTotal, storage);
  }

  @Test
  public void testSingleZoomLevelTotalEntriesForNonExistentZoom() throws IOException {
    final StorageImpl storage = getStorage(SINGLE_ZOOM_LEVEL_FILESYSTEM);

    final int z6 = 6;
    final int z6ExpectedTotal = 0;
    verifyTotalEntries(z6, z6ExpectedTotal, storage);
  }

  @Test
  public void testSingleZoomLevelMaxZoom() throws IOException {
    final StorageImpl storage = getStorage(SINGLE_ZOOM_LEVEL_FILESYSTEM);
    verifyMaxZoomLevel(5, storage);
  }

  @Test
  public void testSingleZoomLevelMinZoom() throws IOException {
    final StorageImpl storage = getStorage(SINGLE_ZOOM_LEVEL_FILESYSTEM);
    verifyMinZoomLevel(5, storage);
  }

  @Test
  public void testMultipleZoomLevelTotalEntries() throws IOException {
    verifyTotalEntries(10, getStorage(MULTIPLE_ZOOM_LEVEL_FILESYSTEM));
  }

  @Test
  public void testMultipleZoomLevelTotalEntriesAtSpecificZoomLevels() throws IOException {
    final StorageImpl storage = getStorage(MULTIPLE_ZOOM_LEVEL_FILESYSTEM);

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
    final StorageImpl storage = getStorage(MULTIPLE_ZOOM_LEVEL_FILESYSTEM);

    final int z6 = 6;
    final int z6ExpectedTotal = 0;
    verifyTotalEntries(z6, z6ExpectedTotal, storage);
  }

  @Test
  public void testMultipleZoomLevelMaxZoom() throws IOException {
    final StorageImpl storage = getStorage(MULTIPLE_ZOOM_LEVEL_FILESYSTEM);
    verifyMaxZoomLevel(4, storage);
  }

  @Test
  public void testMultipleZoomLevelMinZoom() throws IOException {
    final StorageImpl storage = getStorage(MULTIPLE_ZOOM_LEVEL_FILESYSTEM);
    verifyMinZoomLevel(0, storage);
  }

  private static File getFile(String file) throws IOException {
    final URL result = StorageImplCannedDataTest.class.getClassLoader().getResource(file);
    if (result == null) {
      throw new IllegalStateException("Problem accessing resources!");
    }
    try {
      final Path path = Paths.get(result.toURI());
      return path.toFile();
    } catch (final Exception ex) {
      throw new IOException("cannot return resource: " + file, ex);
    }
  }

  private static StorageImpl getStorage(String directory) throws IOException {
    final File file = getFile(directory);
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
