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
 * Tests for {@link TimeDuration}
 */
public class TimeDurationTest {

  @Test
  public void testValidDurations() {
    TimeDuration milliseconds = new TimeDuration("1500ms");
    Assert.assertEquals(1500.0, milliseconds.getMilliseconds(), 0.001);
    Assert.assertEquals(1.5, milliseconds.getSeconds(), 0.001);

    TimeDuration seconds = new TimeDuration("2.5s");
    Assert.assertEquals(2500.0, seconds.getMilliseconds(), 0.001);
    Assert.assertEquals(2.5, seconds.getSeconds(), 0.001);

    TimeDuration minutes = new TimeDuration("1.5m");
    Assert.assertEquals(90.0, minutes.getSeconds(), 0.001);
    Assert.assertEquals(1.5, minutes.getMinutes(), 0.001);

    TimeDuration hours = new TimeDuration("2h");
    Assert.assertEquals(2.0, hours.getHours(), 0.001);
    Assert.assertEquals(120.0, hours.getMinutes(), 0.001);

    TimeDuration days = new TimeDuration("1.5d");
    Assert.assertEquals(1.5, days.getDays(), 0.001);
    Assert.assertEquals(36.0, days.getHours(), 0.001);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    new TimeDuration("1.5");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    new TimeDuration("1.5x");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidNumber() {
    new TimeDuration("abc ms");
  }

  @Test
  public void testToString() {
    String value = "1.5h";
    TimeDuration duration = new TimeDuration(value);
    Assert.assertEquals(value, duration.toString());
  }

  @Test
  public void testJsonSerialization() {
    TimeDuration duration = new TimeDuration("2.5m");
    Assert.assertEquals(TokenType.TIME_DURATION, duration.type());
    Assert.assertTrue(duration.toJson().isJsonObject());
    Assert.assertEquals("2.5m", duration.toJson().getAsJsonObject().get("value").getAsString());
    Assert.assertEquals(2.5 * 60 * 1000, duration.toJson().getAsJsonObject().get("milliseconds").getAsDouble(), 0.001);
  }
} 