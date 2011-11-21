package nl.surfnet.bod.support;

import java.io.File;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class Screenshotter implements MethodRule {
    private static final File SCREENSHOTDIR = new File("target/selenium/screenshots");

    static {
        SCREENSHOTDIR.mkdirs();
    }

    private final WebDriver webDriver;

    public Screenshotter(WebDriver driver) {
        this.webDriver = driver;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                }
                catch (Throwable t) {
                    t.printStackTrace();
                    webDriver.takeScreenshot(new File(SCREENSHOTDIR, method.getName() + ".png"));
                    throw t;
                }
            }
        };
    }
}
