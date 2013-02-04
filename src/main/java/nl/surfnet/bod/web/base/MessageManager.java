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
