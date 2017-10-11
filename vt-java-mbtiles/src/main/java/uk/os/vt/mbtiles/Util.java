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

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Util {

  private static final Logger LOG = LoggerFactory.getLogger(Util.class.getSimpleName());
  private static final int STATEMENT_QUERY_TIMEOUT_IN_SECONDS = 30;

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

  // TODO remove when issues ironed out
  private static Connection printMetadata(Connection connection) throws SQLException {
    final DatabaseMetaData meta = connection.getMetaData();
    LOG.info("Using driver: " + meta.getDriverName());
    return connection;
  }
}
