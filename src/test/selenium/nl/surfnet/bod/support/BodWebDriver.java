package nl.surfnet.bod.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.util.concurrent.TimeUnit;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.pages.ListPhysicalResourceGroupPage;
import nl.surfnet.bod.pages.NewPhysicalResourceGroupPage;

import org.hamcrest.Matcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.google.common.io.Files;

public class BodWebDriver {

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

    public void createNewPhysicalGroup(String name) throws Exception {
        NewPhysicalResourceGroupPage page = NewPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);
        page.sendName(name);
        page.sendInstitution("Utrecht");

        page.save();
    }

    private static String withEndingSlash(String path) {
        return path.endsWith("/") ? path : path + "/";
    }

    public void deletePhysicalGroup(PhysicalResourceGroup group) {
        ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);

        page.deleteByName(group.getName());
    }

    public void verifyGroupWasCreated(String name) {
        assertListTable(containsString(name));
    }

    public void verifyGroupWasDeleted(PhysicalResourceGroup group) {
        assertListTable(not(containsString(group.getName())));
    }

    private void assertListTable(Matcher<String> tableMatcher) {
        ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver);
        String row = page.getTable();

        assertThat(row, tableMatcher);
    }
}
