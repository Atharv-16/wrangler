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
 * A {@link Token} implementation for representing time durations with units.
 */
@PublicEvolving
public class TimeDuration implements Token {
  private static final Pattern TIME_DURATION_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)(ms|s|m|h|d)");
  private final String originalValue;
  private final double milliseconds;

  public TimeDuration(String value) {
    this.originalValue = value;
    this.milliseconds = parseMilliseconds(value);
  }

  private double parseMilliseconds(String value) {
    Matcher matcher = TIME_DURATION_PATTERN.matcher(value);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid time duration format: " + value);
    }

    double duration = Double.parseDouble(matcher.group(1));
    String unit = matcher.group(2);

    switch (unit) {
      case "ms":
        return duration;
      case "s":
        return duration * 1000;
      case "m":
        return duration * 60 * 1000;
      case "h":
        return duration * 60 * 60 * 1000;
      case "d":
        return duration * 24 * 60 * 60 * 1000;
      default:
        throw new IllegalArgumentException("Unsupported time unit: " + unit);
    }
  }

  @Override
  public Object value() {
    return milliseconds;
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

  /**
   * Gets the duration in milliseconds.
   *
   * @return duration in milliseconds
   */
  public double getMilliseconds() {
    return milliseconds;
  }

  /**
   * Gets the duration in seconds.
   *
   * @return duration in seconds
   */
  public double getSeconds() {
    return milliseconds / 1000.0;
  }

  /**
   * Gets the duration in minutes.
   *
   * @return duration in minutes
   */
  public double getMinutes() {
    return milliseconds / (60.0 * 1000.0);
  }

  /**
   * Gets the duration in hours.
   *
   * @return duration in hours
   */
  public double getHours() {
    return milliseconds / (60.0 * 60.0 * 1000.0);
  }

  /**
   * Gets the duration in days.
   *
   * @return duration in days
   */
  public double getDays() {
    return milliseconds / (24.0 * 60.0 * 60.0 * 1000.0);
  }

  @Override
  public String toString() {
    return originalValue;
  }
} 