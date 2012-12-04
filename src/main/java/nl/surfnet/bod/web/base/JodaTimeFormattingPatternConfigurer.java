/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.web.base;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.joda.DateTimeParser;
import org.springframework.format.datetime.joda.JodaTimeFormatterRegistrar;
import org.springframework.format.datetime.joda.ReadableInstantPrinter;
import org.springframework.format.datetime.joda.ReadablePartialPrinter;

import static com.google.common.base.Strings.nullToEmpty;

public class JodaTimeFormattingPatternConfigurer extends JodaTimeFormatterRegistrar {

  private String dateTimePattern;
  private String datePattern;
  private String timePattern;

  @Override
  public void registerFormatters(FormatterRegistry registry) {
    super.registerFormatters(registry);

    DateTimeFormatter jodaDateFormatter = getJodaDateFormatter();
    if (jodaDateFormatter != null) {
      register(registry, LocalDate.class, jodaDateFormatter);
    }

    DateTimeFormatter jodaTimeFormatter = getJodaTimeFormatter();
    if (jodaTimeFormatter != null) {
      register(registry, LocalTime.class, jodaTimeFormatter);
    }

    DateTimeFormatter jodaDateTimeFormatter = getJodaDateTimeFormatter();
    if (jodaDateTimeFormatter != null) {
      register(registry, DateTime.class, jodaDateTimeFormatter);
    }
  }

  private void register(FormatterRegistry registry, Class<?> clazz, DateTimeFormatter formatter) {
    if (ReadablePartial.class.isAssignableFrom(clazz)) {
      registry.addFormatterForFieldType(clazz, new ReadablePartialPrinter(formatter), new DateTimeParser(formatter));
    }
    else if (ReadableInstant.class.isAssignableFrom(clazz)) {
      registry.addFormatterForFieldType(clazz, new ReadableInstantPrinter(formatter), new DateTimeParser(formatter));
    }
  }

  private DateTimeFormatter getJodaDateFormatter() {
    if (nullToEmpty(datePattern).isEmpty()) {
      return null;
    }
    return DateTimeFormat.forPattern(datePattern);
  }

  private DateTimeFormatter getJodaTimeFormatter() {
    if (nullToEmpty(timePattern).isEmpty()) {
      return null;
    }
    return DateTimeFormat.forPattern(timePattern);
  }

  private DateTimeFormatter getJodaDateTimeFormatter() {
    if (nullToEmpty(dateTimePattern).isEmpty()) {
      return null;
    }
    return DateTimeFormat.forPattern(dateTimePattern);
  }

  public JodaTimeFormattingPatternConfigurer setDateTimePattern(String pattern) {
    this.dateTimePattern = pattern;
    return this;
  }

  public JodaTimeFormattingPatternConfigurer setTimePattern(String pattern) {
    this.timePattern = pattern;
    return this;
  }

  public JodaTimeFormattingPatternConfigurer setDatePattern(String pattern) {
    this.datePattern = pattern;
    return this;
  }

}
