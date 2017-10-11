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

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import uk.os.vt.Entry;
import uk.os.vt.common.CompressUtil;

class FilesystemUtil {

  private static final long KILOBYTE = 1024;

  // only *nix considered
  private static final Pattern PATTERN = Pattern.compile("^.*/(\\d+)/(\\d+)/(\\d+)(.mvt|.pbf)?$");
  private static final int PATTERN_Z = 1;
  private static final int PATTERN_X = 2;
  private static final int PATTERN_Y = 3;

  private static final String MVT_FILE_EXTENSION = ".mvt";

  private FilesystemUtil() {}

  public static void addEntry(File baseDirectory, Entry entry, boolean useGzipCompression)
      throws IOException {
    // TODO establish if "limit tiles to 500K bytes" is raw size or
    // compressed size. Suspect former.

    final String relativePath = entry.getZoomLevel() + File.separator + entry.getColumn()
        + File.separator + entry.getRow() + MVT_FILE_EXTENSION;
    final File destination = new File(baseDirectory, relativePath);

    byte[] data;
    if (useGzipCompression) {
      final byte[] compressed = CompressUtil.getCompressedAsGzip(entry.getVector());
      data = compressed;
    } else {
      data = entry.getVector();
    }

    FileUtils.writeByteArrayToFile(destination, data);
  }

  public static Entry toEntry(File file) throws IOException {

    final boolean isValidMvtSize = file.length() <= 500 * KILOBYTE;
    if (!isValidMvtSize) {
      throw new IOException("Illegal mvt - file exceeds 500kb! " + file.getAbsolutePath());
    }

    final Matcher m = PATTERN.matcher(file.getAbsolutePath());
    if (m.matches()) {
      byte[] bytes = Files.readAllBytes(file.toPath());

      final boolean isCompressed = CompressUtil.isGzipStream(bytes);
      bytes = isCompressed ? CompressUtil.getUncompressedFromGzip(bytes) : bytes;

      final int z = Integer.parseInt(m.group(PATTERN_Z));
      final int x = Integer.parseInt(m.group(PATTERN_X));
      final int y = Integer.parseInt(m.group(PATTERN_Y));
      return new Entry(z, x, y, bytes);
    } else {
      throw new IllegalStateException("file does not match: " + file.getAbsolutePath());
    }
  }

  /**
   * @param path the root directory containing files to be emitted
   * @param depth offset, where 1 == data directory, 2 == zoom directory, 3 == row, 4 == column
   * @return a stream of file references to data structured as the Google tiling scheme.
   */
  public static Observable<File> getTiles(String path, int depth) {
    // programmer's note:
    // https://medium.com/we-are-yammer/converting-callback-async-calls-to-rxjava-ebc68bde5831
    // http://vlkan.com/blog/post/2016/07/20/rxjava-backpressure/
    // Essentially lots of way to skin this cat - e.g. onBackpressureBlock /
    // reactive pull

    return Flowable.create(new FlowableOnSubscribe<File>() {
      @Override
      public void subscribe(FlowableEmitter<File> subscriber) throws Exception {
        try {
          // warning: depth 4 is not really walking (semantics)
          walk(path, depth, subscriber);
          if (!subscriber.isCancelled()) {
            subscriber.onComplete();
          }
        } catch (final Exception ex) {
          if (!subscriber.isCancelled()) {
            subscriber.onError(ex);
          }
        }
      }
    }, BackpressureStrategy.BUFFER).toObservable();
  }

  public static Observable<File> getTiles(String path) {
    // programmer's note:
    // https://medium.com/we-are-yammer/converting-callback-async-calls-to-rxjava-ebc68bde5831
    // http://vlkan.com/blog/post/2016/07/20/rxjava-backpressure/
    // Essentially lots of way to skin this cat - e.g. onBackpressureBlock /
    // reactive pull

    return Flowable.create(new FlowableOnSubscribe<File>() {
      @Override
      public void subscribe(FlowableEmitter<File> subscriber) throws Exception {
        try {
          walk(path, 1, subscriber);
          if (!subscriber.isCancelled()) {
            subscriber.onComplete();
          }
        } catch (final Exception ex) {
          if (!subscriber.isCancelled()) {
            subscriber.onError(ex);
          }
        }
      }
    }, BackpressureStrategy.BUFFER).toObservable();
  }

  public static int[] toZxy(File file) {
    final Matcher m = PATTERN.matcher(file.getAbsolutePath());
    if (m.matches()) {

      final int z = Integer.parseInt(m.group(PATTERN_Z));
      final int x = Integer.parseInt(m.group(PATTERN_X));
      final int y = Integer.parseInt(m.group(PATTERN_Y));
      return new int[] {z, x, y};
    } else {
      throw new IllegalStateException("file does not match: " + file.getAbsolutePath());
    }
  }

  /**
   * Walk the directory tree.
   *
   * @param path the starting path
   * @param depth offset, where 1 == data directory, 2 == zoom directory, 3 == row, 4 == column
   * @param subscriber to provide feedback and terminate according to
   */
  private static void walk(String path, int depth, FlowableEmitter<File> subscriber) {

    final File root = new File(path);

    File[] list;
    switch (depth) {
      case 1:
      case 2:
        list = root.listFiles(File::isDirectory);
        break;
      case 3:
        list = root.listFiles(file -> file.isFile() && PATTERN.matcher(file.toString()).matches());
        break;
      case 4:
        // consider removing out of walk
        list = getSingleMatchOrEmpty(root);
        break;
      default:
        throw new IllegalArgumentException("unsupported depth");
    }

    if (list == null) {
      return;
    }

    for (final File f : list) {
      if (subscriber.isCancelled()) {
        return;
      }

      if (f.isDirectory()) {
        walk(f.getAbsolutePath(), depth + 1, subscriber);
      } else {
        // move close as possible - but no guarantee!
        final boolean isSubscribed = !subscriber.isCancelled();
        if (isSubscribed) {
          subscriber.onNext(f);
        }
      }
    }
  }

  /**
   *
   * @param fullCoordinate file path, e.g. /0/1/2
   * @return file matching either /0/1/2.mvt or /0/1/2.pbf else empty
   */
  private static File[] getSingleMatchOrEmpty(File fullCoordinate) {
    File[] possible = new File[]{
        new File(fullCoordinate.getAbsoluteFile() + ".mvt"),
        // should remove .pbf as external put mandates .mvt
        new File(fullCoordinate.getAbsoluteFile() + ".pbf")
    };

    for (File f : possible) {
      if (PATTERN.matcher(f.toString()).matches() && f.exists()) {
        return new File[]{f};
      }
    }
    return new File[]{};
  }
}
