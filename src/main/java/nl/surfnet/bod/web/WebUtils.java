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

import java.util.List;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.HtmlUtils;

import com.google.common.collect.Lists;

public final class WebUtils {

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

  public static final int MAX_ITEMS_PER_PAGE = 10;

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

  /**
   * Adds an infoMessage, depending on the type of {@link Model} it will will
   * survive a redirect.
   * 
   * @param model
   *          Model to add the message to
   * @param message
   *          Message to add
   * @param messageArg
   *          Arguments to parse into the message, using
   *          {@link String#format(String, Object...)}
   */
  public static void addInfoMessage(Model model, String message, Object... messageArgs) {
    addMessage(model, formatAndEscapeMessage(message, messageArgs));
  }

  public static void addInfoMessage(RedirectAttributes model, String message, Object... messageArgs) {
    addMessage(model, formatAndEscapeMessage(message, messageArgs));
  }

  public static void addInfoMessageWithHtml(Model model, String htmlMessage, String message, Object... messageArgs) {
    addMessage(model, formatAndEscapeMessage(htmlMessage, message, messageArgs));
  }

  public static void addInfoMessageWithHtml(RedirectAttributes model, String htmlMessage, String message,
      Object... messageArgs) {
    String formattedMessage = formatAndEscapeMessage(htmlMessage, message, messageArgs);
    addMessage(model, formattedMessage);
  }

  public static String getFirstInfoMessage(Model model) {
    String message = null;
    @SuppressWarnings("unchecked")
    List<String> messages = (List<String>) model.asMap().get(INFO_MESSAGES_KEY);
    if (messages != null) {
      message = messages.get(0);
    }
    return message;
  }

  public static String getFirstInfoMessage(RedirectAttributes redirectAttributes) {
    String message = null;
    @SuppressWarnings("unchecked")
    List<String> messages = (List<String>) redirectAttributes.getFlashAttributes().get(INFO_MESSAGES_KEY);
    if (messages != null) {
      message = messages.get(0);
    }
    return message;
  }

  static String formatAndEscapeMessage(String message, Object... args) {
    if (message != null) {
      return HtmlUtils.htmlEscape(String.format(message, args));
    }

    return "";
  }

  private static String formatAndEscapeMessage(String htmlMessage, String message, Object... messageArgs) {
    String formattedMessage = formatAndEscapeMessage(message, messageArgs);

    // Add the html
    formattedMessage = formattedMessage + " " + htmlMessage;
    return formattedMessage;
  }

  static void addMessage(RedirectAttributes model, String message) {
    @SuppressWarnings("unchecked")
    List<String> messages = (List<String>) model.getFlashAttributes().get(INFO_MESSAGES_KEY);

    if (messages == null) {
      model.addFlashAttribute(INFO_MESSAGES_KEY, Lists.newArrayList(message));
    }
    else {
      messages.add(message);
    }
  }

  static void addMessage(Model model, String message) {
    @SuppressWarnings("unchecked")
    List<String> messages = (List<String>) model.asMap().get(INFO_MESSAGES_KEY);
    if (messages == null) {
      model.addAttribute(INFO_MESSAGES_KEY, Lists.newArrayList(message));
    }
    else {
      messages.add(message);
    }
  }
}
