package nl.surfnet.bod.pages.noc;

import nl.surfnet.bod.event.LogEventType;
import nl.surfnet.bod.pages.AbstractListPage;

import org.joda.time.LocalDateTime;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

public class ListLogEventsPage extends AbstractListPage {
  private static final String PAGE = "/noc/logevents";

  public ListLogEventsPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static ListLogEventsPage get(RemoteWebDriver driver) {
    ListLogEventsPage page = new ListLogEventsPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public static ListLogEventsPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);
    return get(driver);
  }

  public Integer getNumberOfLogEvents() {
    return getRows().size();
  }

  public void logEventShouldBe(LocalDateTime created, String userId, LogEventType type, String entity) {

    WebElement row = findRow(userId, type.name(), entity);

    LocalDateTime logEventCreated = getLocalDateTimeFromRow(created.getYear(), row);

    long duration = created.getMillisOfDay() - logEventCreated.getMillisOfDay();
    // Allow a 15 second margin
    assertThat(duration, lessThan(15000L));
  }
}
