package nl.surfnet.bod.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.util.concurrent.TimeUnit;

import nl.surfnet.bod.pages.NewPhysicalGroupPage;
import nl.surfnet.bod.pages.ShowPhysicalGroupPage;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.google.common.io.Files;

public class WebDriver {

    protected FirefoxDriver driver;
    
    private static final String URL_UNDER_TEST = System.getProperty("selenium.test.url");

    public synchronized void initializeOnce() {
        if (driver == null) {
            this.driver = new FirefoxDriver();
            this.driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (driver != null) {
                        driver.quit();
                    }
                }
            });
        }
    }

    public void takeScreenshot(File screenshot) throws Exception {
        if (driver != null) {
            File temp = driver.getScreenshotAs(OutputType.FILE);
            Files.copy(temp, screenshot);
        }
    }
    
    

    public void createNewPhysicalGroup(String name) {
        NewPhysicalGroupPage page = NewPhysicalGroupPage.get(driver, URL_UNDER_TEST);
        page.sendName(name);
        page.sendInstitution("SURFnet B.V.");
        
        page.save();
    }

    public void verifyGroupWasCreated(String name) {
        ShowPhysicalGroupPage page = ShowPhysicalGroupPage.get(driver);
        String row = page.getNameRow();

        assertThat(row, containsString(name));
    }
}
