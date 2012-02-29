package nl.surfnet.bod.web;

import org.springframework.context.MessageSource;

public class MessageCommand {
  public static final String PAGE_URL = "message";
  public static final String MODEL_KEY = "message";

  private String header;
  private String paragraph;
  private String url;
  private String urlText;
  private MessageSource messageSource;

  public MessageCommand(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public MessageCommand setInfoMessage(String messageKey) {
    this.header = WebUtils.getMessage(messageSource, "message_info");
    this.paragraph = WebUtils.getMessage(messageSource, messageKey);

    return this;
  }

  public MessageCommand setWarnMessage(String messageKey) {
    this.header = WebUtils.getMessage(messageSource, "message_warn");
    this.paragraph = WebUtils.getMessage(messageSource, messageKey);

    return this;
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
