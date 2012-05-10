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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import nl.surfnet.bod.support.ModelStub;

import org.junit.Test;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class WebUtilsTest {

  private Model model = new ModelStub();
  private RedirectAttributes redirectModel = new ModelStub();

  private final String messageBase = "Test message ";
  private final String message = messageBase + "{}";
  private final String messageArg = "unittest";
  private final String messageArgWithMarkup = WebUtils.PARAM_MARKUP_START + messageArg + WebUtils.PARAM_MARKUP_END;
  private final String[] messageArgs = { messageArg };
  private final String[] emptyArgs = new String[0];

  @Test
  public void testSecondPage() {
    assertThat(WebUtils.calculateFirstPage(2), is(WebUtils.MAX_ITEMS_PER_PAGE));
  }

  @Test
  public void shouldContainOnePage() {
    assertThat(WebUtils.calculateMaxPages(0), is(1));
    assertThat(WebUtils.calculateMaxPages(WebUtils.MAX_ITEMS_PER_PAGE), is(1));
    assertThat(WebUtils.calculateMaxPages(WebUtils.MAX_ITEMS_PER_PAGE - 1), is(1));
  }

  @Test
  public void shouldContainTwoPages() {
    assertThat(WebUtils.calculateMaxPages(WebUtils.MAX_ITEMS_PER_PAGE + 1), is(2));
    assertThat(WebUtils.calculateMaxPages(WebUtils.MAX_ITEMS_PER_PAGE + WebUtils.MAX_ITEMS_PER_PAGE), is(2));
  }

  @Test
  public void shouldHaveReplacedPlaceholderWithNormalRedirectModel() {
    WebUtils.addInfoMessage(redirectModel, message, messageArgs);

    assertThat(WebUtils.getFirstInfoMessage(redirectModel), is(messageBase + messageArgWithMarkup));
  }

  @Test
  public void shouldHaveAddedTwoMessagesWithRedirectModel() {
    WebUtils.addInfoMessage(redirectModel, message, messageArgs);
    WebUtils.addInfoMessage(redirectModel, "SecondMessage", emptyArgs);

    @SuppressWarnings("unchecked")
    List<String> messages = (List<String>) redirectModel.getFlashAttributes().get(WebUtils.INFO_MESSAGES_KEY);
    assertThat(messages.get(0), is(messageBase + messageArgWithMarkup));
    assertThat(messages.get(1), is("SecondMessage"));
  }


  @Test
  public void shouldFormatMessage() {
    String formatAndEscapeMessage = WebUtils.formatAndEscapeMessage(message, messageArgs);

    assertThat(formatAndEscapeMessage, is(messageBase + messageArgWithMarkup));
  }

  @Test
  public void shouldNotEscapeMessage() {
    String formatAndEscapeMessage = WebUtils.formatAndEscapeMessage("<p>", emptyArgs);

    assertThat(formatAndEscapeMessage, is("<p>"));
  }

  @Test
  public void shouldFormatAndNotEscapeMessageButArgs() {
    String formatAndEscapeMessage = WebUtils.formatAndEscapeMessage("<p>%s</p>", "<b>");

    assertThat(formatAndEscapeMessage, is("<p>" + WebUtils.PARAM_MARKUP_START + "&lt;b&gt;" + WebUtils.PARAM_MARKUP_END
        + "</p>"));
  }

  @Test
  public void shouldAddMessagToModel() {
    WebUtils.addMessage(model, message);

    assertThat(WebUtils.getFirstInfoMessage(model), is(message));
    assertThat(WebUtils.getFirstInfoMessage(redirectModel), nullValue());
  }

  @Test
  public void shouldAddMessagToRedirectModel() {
    WebUtils.addMessage(redirectModel, message);

    assertThat(WebUtils.getFirstInfoMessage(redirectModel), is(message));
    assertThat(WebUtils.getFirstInfoMessage(model), nullValue());
  }

}
