package nl.surfnet.bod.support;

import nl.surfnet.bod.pages.AbstractListPage;

public abstract class AbstractBoDWebDriver<T extends AbstractListPage> {

  public void switchToAppManager() {
    switchTo("Application Manager");
  }

  public void switchToNoc() {
    switchTo("NOC Engineer");
  }

  public void switchToManager(final String name) {
    switchTo("BoD Administrator", name);
  }

  public void switchToManager() {
    switchTo("BoD Administrator");
  }

  public void switchToUser() {
    switchTo("User");
  }

  protected abstract T getDashboardPage();

  private void switchTo(String... role) {
    getDashboardPage().clickSwitchRole(role);
  }

}
