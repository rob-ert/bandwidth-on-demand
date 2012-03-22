package nl.surfnet.bod.pages.user;

import nl.surfnet.bod.pages.AbstractListPage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class RequestNewVirtualPortSelectTeamPage extends AbstractListPage {
  private static final String PAGE = "request";

  private RequestNewVirtualPortSelectTeamPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static RequestNewVirtualPortSelectTeamPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);

    RequestNewVirtualPortSelectTeamPage page = new RequestNewVirtualPortSelectTeamPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public RequestNewVirtualPortSelectInstitutePage selectInstitute(String institute) {
    WebElement row = findRow(institute);
    row.findElement(By.tagName("a")).click();

    return RequestNewVirtualPortSelectInstitutePage.get(driver);
  }

  @Override
  public void delete(String... fields) {
    throw new UnsupportedOperationException("Can not delete...");
  }

}
