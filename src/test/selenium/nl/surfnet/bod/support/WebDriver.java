package nl.surfnet.bod.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.util.concurrent.TimeUnit;

import nl.surfnet.bod.pages.NewPhysicalGroupPage;
import nl.surfnet.bod.pages.ListPhysicalGroupPage;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.google.common.io.Files;

public class WebDriver {

    private static final String URL_UNDER_TEST = withEndingSlash(System.getProperty("selenium.test.url"));

    private FirefoxDriver driver;

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
        ListPhysicalGroupPage page = ListPhysicalGroupPage.get(driver);
        String row = page.getTable();

        assertThat(row, containsString(name));
    }

    private static String withEndingSlash(String path) {
        return path.endsWith("/") ? path : path + "/";
    }
}
