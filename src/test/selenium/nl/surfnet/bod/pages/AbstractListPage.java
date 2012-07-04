/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.pages;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Uninterruptibles;

public class AbstractListPage extends AbstractPage {

  @FindBy(css = "table.table tbody")
  private WebElement table;

  public AbstractListPage(RemoteWebDriver driver) {
    super(driver);
  }

  public String getTable() {
    return table.getText();
  }

  public void delete(String... fields) {
    deleteForIcon("icon-remove", fields);
  }

  public void deleteAndVerifyAlert(String alertText, String... fields) {
    deleteForIconAndVerifyAlert("icon-remove", alertText, fields);
  }

  protected void deleteForIconAndVerifyAlert(String icon, String alertText, String... fields) {
    delete(icon, fields);
    Alert alert = getDriver().switchTo().alert();
    alert.getText().contains(alertText);
    alert.accept();

    // wait for the reload, row should be gone..
    Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
  }

  protected void deleteForIcon(String icon, String... fields) {
    delete(icon, fields);
    getDriver().switchTo().alert().accept();

    // wait for the reload, row should be gone..
    Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
  }

  protected void editRow(String... fields) {
    clickRowIcon("icon-pencil", fields);
  }

  protected void clickRowIcon(String icon, String... fields) {
    findRow(fields).findElement(By.cssSelector("a i[class~=" + icon + "]")).click();
  }

  private void delete(String icon, String... fields) {
    WebElement row = findRow(fields);

    WebElement deleteButton = row.findElement(By.cssSelector(String.format("a i[class~=%s]", icon)));
    deleteButton.click();
  }

  public boolean isTableEmpty() {
    try {
      table.findElements(By.tagName("tr"));
      return false;
    }
    catch (NoSuchElementException e) {
      return true;
    }
  }

  public List<WebElement> getRows() {
    return table.findElements(By.cssSelector("tbody tr"));
  }

  public WebElement findRow(String... fields) {
    List<WebElement> rows = getRows();

    for (final WebElement row : rows) {
      if (containsAll(row, fields)) {
        return row;
      }
    }
    throw new NoSuchElementException(String.format("row with fields '%s' not found in rows: '%s'",
        Joiner.on(',').join(fields),
        Joiner.on(" | ").join(Iterables.transform(rows, new Function<WebElement, String>() {
          @Override
          public String apply(WebElement row) {
            return row.getText();
          }
        }))));
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

  /**
   * Overrides the default selected table by the given one in case there are
   * multiple tables on a page.
   * 
   * @param table
   *          Table to set.
   */
  protected void setTable(WebElement table) {
    this.table = table;
  }

}
