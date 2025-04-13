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
 * A {@link Token} implementation for representing byte sizes with units.
 */
@PublicEvolving
public class ByteSize implements Token {
  private static final Pattern BYTE_SIZE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)(KB|MB|GB|TB|B)", Pattern.CASE_INSENSITIVE);
  private final String originalValue;
  private final double bytes;

  public ByteSize(String value) {
    this.originalValue = value;
    this.bytes = parseBytes(value);
  }

  private double parseBytes(String value) {
    Matcher matcher = BYTE_SIZE_PATTERN.matcher(value);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid byte size format: " + value);
    }

    double size = Double.parseDouble(matcher.group(1));
    String unit = matcher.group(2).toUpperCase();

    switch (unit) {
      case "B":
        return size;
      case "KB":
        return size * 1024;
      case "MB":
        return size * 1024 * 1024;
      case "GB":
        return size * 1024 * 1024 * 1024;
      case "TB":
        return size * 1024 * 1024 * 1024 * 1024;
      default:
        throw new IllegalArgumentException("Unsupported byte unit: " + unit);
    }
  }

  @Override
  public Object value() {
    return bytes;
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

  /**
   * Gets the size in bytes.
   *
   * @return size in bytes
   */
  public double getBytes() {
    return bytes;
  }

  /**
   * Gets the size in kilobytes.
   *
   * @return size in kilobytes
   */
  public double getKilobytes() {
    return bytes / 1024.0;
  }

  /**
   * Gets the size in megabytes.
   *
   * @return size in megabytes
   */
  public double getMegabytes() {
    return bytes / (1024.0 * 1024.0);
  }

  /**
   * Gets the size in gigabytes.
   *
   * @return size in gigabytes
   */
  public double getGigabytes() {
    return bytes / (1024.0 * 1024.0 * 1024.0);
  }

  /**
   * Gets the size in terabytes.
   *
   * @return size in terabytes
   */
  public double getTerabytes() {
    return bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0);
  }

  @Override
  public String toString() {
    return originalValue;
  }
} 