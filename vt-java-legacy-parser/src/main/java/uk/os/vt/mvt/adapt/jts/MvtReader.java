/*
 * Copyright (C) 2017 Weather Decision Technologies
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

package uk.os.vt.mvt.adapt.jts;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.algorithm.Area;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.LoggerFactory;
import uk.os.vt.mvt.VectorTile;
import uk.os.vt.mvt.adapt.jts.model.JtsLayer;
import uk.os.vt.mvt.adapt.jts.model.JtsMvt;
import uk.os.vt.mvt.encoding.GeomCmd;
import uk.os.vt.mvt.encoding.GeomCmdHdr;
import uk.os.vt.mvt.encoding.ZigZag;
import uk.os.vt.mvt.util.Vec2d;

/**
 * Load Mapbox Vector Tiles (MVT) to JTS {@link Geometry}. Feature tags may be converted to user
 * data via {@link ITagConverter}.
 *
 * @see JtsMvt
 * @see JtsLayer
 */
public final class MvtReader {
  private static final int MIN_LINE_STRING_LEN = 6; // MoveTo,1 + LineTo,1
  private static final int MIN_POLYGON_LEN = 9; // MoveTo,1 + LineTo,2 + ClosePath

  /**
   * Convenience method for loading MVT from file.
   * See {@link #loadMvt(InputStream, GeometryFactory, ITagConverter, RingClassifier)}.
   * Uses {@link #RING_CLASSIFIER_V2_1} for forming Polygons and MultiPolygons.
   *
   * @param file         path to the MVT
   * @param geomFactory  allows for JTS geometry creation
   * @param tagConverter converts MVT feature tags to JTS user data object
   * @return JTS MVT with geometry in MVT coordinates
   * @throws IOException failure reading MVT from path
   * @see #loadMvt(InputStream, GeometryFactory, ITagConverter, RingClassifier)
   * @see Geometry
   * @see Geometry#getUserData()
   * @see RingClassifier
   */
  public static JtsMvt loadMvt(File file,
                               GeometryFactory geomFactory,
                               ITagConverter tagConverter) throws IOException {
    return loadMvt(file, geomFactory, tagConverter, RING_CLASSIFIER_V2_1);
  }

  /**
   * Convenience method for loading MVT from file.
   * See {@link #loadMvt(InputStream, GeometryFactory, ITagConverter, RingClassifier)}.
   *
   * @param file           path to the MVT
   * @param geomFactory    allows for JTS geometry creation
   * @param tagConverter   converts MVT feature tags to JTS user data object
   * @param ringClassifier determines how rings are parsed into Polygons and MultiPolygons
   * @return JTS MVT with geometry in MVT coordinates
   * @throws IOException failure reading MVT from path
   * @see #loadMvt(InputStream, GeometryFactory, ITagConverter, RingClassifier)
   * @see Geometry
   * @see Geometry#getUserData()
   * @see RingClassifier
   */
  public static JtsMvt loadMvt(File file,
                               GeometryFactory geomFactory,
                               ITagConverter tagConverter,
                               RingClassifier ringClassifier) throws IOException {
    final JtsMvt jtsMvt;

    try (final InputStream is = new FileInputStream(file)) {
      jtsMvt = loadMvt(is, geomFactory, tagConverter, ringClassifier);
    }

    return jtsMvt;
  }

  /**
   * Load an MVT to JTS geometries using coordinates. Uses {@code tagConverter} to create user data
   * from feature properties.
   *
   * @param is           stream with MVT data
   * @param geomFactory  allows for JTS geometry creation
   * @param tagConverter converts MVT feature tags to JTS user data object.
   * @return JTS MVT with geometry in MVT coordinates
   * @throws IOException failure reading MVT from stream
   * @see Geometry
   * @see Geometry#getUserData()
   * @see RingClassifier
   */
  public static JtsMvt loadMvt(InputStream is,
                               GeometryFactory geomFactory,
                               ITagConverter tagConverter) throws IOException {
    return loadMvt(is, geomFactory, tagConverter, RING_CLASSIFIER_V2_1);
  }

  /**
   * Load an MVT to JTS geometries using coordinates. Uses {@code tagConverter} to create user data
   * from feature properties.
   *
   * @param is             stream with MVT data
   * @param geomFactory    allows for JTS geometry creation
   * @param tagConverter   converts MVT feature tags to JTS user data object.
   * @param ringClassifier determines how rings are parsed into Polygons and MultiPolygons
   * @return JTS MVT with geometry in MVT coordinates
   * @throws IOException failure reading MVT from stream
   * @see Geometry
   * @see Geometry#getUserData()
   * @see RingClassifier
   */
  public static JtsMvt loadMvt(InputStream is,
                               GeometryFactory geomFactory,
                               ITagConverter tagConverter,
                               RingClassifier ringClassifier) throws IOException {

    final VectorTile.Tile mvt = VectorTile.Tile.parseFrom(is);
    final Vec2d cursor = new Vec2d();
    final List<JtsLayer> jtsLayers = new ArrayList<>(mvt.getLayersList().size());

    for (VectorTile.Tile.Layer nextLayer : mvt.getLayersList()) {

      final List<String> keysList = nextLayer.getKeysList();
      final List<VectorTile.Tile.Value> valuesList = nextLayer.getValuesList();
      final List<Geometry> layerGeoms = new ArrayList<>(nextLayer.getFeaturesList().size());

      for (VectorTile.Tile.Feature nextFeature : nextLayer.getFeaturesList()) {

        final Long id = nextFeature.hasId() ? nextFeature.getId() : null;

        final VectorTile.Tile.GeomType geomType = nextFeature.getType();

        if (geomType == VectorTile.Tile.GeomType.UNKNOWN) {
          continue;
        }

        final List<Integer> geomCmds = nextFeature.getGeometryList();
        cursor.set(0d, 0d);
        final Geometry nextGeom = readGeometry(geomCmds, geomType, geomFactory, cursor,
            ringClassifier);
        if (nextGeom != null) {
          nextGeom.setUserData(tagConverter.toUserData(id, nextFeature.getTagsList(),
              keysList, valuesList));
          layerGeoms.add(nextGeom);
        }
      }

      jtsLayers.add(new JtsLayer(nextLayer.getName(), layerGeoms));
    }


    return new JtsMvt(jtsLayers);
  }

  private static Geometry readGeometry(List<Integer> geomCmds,
                                       VectorTile.Tile.GeomType geomType,
                                       GeometryFactory geomFactory,
                                       Vec2d cursor,
                                       RingClassifier ringClassifier) {
    Geometry result = null;

    switch (geomType) {
      case POINT:
        result = readPoints(geomFactory, geomCmds, cursor);
        break;
      case LINESTRING:
        result = readLines(geomFactory, geomCmds, cursor);
        break;
      case POLYGON:
        result = readPolys(geomFactory, geomCmds, cursor, ringClassifier);
        break;
      default:
        LoggerFactory.getLogger(MvtReader.class)
            .error("readGeometry(): Unhandled geometry type [{}]", geomType);
    }

    return result;
  }

  /**
   * Create {@link Point} or {@link MultiPoint} from MVT geometry drawing commands.
   *
   * @param geomFactory creates JTS geometry
   * @param geomCmds    contains MVT geometry commands
   * @param cursor      contains current MVT extent position
   * @return JTS geometry or null on failure
   */
  private static Geometry readPoints(GeometryFactory geomFactory, List<Integer> geomCmds,
                                     Vec2d cursor) {

    // Guard: must have header
    if (geomCmds.isEmpty()) {
      return null;
    }

    /** Geometry command index. */
    int index = 0;

    // Read command header
    final int cmdHdr = geomCmds.get(index++);
    final int cmdLength = GeomCmdHdr.getCmdLength(cmdHdr);
    final GeomCmd cmd = GeomCmdHdr.getCmd(cmdHdr);

    // Guard: command type
    if (cmd != GeomCmd.MoveTo) {
      return null;
    }

    // Guard: minimum command length
    if (cmdLength < 1) {
      return null;
    }

    // Guard: header data unsupported by geometry command buffer
    //  (require header and at least 1 value * 2 params)
    if (cmdLength * GeomCmd.MoveTo.getParamCount() + 1 > geomCmds.size()) {
      return null;
    }

    final CoordinateSequence coordSeq = geomFactory.getCoordinateSequenceFactory()
        .create(cmdLength, 2);
    int coordIndex = 0;
    Coordinate nextCoord;

    while (index < geomCmds.size() - 1) {
      cursor.add(
          ZigZag.decode(geomCmds.get(index++)),
          ZigZag.decode(geomCmds.get(index++))
      );

      nextCoord = coordSeq.getCoordinate(coordIndex++);
      nextCoord.setOrdinate(0, cursor.x);
      nextCoord.setOrdinate(1, cursor.y);
    }

    if (coordSeq.size() == 1) {
      return geomFactory.createPoint(coordSeq);
    } else {
      return geomFactory.createMultiPoint(coordSeq);
    }
  }

  /**
   * Create {@link LineString} or {@link MultiLineString} from MVT geometry drawing commands.
   *
   * @param geomFactory creates JTS geometry
   * @param geomCmds    contains MVT geometry commands
   * @param cursor      contains current MVT extent position
   * @return JTS geometry or null on failure
   */
  private static Geometry readLines(GeometryFactory geomFactory, List<Integer> geomCmds,
                                    Vec2d cursor) {

    // Guard: must have header
    if (geomCmds.isEmpty()) {
      return null;
    }

    /** Geometry command index. */
    int index = 0;

    int cmdHdr;
    int cmdLength;
    GeomCmd cmd;
    List<LineString> geoms = new ArrayList<>(1);
    CoordinateSequence nextCoordSeq;
    Coordinate nextCoord;

    while (index <= geomCmds.size() - MIN_LINE_STRING_LEN) {

      // --------------------------------------------
      // Expected: MoveTo command of length 1
      // --------------------------------------------

      // Read command header
      cmdHdr = geomCmds.get(index++);
      cmdLength = GeomCmdHdr.getCmdLength(cmdHdr);
      cmd = GeomCmdHdr.getCmd(cmdHdr);

      // Guard: command type and length
      if (cmd != GeomCmd.MoveTo || cmdLength != 1) {
        break;
      }

      // Update cursor position with relative move
      cursor.add(
          ZigZag.decode(geomCmds.get(index++)),
          ZigZag.decode(geomCmds.get(index++))
      );


      // --------------------------------------------
      // Expected: LineTo command of length > 0
      // --------------------------------------------

      // Read command header
      cmdHdr = geomCmds.get(index++);
      cmdLength = GeomCmdHdr.getCmdLength(cmdHdr);
      cmd = GeomCmdHdr.getCmd(cmdHdr);

      // Guard: command type and length
      if (cmd != GeomCmd.LineTo || cmdLength < 1) {
        break;
      }

      // Guard: header data length unsupported by geometry command buffer
      //  (require at least (1 value * 2 params) + current_index)
      if ((cmdLength * GeomCmd.LineTo.getParamCount()) + index > geomCmds.size()) {
        break;
      }

      nextCoordSeq = geomFactory.getCoordinateSequenceFactory().create(1 + cmdLength, 2);

      // Set first point from MoveTo command
      nextCoord = nextCoordSeq.getCoordinate(0);
      nextCoord.setOrdinate(0, cursor.x);
      nextCoord.setOrdinate(1, cursor.y);

      // Set remaining points from LineTo command
      for (int lineToIndex = 0; lineToIndex < cmdLength; ++lineToIndex) {

        // Update cursor position with relative line delta
        cursor.add(
            ZigZag.decode(geomCmds.get(index++)),
            ZigZag.decode(geomCmds.get(index++))
        );

        nextCoord = nextCoordSeq.getCoordinate(lineToIndex + 1);
        nextCoord.setOrdinate(0, cursor.x);
        nextCoord.setOrdinate(1, cursor.y);
      }

      geoms.add(geomFactory.createLineString(nextCoordSeq));
    }

    if (geoms.size() == 1) {
      return geoms.get(0);
    } else {
      return geomFactory.createMultiLineString(geoms.toArray(new LineString[geoms.size()]));
    }
  }

  /**
   * Create {@link Polygon} or {@link MultiPolygon} from MVT geometry drawing commands.
   *
   * @param geomFactory    creates JTS geometry
   * @param geomCmds       contains MVT geometry commands
   * @param cursor         contains current MVT extent position
   * @param ringClassifier classifier implementation to use
   * @return JTS geometry or null on failure
   */
  private static Geometry readPolys(GeometryFactory geomFactory,
                                    List<Integer> geomCmds,
                                    Vec2d cursor,
                                    RingClassifier ringClassifier) {

    // Guard: must have header
    if (geomCmds.isEmpty()) {
      return null;
    }

    /** Geometry command index. */
    int index = 0;

    int cmdHdr;
    int cmdLength;
    GeomCmd cmd;
    List<LinearRing> rings = new ArrayList<>(1);
    CoordinateSequence nextCoordSeq;
    Coordinate nextCoord;

    while (index <= geomCmds.size() - MIN_POLYGON_LEN) {

      // --------------------------------------------
      // Expected: MoveTo command of length 1
      // --------------------------------------------

      // Read command header
      cmdHdr = geomCmds.get(index++);
      cmdLength = GeomCmdHdr.getCmdLength(cmdHdr);
      cmd = GeomCmdHdr.getCmd(cmdHdr);

      // Guard: command type and length
      if (cmd != GeomCmd.MoveTo || cmdLength != 1) {
        break;
      }

      // Update cursor position with relative move
      cursor.add(
          ZigZag.decode(geomCmds.get(index++)),
          ZigZag.decode(geomCmds.get(index++))
      );


      // --------------------------------------------
      // Expected: LineTo command of length > 1
      // --------------------------------------------

      // Read command header
      cmdHdr = geomCmds.get(index++);
      cmdLength = GeomCmdHdr.getCmdLength(cmdHdr);
      cmd = GeomCmdHdr.getCmd(cmdHdr);

      // Guard: command type and length
      if (cmd != GeomCmd.LineTo || cmdLength < 2) {
        break;
      }

      // Guard: header data length unsupported by geometry command buffer
      //  (require at least (2 values * 2 params) + (current index 'i') + (1 for ClosePath))
      if ((cmdLength * GeomCmd.LineTo.getParamCount()) + index + 1 > geomCmds.size()) {
        break;
      }

      nextCoordSeq = geomFactory.getCoordinateSequenceFactory().create(2 + cmdLength, 2);

      // Set first point from MoveTo command
      nextCoord = nextCoordSeq.getCoordinate(0);
      nextCoord.setOrdinate(0, cursor.x);
      nextCoord.setOrdinate(1, cursor.y);

      // Set remaining points from LineTo command
      for (int lineToIndex = 0; lineToIndex < cmdLength; ++lineToIndex) {

        // Update cursor position with relative line delta
        cursor.add(
            ZigZag.decode(geomCmds.get(index++)),
            ZigZag.decode(geomCmds.get(index++))
        );

        nextCoord = nextCoordSeq.getCoordinate(lineToIndex + 1);
        nextCoord.setOrdinate(0, cursor.x);
        nextCoord.setOrdinate(1, cursor.y);
      }


      // --------------------------------------------
      // Expected: ClosePath command of length 1
      // --------------------------------------------

      // Read command header
      cmdHdr = geomCmds.get(index++);
      cmdLength = GeomCmdHdr.getCmdLength(cmdHdr);
      cmd = GeomCmdHdr.getCmd(cmdHdr);

      if (cmd != GeomCmd.ClosePath || cmdLength != 1) {
        break;
      }

      // Set last point from ClosePath command
      nextCoord = nextCoordSeq.getCoordinate(nextCoordSeq.size() - 1);
      nextCoord.setOrdinate(0, nextCoordSeq.getOrdinate(0, 0));
      nextCoord.setOrdinate(1, nextCoordSeq.getOrdinate(0, 1));

      rings.add(geomFactory.createLinearRing(nextCoordSeq));
    }


    // Classify rings
    final List<Polygon> polygons = ringClassifier.classifyRings(rings, geomFactory);
    if (polygons.size() < 1) {
      return null;

    } else if (polygons.size() == 1) {
      return polygons.get(0);

    } else {
      return geomFactory.createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
    }
  }


  /**
   * Classifies Polygon and MultiPolygon rings.
   */
  public interface RingClassifier {

    /**
     * <p>Classify a list of rings into polygons using surveyor formula.</p>
     * <p>Zero-area polygons are removed.</p>
     *
     * @param rings       linear rings to classify into polygons
     * @param geomFactory creates JTS geometry
     * @return polygons from classified rings
     */
    List<Polygon> classifyRings(List<LinearRing> rings, GeometryFactory geomFactory);
  }


  /**
   * Area for surveyor formula may be positive or negative for exterior rings. Mimics Mapbox parsers
   * supporting V1.
   */
  public static final RingClassifier RING_CLASSIFIER_V1 = new PolyRingClassifierV1();

  /**
   * Area from surveyor formula must be positive for exterior rings. Obeys V2.1 spec.
   */
  public static final RingClassifier RING_CLASSIFIER_V2_1 = new PolyRingClassifierV2r1();


  /**
   * Area from surveyor formula must be positive for exterior rings. Obeys V2.1 spec.
   *
   * @see Area#ofRingSigned(Coordinate[])
   */
  private static final class PolyRingClassifierV2r1 implements RingClassifier {

    @Override
    public List<Polygon> classifyRings(List<LinearRing> rings, GeometryFactory geomFactory) {
      final List<Polygon> polygons = new ArrayList<>();
      final List<LinearRing> holes = new ArrayList<>();

      double outerArea = 0d;
      LinearRing outerPoly = null;

      for (LinearRing r : rings) {
        double area = Area.ofRingSigned(r.getCoordinates());

        if (!r.isRing()) {
          continue; // sanity check, could probably be handled in a isSimple() check
        }

        if (area == 0d) {
          continue; // zero-area
        }

        if (area > 0d) {
          if (outerPoly != null) {
            polygons.add(geomFactory.createPolygon(outerPoly,
                holes.toArray(new LinearRing[holes.size()])));
            holes.clear();
          }

          // Pos --> CCW, Outer
          outerPoly = r;
          outerArea = area;

        } else {

          if (Math.abs(outerArea) < Math.abs(area)) {
            continue; // Holes must have less area, could probably be handled in a isSimple() check
          }

          // Neg --> CW, Hole
          holes.add(r);
        }
      }

      if (outerPoly != null) {
        // TODO: https://github.com/wdtinc/mapbox-vector-tile-java/issues/13
        //holes.toArray();
        polygons.add(geomFactory.createPolygon(outerPoly,
            holes.toArray(new LinearRing[holes.size()])));
      }

      return polygons;
    }
  }


  /**
   * Area for surveyor formula may be positive or negative for exterior rings. Mimics Mapbox
   * parsers supporting V1.
   *
   * @see Area#ofRingSigned(Coordinate[])
   */
  private static final class PolyRingClassifierV1 implements RingClassifier {

    @Override
    public List<Polygon> classifyRings(List<LinearRing> rings, GeometryFactory geomFactory) {
      final List<Polygon> polygons = new ArrayList<>();
      final List<LinearRing> holes = new ArrayList<>();

      double outerArea = 0d;
      LinearRing outerPoly = null;

      for (LinearRing r : rings) {
        double area = Area.ofRingSigned(r.getCoordinates());

        if (!r.isRing()) {
          continue; // sanity check, could probably be handled in a isSimple() check
        }

        if (area == 0d) {
          continue; // zero-area
        }

        if (outerPoly == null || (outerArea < 0 == area < 0)) {
          if (outerPoly != null) {
            polygons.add(geomFactory.createPolygon(outerPoly,
                holes.toArray(new LinearRing[holes.size()])));
            holes.clear();
          }

          // Pos --> CCW, Outer
          outerPoly = r;
          outerArea = area;

        } else {

          if (Math.abs(outerArea) < Math.abs(area)) {
            continue; // Holes must have less area, could probably be handled in a isSimple() check
          }

          // Neg --> CW, Hole
          holes.add(r);
        }
      }

      if (outerPoly != null) {
        // TODO: https://github.com/wdtinc/mapbox-vector-tile-java/issues/13
        //holes.toArray();
        polygons.add(geomFactory.createPolygon(outerPoly,
            holes.toArray(new LinearRing[holes.size()])));
      }

      return polygons;
    }
  }
}
