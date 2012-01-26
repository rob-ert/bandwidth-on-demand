package nl.surfnet.bod.pages;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class AbstractListPage {

  private final RemoteWebDriver driver;

  @FindBy(css = "table.zebra-striped tbody")
  private WebElement table;

  public AbstractListPage(RemoteWebDriver driver) {
    this.driver = driver;
  }

  public String getTable() {
    return table.getText();
  }

  public void delete(String... fields) {
    WebElement row = findRow(fields);

    WebElement deleteButton = row.findElement(By.cssSelector("input[type=image]"));
    deleteButton.click();
    driver.switchTo().alert().accept();
  }

  protected WebElement findRow(String... fields) {
    List<WebElement> rows = table.findElements(By.tagName("tr"));

    for (final WebElement row : rows) {

      if (containsAll(row, fields)) {
        return row;
      }
    }

    throw new AssertionError(String.format("row with name '%s' not found", fields.toString()));
  }

  private boolean containsAll(final WebElement row, String... fields) {
    return Iterables.all(Arrays.asList(fields), new Predicate<String>() {
      @Override
      public boolean apply(String field) {
        return row.getText().contains(field);
      }
    });
  }

  public boolean containsAnyItems() {
    try {
      table.getText();
    }
    catch (NoSuchElementException e) {
      return false;
    }

    return true;
  }
}
