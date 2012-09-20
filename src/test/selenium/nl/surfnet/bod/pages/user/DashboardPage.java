package nl.surfnet.bod.pages.user;

import java.util.List;

import nl.surfnet.bod.pages.AbstractListPage;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class DashboardPage extends AbstractListPage {

  private static final String PAGE = "/user";

  public DashboardPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static DashboardPage get(RemoteWebDriver driver) {
    DashboardPage page = new DashboardPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public static DashboardPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);
    return get(driver);
  }
  public List<String> getTeams() {
    return Lists.newArrayList(Iterables.transform(getRows(), new Function<WebElement, String>() {
      @Override
      public String apply(WebElement row) {
        return row.findElements(By.tagName("td")).get(0).getText();
      }
    }));
  }

  public void selectInstitute(String team) {
    try {
      getDriver().findElementByPartialLinkText(team).click();
    }
    catch (NoSuchElementException e) {
      clickRowIcon("icon-envelope", team);
    }
  }

}
