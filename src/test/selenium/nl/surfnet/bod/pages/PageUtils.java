package nl.surfnet.bod.pages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.surfnet.bod.web.WebUtils;

import org.joda.time.LocalDateTime;

import com.google.common.base.Optional;

public final class PageUtils {

  private PageUtils() {
  }

  public static Optional<LocalDateTime> extractDateTime(String text) {
    Matcher matcher = Pattern.compile(".*(\\d{4}-\\d{2}-\\d{2} \\d{1,2}:\\d{2}:\\d{2}).*").matcher(text);

    if (!matcher.matches()) return Optional.absent();

    return Optional.of(WebUtils.DEFAULT_DATE_TIME_FORMATTER.parseLocalDateTime(matcher.group(1)));
  }

}
