/**
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

package uk.os.vt;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

public class MetadataTest {

  private static final Metadata.Layer.Attribute ATTRIBUTE_1 = getAttribute("inner-diameter",
      "Number the inner diameter of the ring");
  private static final Metadata.Layer.Attribute ATTRIBUTE_2 = getAttribute("outer-diameter",
      "Number the outer diameter of the ring");
  private static final Metadata.Layer.Attribute ATTRIBUTE_3 = getAttribute("weight",
      "total weight of the lifebuoy");

  @Test
  public void attributeBuildsAndIsCorrect() {
    final String nameUnderTest = "weight";
    final String descriptionUnderTest = "total weight of the lifebuoy";

    Metadata.Layer.Attribute attribute = new Metadata.Layer.Attribute.Builder()
        .setName(nameUnderTest)
        .setDescription(descriptionUnderTest)
        .build();

    String expectedName = nameUnderTest;
    String actualName = attribute.getName();
    assertEquals(expectedName, actualName);

    String expectedDescription = descriptionUnderTest;
    String actualDescription = attribute.getDescription();
    assertEquals(expectedDescription, actualDescription);
  }

  @Test
  public void attributesCanBeAddedToLayerAndReadAgain() {
    String attribute1Name = "inner-diameter";
    String attribute1Desc = "Number the inner diameter of the ring";

    String attribute2Name = "outer-diameter";
    String attribute2Desc = "Number the outer diameter of the ring";

    String attribute3Name = "weight";
    String attribute3Desc = "total weight of the lifebuoy";

    Metadata.Layer.Attribute attribute = new Metadata.Layer.Attribute.Builder()
        .setName(attribute3Name)
        .setDescription(attribute3Desc)
        .build();

    Metadata.Layer layer = new Metadata.Layer.Builder()
        .setId("lifebuoy")
        .setMinZoom(10)
        .setMaxZoom(14)
        .preserveFieldOrder(true)
        .addField(attribute1Name, attribute1Desc)
        .addField(attribute2Name, attribute2Desc)
        .addField(attribute)
        .build();

    List<Metadata.Layer.Attribute> attributes = layer.getAttributes();
    int expectedCount = 3;
    int actualCount = attributes.size();
    assertEquals(expectedCount, actualCount);

    testAttribute(attributes.get(0), attribute1Name, attribute1Desc);
    testAttribute(attributes.get(1), attribute2Name, attribute2Desc);
    testAttribute(attributes.get(2), attribute3Name, attribute3Desc);
  }

  @Test
  public void repeatedAttributesWithOrderMeansLastItemAddedIsLast() {
    Metadata.Layer layer = new Metadata.Layer.Builder()
        .setId("lifebuoy")
        .setMinZoom(10)
        .setMaxZoom(14)
        .preserveFieldOrder(true)
        .addField(ATTRIBUTE_1)
        .addField(ATTRIBUTE_2)
        .addField(ATTRIBUTE_3)
        .addField(ATTRIBUTE_1)
        .build();

    List<Metadata.Layer.Attribute> attributes = layer.getAttributes();

    int expectedCount = 3;
    int actualCount = attributes.size();

    assertEquals(expectedCount, actualCount);

    assertEquals(ATTRIBUTE_2, attributes.get(0));
    assertEquals(ATTRIBUTE_3, attributes.get(1));
    assertEquals(ATTRIBUTE_1, attributes.get(2));
  }

  @Test
  public void underOrderedAttributeCanBeAdded() {
    Metadata.Layer layer = new Metadata.Layer.Builder()
        .setId("lifebuoy")
        .setMinZoom(10)
        .setMaxZoom(14)
        .preserveFieldOrder(true)
        .addField(ATTRIBUTE_1)
        .build();

    List<Metadata.Layer.Attribute> attributes = layer.getAttributes();

    int expectedCount = 1;
    int actualCount = attributes.size();

    assertEquals(expectedCount, actualCount);

    assertEquals(ATTRIBUTE_1, attributes.get(0));
  }

  private static void testAttribute(Metadata.Layer.Attribute attr, String name, String desc) {
    Metadata.Layer.Attribute expected = new Metadata.Layer.Attribute.Builder()
        .setName(name)
        .setDescription(desc)
        .build();
    assertEquals(expected, attr);
  }

  private static Metadata.Layer.Attribute getAttribute(String name, String desc) {
    return new Metadata.Layer.Attribute.Builder().setName(name).setDescription(desc).build();
  }

}
