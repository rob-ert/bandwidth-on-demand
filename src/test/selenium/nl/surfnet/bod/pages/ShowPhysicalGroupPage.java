package nl.surfnet.bod.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ShowPhysicalGroupPage {

    @FindBy(id = "_s_org_surfnet_bod_domain_PhysicalResourceGroup_name_id")
    private WebElement name;
    
    public static ShowPhysicalGroupPage get(RemoteWebDriver driver) {
        ShowPhysicalGroupPage page = new ShowPhysicalGroupPage();
        PageFactory.initElements(driver, page);
        
        return page;
    }

    public String getNameRow() {
        return name.getText();
    }
}
