package nl.surfnet.bod.pages;

import nl.surfnet.bod.support.Probes;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class NewPhysicalResourceGroupPage {

    private static final String PAGE = "noc/physicalresourcegroups?form";

    private final Probes probes;

    @FindBy(id = "_name_id")
    private WebElement nameInput;

    @FindBy(css = "input[name='institutionName_search']")
    private WebElement institutionInput;

    @FindBy(css = "input[type='submit']")
    private WebElement saveButton;

    public NewPhysicalResourceGroupPage(WebDriver driver) {
        this.probes = new Probes(driver);
    }

    public static NewPhysicalResourceGroupPage get(RemoteWebDriver driver, String host) {
        driver.get(host + PAGE);
        NewPhysicalResourceGroupPage page = new NewPhysicalResourceGroupPage(driver);
        PageFactory.initElements(driver, page);

        return page;
    }

    public void sendName(String name) {
        nameInput.clear();
        nameInput.sendKeys(name);
    }

    public void sendInstitution(String institution) throws Exception {
        institutionInput.clear();
        institutionInput.sendKeys(institution);

        probes.assertTextPresent(By.className("as-results"), institution);

        institutionInput.sendKeys("\t");
        institutionInput.sendKeys("\n");
    }

    public void save() {
        saveButton.click();
    }

}
