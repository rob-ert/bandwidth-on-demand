package nl.surfnet.bod.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class RequestNewVirtualPortSelectInstitutePage extends AbstractListPage {
  private static final String PAGE = "virtualports/request";

  private RequestNewVirtualPortSelectInstitutePage(RemoteWebDriver driver) {
    super(driver);
  }

  public static RequestNewVirtualPortSelectInstitutePage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);

    RequestNewVirtualPortSelectInstitutePage page = new RequestNewVirtualPortSelectInstitutePage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public RequestNewVirtualPortRequestPage selectInstitute(String institute) {
    WebElement row = findRow(institute);

    row.findElement(By.linkText("Select institute")).click();

    return RequestNewVirtualPortRequestPage.get(driver);
  }

  @Override
  public void delete(String... fields) {
    throw new UnsupportedOperationException("Can not delete...");
  }

}
