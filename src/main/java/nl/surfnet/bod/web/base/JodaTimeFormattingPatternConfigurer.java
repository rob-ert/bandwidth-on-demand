/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.web.base;

import static com.google.common.base.Strings.nullToEmpty;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.joda.DateTimeParser;
import org.springframework.format.datetime.joda.JodaTimeFormatterRegistrar;
import org.springframework.format.datetime.joda.ReadablePartialPrinter;

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
      register(registry, LocalDateTime.class, jodaDateTimeFormatter);
    }
  }

  private void register(FormatterRegistry registry, Class<?> clazz, DateTimeFormatter formatter) {
    registry.addFormatterForFieldType(clazz, new ReadablePartialPrinter(formatter), new DateTimeParser(formatter));
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
