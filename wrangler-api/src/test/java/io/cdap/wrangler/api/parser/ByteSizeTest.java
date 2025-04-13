/*
 * Copyright © 2024 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.parser;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link ByteSize}
 */
public class ByteSizeTest {

  @Test
  public void testValidByteSizes() {
    ByteSize bytes = new ByteSize("1024B");
    Assert.assertEquals(1024.0, bytes.getBytes(), 0.001);
    Assert.assertEquals(1.0, bytes.getKilobytes(), 0.001);

    ByteSize kilobytes = new ByteSize("1.5KB");
    Assert.assertEquals(1536.0, kilobytes.getBytes(), 0.001);
    Assert.assertEquals(1.5, kilobytes.getKilobytes(), 0.001);

    ByteSize megabytes = new ByteSize("2.5MB");
    Assert.assertEquals(2.5, megabytes.getMegabytes(), 0.001);
    Assert.assertEquals(2560.0, megabytes.getKilobytes(), 0.001);

    ByteSize gigabytes = new ByteSize("1GB");
    Assert.assertEquals(1.0, gigabytes.getGigabytes(), 0.001);
    Assert.assertEquals(1024.0, gigabytes.getMegabytes(), 0.001);

    ByteSize terabytes = new ByteSize("0.5TB");
    Assert.assertEquals(0.5, terabytes.getTerabytes(), 0.001);
    Assert.assertEquals(512.0, terabytes.getGigabytes(), 0.001);
  }

  @Test
  public void testCaseInsensitiveUnits() {
    ByteSize kb1 = new ByteSize("1KB");
    ByteSize kb2 = new ByteSize("1kb");
    Assert.assertEquals(kb1.getBytes(), kb2.getBytes(), 0.001);

    ByteSize mb1 = new ByteSize("1MB");
    ByteSize mb2 = new ByteSize("1mb");
    Assert.assertEquals(mb1.getBytes(), mb2.getBytes(), 0.001);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    new ByteSize("1.5");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    new ByteSize("1.5XB");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidNumber() {
    new ByteSize("abc KB");
  }

  @Test
  public void testToString() {
    String value = "1.5GB";
    ByteSize byteSize = new ByteSize(value);
    Assert.assertEquals(value, byteSize.toString());
  }

  @Test
  public void testJsonSerialization() {
    ByteSize byteSize = new ByteSize("2.5MB");
    Assert.assertEquals(TokenType.BYTE_SIZE, byteSize.type());
    Assert.assertTrue(byteSize.toJson().isJsonObject());
    Assert.assertEquals("2.5MB", byteSize.toJson().getAsJsonObject().get("value").getAsString());
    Assert.assertEquals(2.5 * 1024 * 1024, byteSize.toJson().getAsJsonObject().get("bytes").getAsDouble(), 0.001);
  }
} 