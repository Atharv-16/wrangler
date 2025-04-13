/*
 * Copyright © 2016-2017 Cask Data, Inc.
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

import io.cdap.wrangler.api.annotations.PublicEvolving;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class description here.
 */
@PublicEvolving
public class TimeDuration {
  private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d+)\\s*(ms|s|m|h|d)$");
  private final long duration;
  private final TimeUnit unit;

  public TimeDuration(long duration, TimeUnit unit) {
    this.duration = duration;
    this.unit = unit;
  }

  public TimeDuration(String timeStr) {
    Matcher matcher = TIME_PATTERN.matcher(timeStr.trim());
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
        "Invalid time duration format. Expected format: <number><unit> where unit is one of: ms, s, m, h, d");
    }

    long value = Long.parseLong(matcher.group(1));
    String unitStr = matcher.group(2);

    switch (unitStr) {
      case "ms":
        this.unit = TimeUnit.MILLISECONDS;
        this.duration = value;
        break;
      case "s":
        this.unit = TimeUnit.SECONDS;
        this.duration = value;
        break;
      case "m":
        this.unit = TimeUnit.MINUTES;
        this.duration = value;
        break;
      case "h":
        this.unit = TimeUnit.HOURS;
        this.duration = value;
        break;
      case "d":
        this.unit = TimeUnit.DAYS;
        this.duration = value;
        break;
      default:
        throw new IllegalArgumentException("Unknown time unit: " + unitStr);
    }
  }

  public long getDuration() {
    return duration;
  }

  public TimeUnit getUnit() {
    return unit;
  }

  public long toMilliseconds() {
    return unit.toMillis(duration);
  }
}
