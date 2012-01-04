package nl.surfnet.bod.web;

import static com.google.common.base.Strings.nullToEmpty;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.joda.DateTimeParser;
import org.springframework.format.datetime.joda.JodaTimeFormattingConfigurer;
import org.springframework.format.datetime.joda.ReadablePartialPrinter;

public class JodaTimeFormattingPatternConfigurer extends JodaTimeFormattingConfigurer {

  private String dateTimePattern;
  private String datePattern;
  private String timePattern;

  @Override
  public void installJodaTimeFormatting(FormatterRegistry registry) {
    super.installJodaTimeFormatting(registry);

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

  public JodaTimeFormattingPatternConfigurer setDateTimePattern(String dateTimePattern) {
    this.dateTimePattern = dateTimePattern;
    return this;
  }

  public JodaTimeFormattingPatternConfigurer setTimePattern(String timePattern) {
    this.timePattern = timePattern;
    return this;
  }

  public JodaTimeFormattingPatternConfigurer setDatePattern(String datePattern) {
    this.datePattern = datePattern;
    return this;
  }

}
