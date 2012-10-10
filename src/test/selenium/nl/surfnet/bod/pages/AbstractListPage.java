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
import org.springframework.util.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Uninterruptibles;

import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AbstractListPage extends AbstractPage {

  @FindBy(css = "table.table tbody")
  private WebElement table;

  @FindBy(id = "si_id")
  private WebElement searchInputField;

  @FindBy(id = "sb_id")
  private WebElement searchButton;

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
    throw new NoSuchElementException(String.format("row with fields '%s' not found in rows: '%s'", Joiner.on(',').join(
        fields), Joiner.on(" | ").join(Iterables.transform(rows, new Function<WebElement, String>() {
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

  public Integer getNumberOfRows() {
    int numberOfRows;
    try {
      numberOfRows = getRows().size();
    }
    catch (NoSuchElementException e) {
      numberOfRows = 0;
    }

    return numberOfRows;
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

  public void verifyRowsWithLabelExists(String... labels) {
    for (String label : labels) {
      findRow(label);
    }
  }

  public void verifyRowsWithLabelDoesNotExist(String... labels) {
    for (String label : labels) {
      try {
        findRow(label);
        fail(String.format("Row related to [%s] exists, but should not be visible", label));
      }
      catch (NoSuchElementException e) {
        // as expected
      }
    }
  }

  public void search(String searchString) {
    if (StringUtils.hasText(searchString)) {
      searchInputField.sendKeys(searchString);
      searchButton.click();
    }
  }

  public void verifyRowsBySearch(String searchString, String... labels) {
    search(searchString);

    int expectedAmount = labels == null ? 0 : labels.length;
    assertThat(getNumberOfRows(), is(expectedAmount));

    verifyRowsWithLabelExists(labels);
  }

  public void verifyAmountOfRowsWithLabel(int expectedAmount, String... labels) {
    int matchedRows = 0;

    for (final WebElement row : getRows()) {
      if (containsAll(row, labels)) {
        matchedRows++;
      }
    }

    assertThat(matchedRows, is(expectedAmount));
  }

  public int getNumberFromRowWithLinkAndClick(String rowLabel, String linkPart, String tooltipTitle) {
    WebElement rowWithLink = findRow(rowLabel);

    WebElement link = rowWithLink.findElement(By.xpath(String.format(
        ".//a[contains(@href, '%s') and contains(@data-original-title, '%s')]", linkPart, tooltipTitle)));

    int number = Integer.parseInt(link.getText());

    link.click();

    return number;
  }
}
