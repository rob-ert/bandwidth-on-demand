package nl.surfnet.bod.pages.noc;

import nl.surfnet.bod.pages.AbstractListPage;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DashboardPage extends AbstractListPage {

  private static final String PAGE = "/noc";

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

  /**
   * Asserts the number of menu items, if it fails don't just update the number,
   * but add new test cases for the new menu
   */
  public void verifyNumberOfMenuItems() {
    assertThat(getCountMenuItems(), is(7));
  }

  public void verifyMenuReservations() {
    getMenuBar().findElement(By.xpath(".//a[contains(text(), 'Reservations')]")).click();
    ListReservationPage.get(getDriver()).verifyIsCurrentPage();
  }

  public void verifyMenuTeams() {
    getMenuBar().findElement(By.xpath(".//a[contains(text(), 'Teams')]")).click();
    ListVirtualResourceGroupPage.get(getDriver()).verifyIsCurrentPage();
  }

  public void verifyMenuInstitutes() {
    getMenuBar().findElement(By.xpath(".//a[contains(text(), 'Institutes')]")).click();
    ListPhysicalResourceGroupPage.get(getDriver()).verifyIsCurrentPage();
  }

  public void verifyMenuVirtualPorts() {
    getMenuBar().findElement(By.xpath(".//a[contains(text(), 'Virtual')]")).click();
    ListVirtualPortPage.get(getDriver()).verifyIsCurrentPage();
  }

  public void verifyMenuPhysicalPorts() {
    getMenuBar().findElement(By.xpath(".//a[contains(text(), 'Physical')]")).click();
    ListAllocatedPortsPage.get(getDriver()).verifyIsCurrentPage();
  }

  public void verifyMenuLogEvents() {
    getMenuBar().findElement(By.xpath(".//a[contains(text(), 'Log')]")).click();
    ListLogEventsPage.get(getDriver()).verifyIsCurrentPage();
  }

  public void verifyMenuOverview() {
    getMenuBar().findElement(By.xpath(".//a[contains(text(), 'Overview')]")).click();
    verifyIsCurrentPage();
  }

  public void verifyIsCurrentPage() {
    super.verifyIsCurrentPage(PAGE);
  }

}