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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class MessageViewTest {
  private static final String INFO_VALUE = "info";

  private static final String MESSAGE_KEY = "key";

  private static final String MESSAGE_VALUE = "some message";

  private MessageView message;

  @Mock
  private MessageSource messageSource;

  @Before
  public void setUp() {
    when(messageSource.getMessage("message_info", null, LocaleContextHolder.getLocale())).thenReturn(INFO_VALUE);
    when(messageSource.getMessage(MESSAGE_KEY, null, LocaleContextHolder.getLocale())).thenReturn(MESSAGE_VALUE);
  }

  @Test
  public void shouldSetInfoMessage() {
    message = MessageView.createInfoMessage(messageSource, "message_info", MESSAGE_KEY);

    assertThat(message.getHeader(), is(INFO_VALUE));
    assertThat(message.getMessage(), is(MESSAGE_VALUE));
  }

}
