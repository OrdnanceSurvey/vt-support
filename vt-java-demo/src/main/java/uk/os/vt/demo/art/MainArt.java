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

package uk.os.vt.demo.art;

import static uk.os.vt.demo.art.nature.Shapes.bubble;
import static uk.os.vt.demo.art.nature.Shapes.bubble2;
import static uk.os.vt.demo.art.nature.Shapes.fish;
import static uk.os.vt.demo.art.space.Shapes.bomb;
import static uk.os.vt.demo.art.space.Shapes.bullet;
import static uk.os.vt.demo.art.space.Shapes.enemy;
import static uk.os.vt.demo.art.space.Shapes.heart;
import static uk.os.vt.demo.art.space.Shapes.shooter;

import java.io.File;
import java.io.IOException;
import rx.Observable;
import rx.Single;
import uk.os.vt.Entry;
import uk.os.vt.Metadata;
import uk.os.vt.demo.util.ResourceUtil;
import uk.os.vt.mbtiles.StorageImpl;

public class MainArt {

  private MainArt() {}

  /**
   * Main demo to read mb tiles.
   *
   * @param args the args
   * @throws IOException thrown on IO error
   */
  public static void main(String[] args) throws IOException {
    // Planet store
    final File planet = ResourceUtil.getFile("osm_planet_z0-z5.mbtiles");
    final StorageImpl planetStorage = new StorageImpl.Builder(planet).build();
    final Metadata planetMetadata = planetStorage.getMetadata().toBlocking().first();

    // OS boundaries store
    final File boundaries =
        ResourceUtil.getFile("Boundary-line-historic-counties_regionz5.mbtiles");
    final StorageImpl boundaryStorage = new StorageImpl.Builder(boundaries).build();
    final Metadata boundaryMetadata = boundaryStorage.getMetadata().toBlocking().first();

    // New metadata
    final Metadata.Builder metadata = new Metadata.Builder().copyMetadata(boundaryMetadata)
        .appendMetadata(planetMetadata).setMinZoom(0).setMaxZoom(5)
        .setCenter(51, 0, 4).setBounds(-180, -90, 180, 90).setAttribution("Ordnance Survey")
        .setName("magic");

    // Output file
    final File file = new File("magic.mbtiles");
    if (file.exists()) {
      boolean success = file.delete();
      if (!success) {
        throw new IOException("cannot delete file");
      }
    }

    final StorageImpl storage = new StorageImpl.Builder(file).createIfNotExist().build();

    // "and so we become Picasso"

    // add Space Invaders
    PictureWithMetadata spaceInvaders = getSpaceInvadersPicture();
    byte[] spaceInvadersPicture = spaceInvaders.getPicture();
    Metadata spaceInvadersMetadata = spaceInvaders.getMetadata();
    metadata.appendMetadata(spaceInvadersMetadata);
    storage.putEntries(Observable.just(new Entry(0, 0, 0, spaceInvadersPicture)));

    // add Fish
    final Metadata.Layer fishLayer = new Metadata.Layer.Builder().setId("fish")
        .setDescription("OS Terrain data at 50m resolution").setMinZoom(0).setMaxZoom(0)
        .setSource("magic", "Magic OS Layer")
        .build();

    metadata.addLayer(fishLayer);
    storage.putEntries(Observable.just(new Entry(1, 0, 0, getFishPicture(3000, 1000))));
    storage.putEntries(Observable.just(new Entry(1, 1, 0, getFishPicture(3500, 500))));
    storage.putEntries(Observable.just(new Entry(1, 0, 1, getFishPicture(3500, 3000))));
    storage.putEntries(Observable.just(new Entry(1, 1, 1, getFishPicture(2000, 3500))));

    // add planet data
    storage.putEntries(planetStorage.getEntries(2));
    storage.putEntries(planetStorage.getEntries(3));
    storage.putEntries(planetStorage.getEntries(4));
    storage.putEntries(boundaryStorage.getEntries(5));

    // add metadata
    storage.putMetadata(Single.just(metadata.build()));
  }

  private static void applyBombs(Picture.Builder picture, Metadata.Builder md) {
    final String bombs = "bombs";
    picture.setAcetate(bombs).poly(bomb(1120, 1500, 5)).poly(bomb(3000, 1300, 5));

    final Metadata.Layer bombLayer = new Metadata.Layer.Builder().setId(bombs)
        .setDescription("OS Terrain data at 50m resolution").setMinZoom(0).setMaxZoom(0)
        .setSource("magic", "Magic OS Layer")
        .build();
    md.addLayer(bombLayer);
  }

  private static void applyEnemies(Picture.Builder picture, Metadata.Builder md) {
    final String enemies = "enemies";
    picture.setAcetate(enemies).poly(enemy(1000, 3000)).poly(enemy(2000, 3000))
        .poly(enemy(3000, 3000)).poly(enemy(1000, 2500)).poly(enemy(2000, 2500))
        .poly(enemy(3000, 2500)).poly(enemy(1000, 2000)).poly(enemy(2000, 2000))
        .poly(enemy(3000, 2000));

    final Metadata.Layer magicLayer = new Metadata.Layer.Builder().setId(enemies)
        .setDescription("OS Terrain data at 50m resolution").setMinZoom(0).setMaxZoom(0)
        .setSource("magic", "Magic OS Layer")
        .build();
    md.addLayer(magicLayer);
  }

  private static void applyHealth(Picture.Builder picture, Metadata.Builder md) {
    final String health = "health";
    picture.setAcetate(health).poly(heart(3000, 3500, 4)).poly(heart(3250, 3500, 4))
        .poly(heart(3500, 3500, 4));

    final Metadata.Layer healthLayer = new Metadata.Layer.Builder().setId(health)
        .setDescription("OS Terrain data at 50m resolution").setMinZoom(0).setMaxZoom(0)
        .setSource("magic", "Magic OS Layer")
        .build();

    md.addLayer(healthLayer);
  }

  private static void applyBullet(Picture.Builder picture, Metadata.Builder md) {
    final String bullet = "bullet";
    picture.setAcetate(bullet)
        .poly(bullet(1700, 1500, 2)).poly(bullet(2000, 1200, 2)).poly(bullet(2300, 900, 2));

    final Metadata.Layer bulletLayer = new Metadata.Layer.Builder().setId(bullet)
        .setDescription("OS Terrain data at 50m resolution").setMinZoom(0).setMaxZoom(0)
        .setSource("magic", "Magic OS Layer")
        .build();

    md.addLayer(bulletLayer);
  }

  private static void applyShooter(Picture.Builder picture, Metadata.Builder md) {
    final String bullet = "bullet";
    picture.setAcetate(bullet).poly(shooter(2000, 500));

    final Metadata.Layer shooterLayer = new Metadata.Layer.Builder().setId(bullet)
        .setDescription("OS Terrain data at 50m resolution").setMinZoom(0).setMaxZoom(0)
        .setSource("magic", "Magic OS Layer")
        .build();

    md.addLayer(shooterLayer);
  }

  private static byte[] getFishPicture(int col, int row) {
    final String fish = "fish";
    return new Picture.Builder().setAcetate(fish).poly(fish(col, row, 10))
        .poly(bubble(col, row, 10)).poly(bubble2(col, row, 10)).build();
  }

  private static class PictureWithMetadata {
    private final byte[] picture;
    private final Metadata metadata;

    public PictureWithMetadata(Picture.Builder pb, Metadata.Builder mb) {
      picture = pb.build();
      metadata = mb.build();
    }

    public byte[] getPicture() {
      return picture;
    }

    public Metadata getMetadata() {
      return metadata;
    }
  }

  private static PictureWithMetadata getSpaceInvadersPicture() {
    // Space invaders picture
    final Picture.Builder picture = new Picture.Builder();
    // Space invaders metadata
    final Metadata.Builder metadata = new Metadata.Builder();

    // Add drawing to picture (tile) and update the metadata
    applyEnemies(picture, metadata);
    applyBombs(picture, metadata);
    applyHealth(picture, metadata);
    applyBullet(picture, metadata);
    applyShooter(picture, metadata);

    return new PictureWithMetadata(picture, metadata);
  }
}
