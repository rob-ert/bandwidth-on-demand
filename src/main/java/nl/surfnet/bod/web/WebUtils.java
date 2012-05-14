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

import nl.surfnet.bod.web.security.Security;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.HtmlUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public final class WebUtils {

  public static final String CREATE = "/create";
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
  public static final String FILTER_LIST = "filterList";
  public static final String FILTER_SELECT = "filterSelect";
  public static final String DATA_LIST = "list";

  public static final int MAX_ITEMS_PER_PAGE = 15;

  public static final String INFO_MESSAGES_KEY = "infoMessages";

  public static final String PARAM_MARKUP_START = "<b>";
  public static final String PARAM_MARKUP_END = "</b>";

  private WebUtils() {
  }

  public static int calculateFirstPage(Integer page) {
    return page == null ? 0 : (page.intValue() - 1) * MAX_ITEMS_PER_PAGE;
  }

  public static int calculateMaxPages(long totalEntries) {
    float nrOfPages = (float) totalEntries / MAX_ITEMS_PER_PAGE;
    return (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
  }

  public static <T> T getAttributeFromModel(String attributeName, Model model) {
    return getAttributeFromModel(attributeName, model, null);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getAttributeFromModel(String attributeName, Model model, T defaultValue) {
    return (T) (model.asMap().get(attributeName) == null ? defaultValue : model.asMap().get(attributeName));
  }

  /**
   *
   * @return The user selected PhysicalResourceGroupId
   */
  public static Long getSelectedPhysicalResourceGroupId() {
    return Security.getUserDetails().getSelectedRole() == null ? null : Security.getUserDetails().getSelectedRole()
        .getPhysicalResourceGroupId();
  }

  public static void addInfoMessage(RedirectAttributes model, MessageSource messageSource, String label,
      String... messageArgs) {
    addInfoMessage(model, getMessage(messageSource, label, messageArgs));
  }

  public static void addInfoMessage(RedirectAttributes model, String message, String... messageArgs) {
    addMessage(model, formatAndEscapeMessage(message, messageArgs));
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

  public static String getFirstInfoMessage(RedirectAttributes model) {
    String message = null;
    @SuppressWarnings("unchecked")
    List<String> messages = (List<String>) model.getFlashAttributes().get(INFO_MESSAGES_KEY);
    if (messages != null) {
      message = messages.get(0);
    }
    return message;
  }

  /**
   * Html escapes the argument and replaces them with the parameter placeholders
   * in the message. The parameter placeholders can be either "{}" or the
   * regular {@link String#format(String, Object...)} placeholders.
   *
   * @param message
   *          The message to parse
   * @param args
   *          Values the replace the placeholders with
   * @return Formatted string, with the arguments html escaped.
   */
  public static String formatAndEscapeMessage(String message, String... args) {
    Preconditions.checkNotNull(message);

    // Enable replacement by log and spring convention
    String replacingMessage = StringUtils.replace(message, "{}", "%s");
    replacingMessage = StringUtils.replace(replacingMessage, "%s", PARAM_MARKUP_START + "%s" + PARAM_MARKUP_END);

    List<String> escapedArgs = Lists.newArrayList();
    for (String arg : args) {
      escapedArgs.add(HtmlUtils.htmlEscape(arg));
    }

    return String.format(replacingMessage, escapedArgs.toArray());
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

  public static String getMessage(MessageSource messageSource, String key, String... args) {
    return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
  }

  public static boolean not(boolean expression) {
    return !expression;
  }
}
