package nl.surfnet.bod.pages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class AbstractPhysicalPortListPage extends AbstractListPage {

  public AbstractPhysicalPortListPage(RemoteWebDriver driver) {
    super(driver);
  }

  public WebElement verifyPhysicalPortWasAllocated(String networkElementPk, String label) {

    return findRow(networkElementPk, label);
  }

  public void verifyPhysicalPortHasEnabledUnallocateIcon(String networkElementPk, String label) {
    WebElement row = verifyPhysicalPortWasAllocated(networkElementPk, label);

    try {
      row.findElement(By.cssSelector("span.disabled-icon"));
      assertThat("PhysicalPort should not contain disabled unallocate Icon", false);
    }
    catch (NoSuchElementException e) {
      // Expected
    }
  }

  public void verifyPhysicalPortHasDisabledUnallocateIcon(String networkElementPk, String label, String toolTipText) {

    WebElement row = verifyPhysicalPortWasAllocated(networkElementPk, label);

    WebElement unAllocateElement = row.findElement(By.cssSelector("span.disabled-icon"));
    String deleteTooltip = unAllocateElement.getAttribute("data-original-title");

    assertThat(deleteTooltip, containsString(toolTipText));
  }

  public void verifyPhysicalPortIsNotOnUnallocatedPage(String networkElementPk, String label) {
    try {
      verifyPhysicalPortWasAllocated(networkElementPk, label);
      assertThat("PhysicalPort should not be listed on unAllocated page", false);
    }
    catch (NoSuchElementException e) {
      // Expected
    }
  }

}
