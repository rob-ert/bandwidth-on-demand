package nl.surfnet.bod.support;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class Probes {
  private final WebDriver webDriver;

  public Probes(WebDriver webDriver) {
    this.webDriver = webDriver;
  }

  public void assertTextPresent(By locator, String textPresent) throws Exception {
    new Poller().check(new TextOnPageProbe(webDriver, locator, textPresent));
  }
}
