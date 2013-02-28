package nl.surfnet.bod.pages.noc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import nl.surfnet.bod.pages.AbstractPhysicalPortListPage;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public abstract class AbstractPhysicalPortListNocPage extends AbstractPhysicalPortListPage {

  public AbstractPhysicalPortListNocPage(RemoteWebDriver driver) {
    super(driver);
  }

  public void verifyPhysicalPortHasDisabledUnallocateIcon(String nmsPortId, String label, String toolTipText) {

    WebElement row = verifyPhysicalPortWasAllocated(nmsPortId, label);

    WebElement unAllocateElement = row.findElement(By.cssSelector("span.disabled-icon"));
    String deleteTooltip = unAllocateElement.getAttribute("data-original-title");

    assertThat(deleteTooltip, containsString(toolTipText));
  }

  public void verifyPhysicalPortHasEnabledUnallocateIcon(String nmsPortId, String label) {
    WebElement row = verifyPhysicalPortWasAllocated(nmsPortId, label);

    try {
      row.findElement(By.cssSelector("span.disabled-icon"));
      fail("PhysicalPort should not contain disabled unallocate Icon");
    }
    catch (NoSuchElementException e) {
      // Expected
    }
  }

}
