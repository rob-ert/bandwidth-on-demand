package nl.surfnet.bod.pages;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

public abstract class AbstractLogEventListPage extends AbstractListPage {

  public AbstractLogEventListPage(RemoteWebDriver driver) {
    super(driver);
  }

  public Integer getNumberOfLogEvents() {
    return getRows().size();
  }

  public void logEventShouldBe(DateTime created, String... fields) {

    WebElement row = findRow(fields);

    LocalDateTime logEventCreated = getLocalDateTimeFromRow(row);

    long duration = created.getMillisOfDay() - logEventCreated.getMillisOfDay();
    // Allow an x second margin
    assertThat(duration, lessThan(60000L));
  }
}
