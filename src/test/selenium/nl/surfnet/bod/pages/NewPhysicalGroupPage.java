package nl.surfnet.bod.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class NewPhysicalGroupPage {

    private static final String PAGE = "noc/physicalresourcegroups?form";

    @FindBy(id = "_name_id")
    private WebElement nameInput;

    @FindBy(id = "_institutionName_id")
    private WebElement institutionInput;

    @FindBy(css = "input[type='submit']")
    private WebElement saveButton;

    public static NewPhysicalGroupPage get(RemoteWebDriver driver, String host) {
        driver.get(host + PAGE);
        NewPhysicalGroupPage page = new NewPhysicalGroupPage();
        PageFactory.initElements(driver, page);

        return page;
    }

    public void sendName(String name) {
        nameInput.clear();
        nameInput.sendKeys(name);
    }

    public void sendInstitution(String institution) {
        institutionInput.clear();
        institutionInput.sendKeys(institution);
    }

    public void save() {
        saveButton.click();
    }

}
