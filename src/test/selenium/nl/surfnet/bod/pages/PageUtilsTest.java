package nl.surfnet.bod.pages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import com.google.common.base.Optional;

public class PageUtilsTest {

  @Test
  public void shouldBeAbleToExtractDateWhenHourHasOnlyOneDigit() {
    Optional<LocalDateTime> extractDateTime = PageUtils.extractDateTime("asfaf asdfasdf asdf 2012-07-25 9:28:49 asdf asdf");

    assertThat(extractDateTime.isPresent(), is(true));
  }

  @Test
  public void shouldBeAbleToExtractDateWhenHourHasTwoDigits() {
    Optional<LocalDateTime> extractDateTime = PageUtils.extractDateTime("asfaf asdfasdf asdf 2012-07-25 12:28:49 asdf asdf");

    assertThat(extractDateTime.isPresent(), is(true));
  }

  @Test
  public void shouldBeAbleToExtractADate() {
    Optional<LocalDateTime> extractDateTime = PageUtils.extractDateTime("asfaf asdfasdf asdf 201207a25 1:2849 asdf asdf");

    assertThat(extractDateTime.isPresent(), is(false));
  }
}
