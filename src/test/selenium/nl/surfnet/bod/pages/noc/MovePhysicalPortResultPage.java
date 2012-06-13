package nl.surfnet.bod.pages.noc;

import nl.surfnet.bod.pages.AbstractListPage;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class MovePhysicalPortResultPage extends AbstractListPage {

  public MovePhysicalPortResultPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static MovePhysicalPortResultPage get(RemoteWebDriver driver) {
    MovePhysicalPortResultPage page = new MovePhysicalPortResultPage(driver);

    PageFactory.initElements(driver, page);

    return page;
  }

  public int getNumberOfReservations() {
    return getRows().size();
  }
}
