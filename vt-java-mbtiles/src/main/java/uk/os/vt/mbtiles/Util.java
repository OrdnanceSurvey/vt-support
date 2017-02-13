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

import com.github.davidmoten.rx.jdbc.Database;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;

final class Util {

  private static final Logger LOG = LoggerFactory.getLogger(Util.class.getSimpleName());
  private static final int STATEMENT_QUERY_TIMEOUT_IN_SECONDS = 30;
  private static final int BUFFER_SIZE_IN_BYTES = 4096;

  private Util() {}

  /**
   * Provide a freshly initialized mbtiles file.
   *
   * @param file to create and apply mbtiles SQL
   * @throws IOException thrown on IO error
   */
  protected static void initialise(File file) throws IOException {
    LOG.info("initializing: " + file.getAbsolutePath());
    if (file.exists() && !file.delete()) {
      throw new IOException("initialization failure - cannot delete file " + file);
    }
    legacyMaker(file);
  }

  private static Connection getConnection(File file) throws SQLException {
    return getConnection(file.getAbsolutePath());
  }

  private static Connection getConnection(String fileLocation) throws SQLException {
    LOG.info("DB file location " + fileLocation);
    return printMetadata(DriverManager.getConnection("jdbc:sqlite:" + fileLocation));
  }

  /**
   * Legacy from file.
   *
   * <p>Note: please see RxJDBC DatabaseCreator:62 with:
   * public static void createDatabase(Connection c) {
   *
   * @param file the file
   * @throws IOException thrown on IO error
   */
  private static void legacyMaker(File file) throws IOException {
    Connection connection = null;
    try {
      connection = getConnection(file);
      final URL url = Resources.getResource("mbtiles_schema.sql");
      final String sql = Resources.toString(url, Charsets.UTF_8);

      final Statement statement = connection.createStatement();
      statement.setQueryTimeout(STATEMENT_QUERY_TIMEOUT_IN_SECONDS);
      statement.executeUpdate(sql); // replaced statement.execute(text); because multi statement SQL
      statement.close();
    } catch (IOException | SQLException ex) {
      throw new IOException("cannot produce a valid MBTiles file", ex);
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (final SQLException ex) {
        // connection close failed.
        LOG.error("cannot close connection", ex);
      }
    }
  }

  // TODO investigate issues with RX lib / SQLite
  // FYI:
  // https://github.com/davidmoten/rxjava-jdbc/commit/ce3369446ca1880afe2ec62a329db198e656d954#diff-8a68d79b6b3052d591d778f956384cf9
  private static void modernMaker(File file) throws IOException {
    Connection connection = null;
    Database db = null;
    InputStream is = null;
    try {
      connection = getConnection(file);
      db = Database.from(connection);

      localResourcePrintProofAllIsOk();

      // TODO - replace mbtiles_schema_simple.sql with the full mbtiles script
      is = StorageImpl.class.getResourceAsStream("/mbtiles_schema_simple.sql");

      final Observable<Integer> create =
          db.run(is, ";");
      final Observable<Integer> count =
          db.select("select * from map").dependsOn(create).getAs(String.class).count();
      assertIs(0, count);
    } catch (final SQLException ex) {
      throw new IOException("cannot produce valid MBTiles file", ex);
    } finally {
      if (db != null) {
        // contract does not mention any thrown exceptions
        db.close();
      }
      if (connection != null) {
        try {
          connection.close();
        } catch (final SQLException ex) {
          LOG.error("problem closing DB connection", ex);
        }
      }
      if (is != null) {
        IOUtils.closeQuietly(is);
      }
    }
  }

  // TODO remove when issues ironed out
  private static void localResourcePrintProofAllIsOk() throws IOException {
    final Charset charset = StandardCharsets.UTF_8;
    final String content = new String(readResoureStream("/mbtiles_schema_simple.sql"), charset);
    final String[] lines = content.split("\\n");

    System.out.println("whoop: ");
    for (final String line : lines) {
      System.out.println(line);
    }
  }

  // TODO remove when issues ironed out
  private static <T> void assertIs(T obj, Observable<T> observable) {
    final T actual = observable.toBlocking().single();
    if (actual.equals(obj)) {
      LOG.info("success");
    } else {
      throw new IllegalStateException("problem with equality");
    }
  }

  // TODO remove when issues ironed out
  private static Connection printMetadata(Connection connection) throws SQLException {
    final DatabaseMetaData meta = connection.getMetaData();
    LOG.info("Using driver: " + meta.getDriverName());
    return connection;
  }

  // TODO remove when issues ironed out
  private static byte[] readResoureStream(String resourcePath) throws IOException {
    final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
    final InputStream in = StorageImpl.class.getResourceAsStream(resourcePath);

    try {
      // Create buffer
      final byte[] buffer = new byte[BUFFER_SIZE_IN_BYTES];
      for (;;) {
        final int nread = in.read(buffer);
        if (nread <= 0) {
          break;
        }
        byteArray.write(buffer, 0, nread);
      }
      return byteArray.toByteArray();
    } finally {
      IOUtils.closeQuietly(in);
    }
  }
}
