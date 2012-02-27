package nl.surfnet.bod.pages.manager;

import nl.surfnet.bod.pages.AbstractListPage;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class ListReservationPage extends AbstractListPage {
  private static final String PAGE = "/manager/reservations";

  public ListReservationPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static ListReservationPage get(RemoteWebDriver driver) {
    ListReservationPage page = new ListReservationPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public static ListReservationPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);
    return get(driver);
  }

}
