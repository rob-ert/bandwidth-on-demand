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

import org.springframework.context.MessageSource;

public class MessageView {
  public static final String PAGE_URL = "message";
  public static final String MODEL_KEY = "message";

  private String header;
  private String paragraph;
  private String url;
  private String urlText;

  public static MessageView createInfoMessage(MessageSource messageSource, String messageKey) {
    MessageView messageView = new MessageView();

    messageView.header = WebUtils.getMessage(messageSource, "message_info");
    messageView.paragraph = WebUtils.getMessage(messageSource, messageKey);

    return messageView;
  }

  public static MessageView createWarningMessage(MessageSource messageSource, String messageKey) {
    MessageView messageView = new MessageView();

    messageView.header = WebUtils.getMessage(messageSource, "message_warn");
    messageView.paragraph = WebUtils.getMessage(messageSource, messageKey);

    return messageView;
  }

  public String getHeader() {
    return header;
  }

  public String getParagraph() {
    return paragraph;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUrlText() {
    return urlText;
  }

  public void setUrlText(String urlText) {
    this.urlText = urlText;
  }

}
