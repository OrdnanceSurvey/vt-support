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

import com.google.common.primitives.Ints;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.BiFunction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.os.vt.Entry;
import uk.os.vt.Metadata;
import uk.os.vt.MetadataProvider;
import uk.os.vt.Storage;

public final class StorageImpl implements Storage, MetadataProvider {

  private static final int[] UNDEFINED_ZXY = new int[]{};

  private final File directory;
  private final boolean gzipEnabled;

  private static final Logger LOG = LoggerFactory.getLogger(StorageImpl.class);

  @Override
  public Single<Metadata> generateDefault() {
    final int[] zMinMax = getMaxMin(tileFilenames(directory));
    return FilesystemUtil
        .getTiles(new File(directory, String.valueOf(zMinMax[1])).getAbsolutePath(), 2)
        .map(FilesystemUtil::toZxy).reduce(UNDEFINED_ZXY, new BiFunction<int[], int[], int[]>() {
          @Override
          public int[] apply(int[] aa, int[] bb) throws Exception {
            return aa == UNDEFINED_ZXY ? (bb == UNDEFINED_ZXY ? UNDEFINED_ZXY : bb)
                : new int[] {Math.max(aa[0], bb[0]), Math.max(aa[1], bb[1]),
                Math.max(aa[2], bb[2])};
          }
        }).map(zxy -> {
          if (zxy == UNDEFINED_ZXY) {
            return new Metadata.Builder().build();
          }
          // TODO should be able to translate tile coordinates to
          // bounds shortly!
          return new Metadata.Builder().setMinZoom(zMinMax[0]).setMaxZoom(zMinMax[1]).build();
        }).toObservable().singleOrError();
  }

  @Override
  public Disposable putMetadata(Single<Metadata> metadata) {
    return metadata.subscribe(m -> {
      final File file = new File(directory, "config.json");
      try {
        FileUtils.writeStringToFile(file, m.getTileJson().toString(), "UTF-8");
      } catch (final IOException ex) {
        LOG.error("problem writing metadata", ex);
      }
    });
  }

  private StorageImpl(File directory, boolean gzipEnabled) {
    this.directory = directory;
    this.gzipEnabled = gzipEnabled;
  }

  @Override
  public void close() throws Exception {
    // no resources to free
  }

  @Override
  public Observable<Entry> getEntries() {
    return getEntries(directory);
  }

  private static Observable<Entry> getEntries(File directory) {
    return FilesystemUtil.getTiles(directory.getPath()).map(file -> {
      try {
        return FilesystemUtil.toEntry(file);
      } catch (final IOException ex) {
        throw Exceptions.propagate(ex);
      }
    });
  }

  @Override
  public Observable<Entry> getEntries(int zoom) {
    return FilesystemUtil.getTiles(directory.getPath() + File.separator + zoom, 2).map(file -> {
      try {
        return FilesystemUtil.toEntry(file);
      } catch (final IOException ex) {
        throw Exceptions.propagate(ex);
      }
    });
  }

  @Override
  public Observable<Entry> getEntry(int zoom, int col, int row) {
    return FilesystemUtil.getTiles(
        directory.getPath() + File.separator + zoom + File.separator + col + File.separator + row,
        4).map(file -> {
          try {
            return FilesystemUtil.toEntry(file);
          } catch (final IOException ex) {
            throw Exceptions.propagate(ex);
          }
        });
  }

  @Override
  public Observable<Integer> getMaxZoomLevel() {
    return Observable.defer(() -> {
      final int[] zoomLevels = getZoomLevels();
      return zoomLevels.length > 0 ? Observable.just(zoomLevels[zoomLevels.length - 1])
          : Observable.empty();
    });
  }

  @Override
  public Observable<Integer> getMinZoomLevel() {
    return Observable.defer(() -> {
      final int[] zoomLevels = getZoomLevels();
      return zoomLevels.length > 0 ? Observable.just(zoomLevels[0]) : Observable.empty();
    });
  }

  @Override
  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
  public void putEntries(Observable<Entry> entries) {
    entries.subscribe(entry -> {
      try {
        FilesystemUtil.addEntry(directory, entry, gzipEnabled);
      } catch (final IOException ex) {
        Exceptions.propagate(ex);
      }
    });
  }

  @Override
  public Observable<Metadata> getMetadata() {
    return Observable.defer(() -> {
      final File metadata = new File(directory, "config.json");
      try {
        if (metadata.exists()) {
          final String raw = FileUtils.readFileToString(metadata);
          final Metadata result = new Metadata.Builder().setTileJson(raw).build();
          return Observable.just(result);
        }
      } catch (final IOException ex) {
        throw Exceptions.propagate(ex);
      }
      return Observable.empty();
    });
  }

  private int[] getMaxMin(String[] value) {
    int max = Integer.MIN_VALUE;
    int min = Integer.MAX_VALUE;
    for (int i = 0; i < value.length; i++) {
      final int d = Integer.parseInt(value[i]);
      max = Math.max(max, d);
      min = Math.min(min, d);
    }
    return new int[] {min, max};
  }

  private int[] toIntArray(String[] value) {
    final int[] result = new int[value.length];
    for (int i = 0; i < value.length; i++) {
      result[i] = Integer.parseInt(value[i]);
    }
    return result;
  }

  private static String[] tileFilenames(File directory) {
    return directory.list(new AbstractFileFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.matches("\\d*");
      }
    });
  }

  private int[] getZoomLevels() {
    final List<Integer> zoomLevels = new ArrayList<>();
    final File[] files = directory.listFiles();

    if (files == null) {
      return new int[]{};
    }

    final Pattern pattern = Pattern.compile("^([0-9]|1[0-9]|2[0-2])$");
    for (final File file : files) {
      final String fileName = file.getName();
      final Matcher matcher = pattern.matcher(fileName);
      if (matcher.matches()) {
        final int value = Integer.parseInt(matcher.group());
        zoomLevels.add(value);
      }
    }
    final int[] result = Ints.toArray(zoomLevels);
    Arrays.sort(result);
    return result;
  }

  public static final class Builder {

    private final File directory;
    private boolean createIfNotExist;
    private boolean gzipEnabled = true;

    public Builder(String directory) throws IOException {
      this.directory = new File(directory);
    }

    public Builder(File directory) throws IOException {
      this.directory = directory;
    }

    public Builder createIfNotExist() {
      createIfNotExist = true;
      return this;
    }

    /**
     * Set gzip compression.
     *
     * @param gzipEnabled set true if individual files should be gzipped, default.
     * @return this builder
     */
    public Builder setGzipCompression(boolean gzipEnabled) {
      this.gzipEnabled = gzipEnabled;
      return this;
    }

    /**
     * Build the storage.
     *
     * @return the tile storage
     * @throws IOException thrown on IO error
     */
    public StorageImpl build() throws IOException {
      if (createIfNotExist && !directory.exists()) {
        LOG.info(String.format("making directory '%s'", directory));
        boolean isSuccess = directory.mkdirs();
        if (!isSuccess) {
          throw new IOException(String.format("could not create directory: '%s'", directory));
        }
      }

      if (!directory.isDirectory()) {
        throw new IOException(String.format("not a directory: '%s'", directory));
      }
      return new StorageImpl(directory, gzipEnabled);
    }
  }
}
