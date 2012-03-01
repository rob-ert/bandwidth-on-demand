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
