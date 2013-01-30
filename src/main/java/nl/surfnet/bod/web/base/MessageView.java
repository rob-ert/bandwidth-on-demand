/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.web.base;

import nl.surfnet.bod.util.MessageRetriever;

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

  public static MessageView createErrorMessage(MessageRetriever messageRetriever, String titleKey, String messageKey,
      String... args) {
    return create(messageRetriever, MessageType.ERROR, titleKey, messageKey, args);
  }

  public static MessageView createInfoMessage(MessageRetriever messageRetriever, String titleKey, String messageKey,
      String... args) {
    return create(messageRetriever, MessageType.INFO, titleKey, messageKey, args);
  }

  private static MessageView create(MessageRetriever messageRetriever, MessageType type, String titleKey,
      String messageKey, String... args) {
    String title = messageRetriever.getMessage(titleKey, args);
    String message = messageRetriever.getMessage(messageKey, args);

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
