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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.cdap.wrangler.api.annotations.PublicEvolving;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A token type that represents time durations with units (e.g., "100ms", "2.5s").
 */
@PublicEvolving
public class TimeDuration implements Token {
  private static final Pattern TIME_DURATION_PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)?)(ms|s|m|h|d)$");
  private final String originalValue;
  private final double value;
  private final String unit;
  private final long milliseconds;

  public TimeDuration(String value) {
    this.originalValue = value;
    Matcher matcher = TIME_DURATION_PATTERN.matcher(value);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid time duration format: " + value);
    }

    this.value = Double.parseDouble(matcher.group(1));
    this.unit = matcher.group(2);
    this.milliseconds = convertToMilliseconds(this.value, this.unit);
  }

  private long convertToMilliseconds(double value, String unit) {
    long multiplier;
    switch (unit) {
      case "ms":
        multiplier = 1L;
        break;
      case "s":
        multiplier = 1000L;
        break;
      case "m":
        multiplier = 60L * 1000L;
        break;
      case "h":
        multiplier = 60L * 60L * 1000L;
        break;
      case "d":
        multiplier = 24L * 60L * 60L * 1000L;
        break;
      default:
        throw new IllegalArgumentException("Unknown unit: " + unit);
    }
    return (long) (value * multiplier);
  }

  /**
   * Gets the duration in milliseconds.
   *
   * @return the duration in milliseconds
   */
  public long getMilliseconds() {
    return milliseconds;
  }

  /**
   * Gets the original numeric value before conversion.
   *
   * @return the original numeric value
   */
  public double getValue() {
    return value;
  }

  /**
   * Gets the unit of measurement (ms, s, m, h, d).
   *
   * @return the unit of measurement
   */
  public String getUnit() {
    return unit;
  }

  @Override
  public Object value() {
    return originalValue;
  }

  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }

  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("type", type().name());
    object.addProperty("value", originalValue);
    object.addProperty("milliseconds", milliseconds);
    return object;
  }

  @Override
  public String toString() {
    return originalValue;
  }
} 