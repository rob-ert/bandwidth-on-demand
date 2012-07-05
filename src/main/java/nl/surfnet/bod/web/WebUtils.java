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

import java.util.Arrays;
import java.util.List;

import nl.surfnet.bod.web.security.Security;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
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
  public static final String ERROR_MESSAGES_KEY = "errorMessages";

  public static final String PARAM_MARKUP_START = "<b>";
  public static final String PARAM_MARKUP_END = "</b>";

  public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd H:mm:ss";
  public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormat
      .forPattern(DEFAULT_DATE_TIME_PATTERN);

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
    addMessage(model, getMessageWithBoldArguments(messageSource, label, messageArgs));
  }

  public static void addInfoMessage(String extraHtml, RedirectAttributes model, MessageSource messageSource,
      String label, String... messageArgs) {
    addMessage(model, getMessageWithBoldArguments(messageSource, label, messageArgs) + " " + extraHtml);
  }

  public static void addInfoMessage(Model model, MessageSource messageSource, String label, String... messageArgs) {
    addInfoMessage(model, getMessageWithBoldArguments(messageSource, label, messageArgs));
  }

  public static void addErrorMessage(String extraHtml, Model model, MessageSource messageSource, String label,
      String... messageArgs) {
    addErrorMessage(model, getMessageWithBoldArguments(messageSource, label, messageArgs) + " " + extraHtml);
  }

  public static String getMessageWithBoldArguments(MessageSource messageSource, String label, String... messageArgs) {
    return getMessage(messageSource, label, makeArgsDisplayBold(messageArgs));
  }

  public static String getMessage(MessageSource messageSource, String key, String... args) {
    return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
  }

  private static String[] makeArgsDisplayBold(String[] objects) {
    return FluentIterable.from(Arrays.asList(objects)).transform(new Function<String, String>() {
      @Override
      public String apply(String input) {
        return String.format("<b>%s</b>", input);
      }
    }).toArray(String.class);
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

  private static void addMessage(RedirectAttributes model, String message) {
    @SuppressWarnings("unchecked")
    List<String> messages = (List<String>) model.getFlashAttributes().get(INFO_MESSAGES_KEY);

    if (messages == null) {
      model.addFlashAttribute(INFO_MESSAGES_KEY, Lists.newArrayList(message));
    }
    else {
      messages.add(message);
    }
  }

  private static void addInfoMessage(Model model, String message) {
    @SuppressWarnings("unchecked")
    List<String> messages = (List<String>) model.asMap().get(INFO_MESSAGES_KEY);

    if (messages == null) {
      model.addAttribute(INFO_MESSAGES_KEY, Lists.newArrayList(message));
    }
    else {
      messages.add(message);
    }
  }

  private static void addErrorMessage(Model model, String message) {
    @SuppressWarnings("unchecked")
    List<String> messages = (List<String>) model.asMap().get(ERROR_MESSAGES_KEY);

    if (messages == null) {
      model.addAttribute(ERROR_MESSAGES_KEY, Lists.newArrayList(message));
    }
    else {
      messages.add(message);
    }
  }

  public static boolean not(boolean expression) {
    return !expression;
  }
}
