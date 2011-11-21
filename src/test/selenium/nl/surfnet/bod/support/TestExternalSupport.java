package nl.surfnet.bod.support;

import org.junit.Before;
import org.junit.Rule;

public abstract class TestExternalSupport {

    private static WebDriver webDriver = new WebDriver();

    @Rule
    public Screenshotter screenshotter = new Screenshotter(webDriver);

    @Before
    public final void initialize() {
        webDriver.initializeOnce();
    }

    protected WebDriver getWebDriver() {
        return webDriver;
    }
}
