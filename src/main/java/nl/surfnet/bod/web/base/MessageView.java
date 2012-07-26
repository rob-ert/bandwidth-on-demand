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
package nl.surfnet.bod.web.base;

import nl.surfnet.bod.web.WebUtils;

import org.springframework.context.MessageSource;

public class MessageView {
  public static final String PAGE_URL = "message";
  public static final String MODEL_KEY = "message";

  private final String header;
  private final String message;
  private final MessageType type;

  private String textButton;
  private String hrefButton;

  public MessageView(MessageType type, String header, String message) {
    this.type = type;
    this.header = header;
    this.message = message;
  }

  public static MessageView createErrorMessage(MessageSource messageSource, String titleKey, String messageKey, String... args) {
    return create(MessageType.ERROR, messageSource, titleKey, messageKey, args);
  }

  public static MessageView createInfoMessage(MessageSource messageSource, String titleKey, String messageKey, String... args) {
    return create(MessageType.INFO, messageSource, titleKey, messageKey, args);
  }

  private static MessageView create(
      MessageType type, MessageSource messageSource, String titleKey, String messageKey, String... args) {
    String title = WebUtils.getMessage(messageSource, titleKey, args);
    String message = WebUtils.getMessage(messageSource, messageKey, args);

    MessageView messageView = new MessageView(type, title, message);

    return messageView;
  }

  public void addButton(String text, String url) {
    this.textButton = text;
    this.hrefButton = url;
  }

  public MessageType getType() {
    return type;
  }

  public String getHeader() {
    return header;
  }

  public String getMessage() {
    return message;
  }

  public String getTextButton() {
    return textButton;
  }

  public String getHrefButton() {
    return hrefButton;
  }

  enum MessageType {
    SUCCESS, INFO, WARNING, ERROR
  }
}
