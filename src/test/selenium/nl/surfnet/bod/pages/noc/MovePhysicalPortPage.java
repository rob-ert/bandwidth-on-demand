package nl.surfnet.bod.pages.noc;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

public final class MovePhysicalPortPage {

  private final RemoteWebDriver driver;

  @FindBy(id = "_c_new_physicalport")
  private WebElement newPhysicalPort;

  @FindBy(css = "input[type=submit]")
  private WebElement moveButton;

  @FindBy(id = "nms_port_id")
  private WebElement nmsPortIdDiv;

  @FindBy(id = "institute_id")
  private WebElement instituteDiv;

  @FindBy(id = "nrvp_id")
  private WebElement nrVirutalPortsDiv;

  @FindBy(id = "nrar_id")
  private WebElement nrActiveReservationsDiv;

  @FindBy(id = "nrr_id")
  private WebElement nrReservationsDiv;

  private MovePhysicalPortPage(RemoteWebDriver driver) {
    this.driver = driver;
  }

  public static MovePhysicalPortPage get(RemoteWebDriver driver) {
    MovePhysicalPortPage page = new MovePhysicalPortPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public void selectNewPhysicalPort(String elementId) {
    new Select(newPhysicalPort).selectByValue(elementId);
  }

  public void movePort() {
    moveButton.click();
  }

  public String getNmsPortId() {
    return getOutputText(nmsPortIdDiv);
  }

  public String getInstituteName() {
    return getOutputText(instituteDiv);
  }

  public Integer getNumberOfVirtualPorts() {
    return Integer.valueOf(getOutputText(nrVirutalPortsDiv));
  }

  public Integer getNumberOfReservations() {
    return Integer.valueOf(getOutputText(nrReservationsDiv));
  }

  public Integer getNumberOfActiveReservations() {
    return Integer.valueOf(getOutputText(nrActiveReservationsDiv));
  }

  private String getOutputText(WebElement element) {
    return element.findElement(By.tagName("output")).getText();
  }
}
