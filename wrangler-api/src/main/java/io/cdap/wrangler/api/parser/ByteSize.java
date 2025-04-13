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
 * A token type that represents byte sizes with units (e.g., "10KB", "1.5MB").
 */
@PublicEvolving
public class ByteSize implements Token {
  private static final Pattern BYTE_SIZE_PATTERN = Pattern.compile(
    "^(\\d+(?:\\.\\d+)?)(B|KB|MB|GB|TB)$",
    Pattern.CASE_INSENSITIVE
  );
  private final String originalValue;
  private final double value;
  private final String unit;
  private final long bytes;

  public ByteSize(String value) {
    this.originalValue = value;
    Matcher matcher = BYTE_SIZE_PATTERN.matcher(value);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid byte size format: " + value);
    }

    this.value = Double.parseDouble(matcher.group(1));
    this.unit = matcher.group(2).toUpperCase();
    this.bytes = convertToBytes(this.value, this.unit);
  }

  private long convertToBytes(double value, String unit) {
    long multiplier;
    switch (unit.toUpperCase()) {
      case "B":
        multiplier = 1L;
        break;
      case "KB":
        multiplier = 1024L;
        break;
      case "MB":
        multiplier = 1024L * 1024L;
        break;
      case "GB":
        multiplier = 1024L * 1024L * 1024L;
        break;
      case "TB":
        multiplier = 1024L * 1024L * 1024L * 1024L;
        break;
      default:
        throw new IllegalArgumentException("Unknown unit: " + unit);
    }
    return (long) (value * multiplier);
  }

  /**
   * Gets the size in bytes.
   *
   * @return the size in bytes
   */
  public long getBytes() {
    return bytes;
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
   * Gets the unit of measurement (B, KB, MB, GB, TB).
   *
   * @return the unit of measurement
   */
  public String getUnit() {
    return unit;
  }

  /**
   * Gets the original value as a string.
   * @return The original value
   */
  public String getOriginalValue() {
    return originalValue;
  }

  @Override
  public Object value() {
    return originalValue;
  }

  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }

  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("type", type().name());
    object.addProperty("value", originalValue);
    object.addProperty("bytes", bytes);
    return object;
  }

  @Override
  public String toString() {
    return originalValue;
  }

  /**
   * Converts the byte size to bytes.
   * @return The size in bytes
   */
  private double toBytes() {
    switch (unit.toUpperCase()) {
      case "B":
        return value;
      case "KB":
        return value * 1024.0;
      case "MB":
        return value * 1024.0 * 1024.0;
      case "GB":
        return value * 1024.0 * 1024.0 * 1024.0;
      case "TB":
        return value * 1024.0 * 1024.0 * 1024.0 * 1024.0;
      default:
        throw new IllegalArgumentException("Invalid unit: " + unit);
    }
  }

  /**
   * Converts the byte size to a different unit.
   * 
   * @param targetUnit The target unit to convert to
   * @return The converted value in the target unit
   * @throws IllegalArgumentException if the target unit is invalid
   */
  public double convertTo(String targetUnit) {
    double bytes = toBytes();
    switch (targetUnit.toUpperCase()) {
      case "B":
        return bytes;
      case "KB":
        return bytes / 1024.0;
      case "MB":
        return bytes / (1024.0 * 1024.0);
      case "GB":
        return bytes / (1024.0 * 1024.0 * 1024.0);
      case "TB":
        return bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0);
      default:
        throw new IllegalArgumentException("Invalid target unit: " + targetUnit);
    }
  }
}
