package nl.surfnet.bod.pages.user;

import nl.surfnet.bod.pages.AbstractListPage;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class ListVirtualPortPage extends AbstractListPage {

  private static final String PAGE = "/virtualports";

  public ListVirtualPortPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static ListVirtualPortPage get(RemoteWebDriver driver) {
    ListVirtualPortPage page = new ListVirtualPortPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public static ListVirtualPortPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);
    return get(driver);
  }

  public EditVirtualPortPage edit(String oldLabel) {
    editRow(oldLabel);
    return EditVirtualPortPage.get(driver);
  }
}
