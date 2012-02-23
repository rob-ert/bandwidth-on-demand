package nl.surfnet.bod.pages.manager;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class EditVirtualPortPage extends NewVirtualPortPage {

  public static EditVirtualPortPage get(RemoteWebDriver driver) {
    EditVirtualPortPage page = new EditVirtualPortPage();
    PageFactory.initElements(driver, page);

    return page;
  }
}
