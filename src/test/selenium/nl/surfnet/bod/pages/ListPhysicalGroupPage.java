package nl.surfnet.bod.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ListPhysicalGroupPage {

    @FindBy(css = "table.zebra-striped tbody")
    private WebElement table;

    public static ListPhysicalGroupPage get(RemoteWebDriver driver) {
        ListPhysicalGroupPage page = new ListPhysicalGroupPage();
        PageFactory.initElements(driver, page);

        return page;
    }

    public String getTable() {
        return table.getText();
    }
}
