package nl.surfnet.bod.util;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

@Component
public class MessageManager {

  public static final String INFO_MESSAGES_KEY = "infoMessages";
  public static final String ERROR_MESSAGES_KEY = "errorMessages";
  public static final String WARN_MESSAGES_KEY = "warnMessages";

  @Resource
  private MessageRetriever messageRetriever;

  public MessageManager() {

  }

  @VisibleForTesting
  public MessageManager(MessageRetriever messageRetriever) {
    this.messageRetriever = messageRetriever;
  }

  public void addInfoFlashMessage(RedirectAttributes model, String key, String... args) {
    addFlashMessage(model, messageRetriever.getMessageWithBoldArguments(key, args), INFO_MESSAGES_KEY);
  }

  public void addInfoFlashMessage(String extraHtml, RedirectAttributes model, String key, String... args) {
    addFlashMessage(model, messageRetriever.getMessageWithBoldArguments(key, args) + " " + extraHtml, INFO_MESSAGES_KEY);
  }

  public void addInfoMessage(Model model, String key, String... args) {
    addMessage(model, messageRetriever.getMessageWithBoldArguments(key, args), INFO_MESSAGES_KEY);
  }

  public void addErrorFlashMessage(RedirectAttributes model, String key, String... args) {
    addFlashMessage(model, messageRetriever.getMessageWithBoldArguments(key, args), ERROR_MESSAGES_KEY);
  }

  public void addErrorMessage(String extraHtml, Model model, String key, String... args) {
    addMessage(model, messageRetriever.getMessageWithBoldArguments(key, args) + " " + extraHtml, ERROR_MESSAGES_KEY);
  }

  public String getFirstInfoMessage(Model model) {
    String message = null;
    @SuppressWarnings("unchecked")
    List<String> messages = (List<String>) model.asMap().get(INFO_MESSAGES_KEY);
    if (messages != null) {
      message = messages.get(0);
    }
    return message;
  }

  public String getFirstInfoMessage(RedirectAttributes model) {
    String message = null;
    @SuppressWarnings("unchecked")
    List<String> messages = (List<String>) model.getFlashAttributes().get(INFO_MESSAGES_KEY);
    if (messages != null) {
      message = messages.get(0);
    }
    return message;
  }

  private void addMessage(Model model, String message, String messageType) {
    @SuppressWarnings("unchecked")
    List<String> messages = (List<String>) model.asMap().get(messageType);

    if (messages == null) {
      model.addAttribute(messageType, Lists.newArrayList(message));
    }
    else {
      messages.add(message);
    }
  }

  private void addFlashMessage(RedirectAttributes model, String message, String messageType) {
    @SuppressWarnings("unchecked")
    List<String> messages = (List<String>) model.getFlashAttributes().get(messageType);

    if (messages == null) {
      model.addFlashAttribute(messageType, Lists.newArrayList(message));
    }
    else {
      messages.add(message);
    }
  }

}
