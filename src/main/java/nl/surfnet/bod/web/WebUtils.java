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
package nl.surfnet.bod.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.HtmlUtils;

@Component
public final class WebUtils {

  @Value("${external.bod.url}")
  private String externalBodUrl;

  public static final String CREATE = "/create";
  public static final String SHOW = "/show";
  public static final String EDIT = "/edit";
  public static final String UPDATE = "/update";
  public static final String DELETE = "/delete";
  public static final String LIST = "/list";

  /**
   * Used to distinguish between the model and a listmodel.
   */
  public static final String LIST_POSTFIX = "List";
  public static final String MAX_PAGES_KEY = "maxPages";
  public static final String PAGE_KEY = "page";
  public static final String ID_KEY = "id";

  public static final int MAX_ITEMS_PER_PAGE = 15;

  public static final String INFO_MESSAGES_KEY = "infoMessages";

  private WebUtils() {
  }

  public static int calculateFirstPage(Integer page) {
    return page == null ? 0 : (page.intValue() - 1) * MAX_ITEMS_PER_PAGE;
  }

  public static int calculateMaxPages(long totalEntries) {
    float nrOfPages = (float) totalEntries / MAX_ITEMS_PER_PAGE;
    return (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
  }

  public String getExternalBodUrl() {
    return externalBodUrl;
  }

  /**
   * Adds a flashMessage which will survive a redirect.
   * 
   * @param redirectAttributes
   *          Model to add the message to
   * @param message
   *          Message to add
   * @param messageArg
   *          Arguments to parse into the message, using
   *          {@link String#format(String, Object...)}
   */
  public static void addInfoMessage(RedirectAttributes redirectAttributes, String message, Object... messageArg) {

    redirectAttributes.addFlashAttribute(INFO_MESSAGES_KEY, formatMessage(message, messageArg));
  }

  public static void addInfoMessageWithHtml(RedirectAttributes redirectAttributes, String htmlMessage, String message,
      Object... messageArgs) {
    String formattedMessage = formatMessage(message, messageArgs);

    // Add the html
    formattedMessage = formattedMessage + "<p>" + htmlMessage + "</p>";

    redirectAttributes.addFlashAttribute(INFO_MESSAGES_KEY, formattedMessage);
  }

  static String formatMessage(String message, Object... args) {
    return HtmlUtils.htmlEscape(String.format(message, args));
  }
}
