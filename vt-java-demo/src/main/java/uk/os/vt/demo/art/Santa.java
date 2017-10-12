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

import static uk.os.vt.demo.art.space.Shapes.bomb;
import static uk.os.vt.demo.art.space.Shapes.bullet;
import static uk.os.vt.demo.art.space.Shapes.enemy;
import static uk.os.vt.demo.art.space.Shapes.heart;
import static uk.os.vt.demo.art.space.Shapes.shooter;
import static uk.os.vt.demo.art.xmas.Shapes.santaBody;
import static uk.os.vt.demo.art.xmas.Shapes.santaNose;
import static uk.os.vt.demo.art.xmas.Shapes.santaSack;
import static uk.os.vt.demo.art.xmas.Shapes.santaSledge;
import static uk.os.vt.demo.art.xmas.Shapes.santaSledgeRunners;
import static uk.os.vt.demo.art.xmas.Shapes.santaTrimmings;
import static uk.os.vt.demo.art.xmas.Shapes.santaVisor;

import io.reactivex.Observable;
import io.reactivex.Single;
import java.io.File;
import java.io.IOException;
import uk.os.vt.Entry;
import uk.os.vt.Metadata;
import uk.os.vt.Storage;
import uk.os.vt.demo.util.ResourceUtil;
import uk.os.vt.mbtiles.StorageImpl;

public class Santa {

  private Santa() {}

  /**
   * Main demo to read mb tiles.
   *
   * @param args the args
   * @throws IOException thrown on IO error
   */
  public static void main(String[] args) throws IOException {
    final File planet = ResourceUtil.getFile("osm_planet_z0-z5.mbtiles");
    final StorageImpl planetStorage = new StorageImpl.Builder(planet).build();
    final Metadata planetMetadata = planetStorage.getMetadata().blockingFirst();

    final File boundaries =
        ResourceUtil.getFile("Boundary-line-historic-counties_regionz5.mbtiles");
    final StorageImpl boundaryStorage = new StorageImpl.Builder(boundaries).build();
    final Metadata boundaryMetadata = boundaryStorage.getMetadata().blockingFirst();

    final String enemies = "enemies";
    final Picture.Builder picture = new Picture.Builder();
    picture.setAcetate(enemies).poly(enemy(1000, 3000)).poly(enemy(2000, 3000))
        .poly(enemy(3000, 3000)).poly(enemy(1000, 2500)).poly(enemy(2000, 2500))
        .poly(enemy(3000, 2500)).poly(enemy(1000, 2000)).poly(enemy(2000, 2000))
        .poly(enemy(3000, 2000));

    final String bombs = "bombs";
    picture.setAcetate(bombs).poly(bomb(1120, 1500, 5)).poly(bomb(3000, 1300, 5));

    final String health = "health";
    picture.setAcetate(health).poly(heart(3000, 3500, 4)).poly(heart(3250, 3500, 4))
        .poly(heart(3500, 3500, 4));

    picture.poly(bullet(1700, 1500, 2)).poly(bullet(2000, 1200, 2)).poly(bullet(2300, 900, 2));

    picture.poly(shooter(2000, 500));

    final int santaScale = 2;

    final String santaBrown = "santa-sack";
    picture.setAcetate(santaBrown).poly(santaSack(1900, 1000, santaScale));

    final String santaRed = "santa-red";
    picture.setAcetate(santaRed).poly(santaBody(1900, 1000, santaScale));

    final String santaSledge = "santa-sledge";
    picture.setAcetate(santaSledge).poly(santaSledge(1900, 1000, santaScale));

    final String santaWhite = "santa-white";
    picture.setAcetate(santaWhite).poly(santaTrimmings(1900, 1000, santaScale));

    final String santaVisor = "santa-visor";
    picture.setAcetate(santaVisor).poly(santaVisor(1900, 1000, santaScale));

    final String santaBlack = "santa-black";
    picture.setAcetate(santaBlack).linestring(santaSledgeRunners(1900, 1000, santaScale));

    final String santaNose = "santa-nose";
    picture.setAcetate(santaNose).poly(santaNose(1900, 1000, santaScale));


    final byte[] result = picture.build();

    final File file = new File("magic.mbtiles");
    if (file.exists() && !file.delete()) {
      throw new IOException("cannot delete output file");
    }

    final Storage storage = new StorageImpl.Builder(file).createIfNotExist().build();

    final Metadata.Layer magicLayer = new Metadata.Layer.Builder().setId(enemies)
        .setDescription("OS Terrain data at 50m resolution").setMinZoom(0).setMaxZoom(0)
        .setSource("magic", "Magic OS Layer")
        .build();

    final Metadata.Layer bombLayer = new Metadata.Layer.Builder().setId(bombs)
        .setDescription("OS Terrain data at 50m resolution").setMinZoom(0).setMaxZoom(0)
        .setSource("magic", "Magic OS Layer")
        .build();

    final Metadata.Layer healthLayer = new Metadata.Layer.Builder().setId(health)
        .setDescription("OS Terrain data at 50m resolution").setMinZoom(0).setMaxZoom(0)
        .setSource("magic", "Magic OS Layer")
        .build();

    final Metadata.Layer fishLayer = new Metadata.Layer.Builder().setId("fish")
        .setDescription("OS Terrain data at 50m resolution").setMinZoom(0).setMaxZoom(0)
        .setSource("magic", "Magic OS Layer")
        .build();

    final Metadata.Layer santaSack = new Metadata.Layer.Builder().setId("santa-sack")
        .setDescription("OS Santa").setMinZoom(0).setMaxZoom(0)
        .setSource("magic", "Magic OS Layer")
        .build();

    final Metadata.Layer santaLayer = new Metadata.Layer.Builder().setId("santa-red")
        .setDescription("OS Santa").setMinZoom(0).setMaxZoom(0)
        .setSource("magic", "Magic OS Layer")
        .build();

    final Metadata.Layer santaSledgeLayer = new Metadata.Layer.Builder().setId("santa-sledge")
        .setDescription("OS Santa").setMinZoom(0).setMaxZoom(0)
        .setSource("magic", "Magic OS Layer")
        .build();

    final Metadata.Layer santaWhiteLayer = new Metadata.Layer.Builder().setId("santa-white")
        .setDescription("OS Santa").setMinZoom(0).setMaxZoom(0)
        .setSource("magic", "Magic OS Layer").build();

    final Metadata.Layer santaVisorLayer = new Metadata.Layer.Builder().setId("santa-visor")
        .setDescription("OS Santa").setMinZoom(0).setMaxZoom(0)
        .setSource("magic", "Magic OS Layer")
        .build();

    final Metadata.Layer santaBlackLayer = new Metadata.Layer.Builder().setId("santa-black")
        .setDescription("OS Santa").setMinZoom(0).setMaxZoom(0)
        .setSource("magic", "Magic OS Layer")
        .build();

    final Metadata.Layer santaNoseLayer = new Metadata.Layer.Builder().setId("santa-nose")
        .setDescription("OS Santa").setMinZoom(0).setMaxZoom(0)
        .setSource("magic", "Magic OS Layer")
        .build();

    final Metadata metadata = new Metadata.Builder().copyMetadata(boundaryMetadata)
        .appendMetadata(planetMetadata).addLayer(magicLayer).setMinZoom(0).setMaxZoom(5)
        .setCenter(51, 0, 4)
        .setBounds(-180, -90, 180, 90)
        .setAttribution("Ordnance Survey")
        .setName("magic")
        .addLayer(magicLayer)
        .addLayer(healthLayer)
        .addLayer(bombLayer)
        .addLayer(fishLayer)
        .addLayer(santaSack)
        .addLayer(santaLayer)
        .addLayer(santaSledgeLayer)
        .addLayer(santaWhiteLayer)
        .addLayer(santaVisorLayer)
        .addLayer(santaBlackLayer)
        .addLayer(santaNoseLayer)
        .build();

    storage.putEntries(Observable.just(new Entry(0, 0, 0, result)));
    storage.putEntries(planetStorage.getEntries(1));
    storage.putEntries(planetStorage.getEntries(2));
    storage.putEntries(planetStorage.getEntries(3));
    storage.putEntries(planetStorage.getEntries(4));
    storage.putEntries(boundaryStorage.getEntries(5));

    storage.putMetadata(Single.just(metadata));
  }
}
