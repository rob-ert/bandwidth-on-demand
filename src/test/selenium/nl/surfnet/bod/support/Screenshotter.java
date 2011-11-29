package nl.surfnet.bod.support;

import java.io.File;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class Screenshotter implements TestRule {
  private static final File SCREENSHOTDIR = new File("target/selenium/screenshots");

  static {
    SCREENSHOTDIR.mkdirs();
  }

  private final BodWebDriver webDriver;

  public Screenshotter(BodWebDriver driver) {
    this.webDriver = driver;
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          base.evaluate();
        }
        catch (Throwable t) {
          t.printStackTrace();
          webDriver.takeScreenshot(new File(SCREENSHOTDIR, description.getMethodName() + ".png"));
          throw t;
        }
      }
    };
  }
}
