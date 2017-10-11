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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.reactivex.Observable;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.os.vt.Entry;

public class FilesystemUtilTest {

  @Rule
  public final TemporaryFolder testFolder = new TemporaryFolder();

  @Test
  public void addEntryTest() throws IOException {
    // GIVEN
    final File baseDirectory = testFolder.newFolder("testdir");
    System.out.println("baseDir: " + baseDirectory);
    final byte[] bytes = getGarbageBytes();
    final Entry entry = new Entry(0, 0, 0, bytes);
    final boolean useGzipCompression = false;

    // WHEN
    FilesystemUtil.addEntry(baseDirectory, entry, useGzipCompression);

    final File expectedFile = new File(testFolder.getRoot() + "/testdir/0/0/0.mvt");
    System.out.println("expectedFile: " + expectedFile);
    assertTrue(expectedFile.exists());
    assertEquals(bytes.length, expectedFile.length());
  }

  @Test
  public void addEntryCompressedTest() throws IOException {
    // GIVEN
    final File baseDirectory = testFolder.newFolder("testdir");
    System.out.println("baseDir: " + baseDirectory);
    final byte[] bytes = getGarbageBytes();
    final Entry entry = new Entry(0, 0, 0, bytes);
    final boolean useGzipCompression = true;

    // WHEN
    FilesystemUtil.addEntry(baseDirectory, entry, useGzipCompression);

    final File expectedFile = new File(testFolder.getRoot() + "/testdir/0/0/0.mvt");
    System.out.println("expectedFile: " + expectedFile);
    assertTrue(expectedFile.exists());
    assertEquals(24, expectedFile.length());
  }

  @Test
  public void readEntryTest() throws IOException {
    // GIVEN
    final File baseDirectory = testFolder.newFolder("testdir");
    System.out.println("baseDir: " + baseDirectory);
    final byte[] bytes = getGarbageBytes();
    final Entry entry = new Entry(3, 4, 5, bytes);
    final boolean useGzipCompression = false;
    FilesystemUtil.addEntry(baseDirectory, entry, useGzipCompression);
    final File inputFile = new File(testFolder.getRoot() + "/testdir/3/4/5.mvt");
    System.out.println("inputFile: " + inputFile);

    // WHEN
    final Entry result = FilesystemUtil.toEntry(inputFile);

    // THEN
    assertNotNull(result);
    assertEquals(3, result.getZoomLevel());
    assertEquals(4, result.getColumn());
    assertEquals(5, result.getRow());
    assertArrayEquals(getGarbageBytes(), result.getVector());
  }

  @Test
  public void getNoEntryTest() throws IOException {
    // GIVEN
    final File baseDirectory = testFolder.newFolder("testdir");

    // WHEN
    final Observable<File> result = FilesystemUtil.getTiles(baseDirectory.getPath() + "/4/0/0", 4);
    final List<File> results = result.toList().blockingGet();

    // THEN
    assertNotNull(results);
    assertEquals(0, results.size());
  }

  @Test
  public void getTilesBasedirTest() throws IOException {
    // GIVEN
    final File baseDirectory = testFolder.newFolder("testdir");
    System.out.println("baseDir: " + baseDirectory);
    final byte[] bytes = getGarbageBytes();
    final boolean useGzipCompression = false;
    final Entry entry1 = new Entry(3, 4, 5, bytes);
    FilesystemUtil.addEntry(baseDirectory, entry1, useGzipCompression);
    final Entry entry2 = new Entry(4, 5, 6, bytes);
    FilesystemUtil.addEntry(baseDirectory, entry2, useGzipCompression);
    final Entry entry3 = new Entry(5, 6, 7, bytes);
    FilesystemUtil.addEntry(baseDirectory, entry3, useGzipCompression);

    // WHEN
    final Observable<File> result = FilesystemUtil.getTiles(baseDirectory.getPath());
    final List<File> results = result.toList().blockingGet();
    System.out.println("results.get(0): " + results.get(0));

    // THEN
    assertNotNull(results);
    assertEquals(3, results.size());
    assertEquals(getGarbageBytes().length, results.get(0).length());
  }

  @Test
  public void getTilesZoomdirTest() throws IOException {
    // GIVEN
    final File baseDirectory = testFolder.newFolder("testdir");
    System.out.println("baseDir: " + baseDirectory);
    final byte[] bytes = getGarbageBytes();
    final boolean useGzipCompression = false;
    final Entry entry1 = new Entry(3, 4, 5, bytes);
    FilesystemUtil.addEntry(baseDirectory, entry1, useGzipCompression);
    final Entry entry2 = new Entry(4, 5, 6, bytes);
    FilesystemUtil.addEntry(baseDirectory, entry2, useGzipCompression);
    final Entry entry3 = new Entry(5, 6, 7, bytes);
    FilesystemUtil.addEntry(baseDirectory, entry3, useGzipCompression);

    // WHEN
    final Observable<File> result = FilesystemUtil.getTiles(baseDirectory.getPath() + "/4", 2);
    final List<File> results = result.toList().blockingGet();
    System.out.println("results.get(0): " + results.get(0));

    // THEN
    assertNotNull(results);
    assertEquals(1, results.size());
    assertEquals(getGarbageBytes().length, results.get(0).length());
  }

  private byte[] getGarbageBytes() {
    try {
      return "test".getBytes("UTF-8");
    } catch (UnsupportedEncodingException uee) {
      return new byte[]{};
    }
  }
}
