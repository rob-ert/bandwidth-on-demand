/**
 * Copyright (c) 2012, 2013 SURFnet BV
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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import nl.surfnet.bod.web.base.MessageRetriever;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class MessageRetrieverTest {
  private static final String TEST_MESSAGE = "test message";

  @InjectMocks
  private MessageRetriever subject;

  @Mock
  private MessageSource messageSourceMock;

  @Test
  public void shouldAddFlashMessage() {
    when(
        messageSourceMock.getMessage(eq("info_message"), aryEq(new String[] { "<b>een</b>", "<b>twee</b>" }),
            eq(LocaleContextHolder.getLocale()))).thenReturn(TEST_MESSAGE);

    String message = subject.getMessageWithBoldArguments("info_message", "een", "twee");

    assertThat(message, is(TEST_MESSAGE));
  }

  @Test
  public void shouldAddMessage() {
    when(
        messageSourceMock.getMessage(eq("info_message"), aryEq(new String[] { "<b>een</b>", "<b>twee</b>" }),
            eq(LocaleContextHolder.getLocale()))).thenReturn(TEST_MESSAGE);

    String message = subject.getMessageWithBoldArguments("info_message", "een", "twee");

    assertThat(message, is(TEST_MESSAGE));
  }

  @Test
  public void shouldHtmlEscapeArgumentsAndApplyBoldStyling() {
    when(
        messageSourceMock.getMessage(eq("info_message"), aryEq(new String[] { "<b>&lt;&gt;&quot;</b>" }),
            eq(LocaleContextHolder.getLocale()))).thenReturn(TEST_MESSAGE);

    String message = subject.getMessageWithBoldArguments("info_message", "<>\"");

    assertThat(message, is(TEST_MESSAGE));
  }

  @Test
  public void shouldHtmlEscapeArguments() {
    when(
        messageSourceMock.getMessage(eq("info_message"), aryEq(new String[] { "&lt;&gt;&quot;" }),
            eq(LocaleContextHolder.getLocale()))).thenReturn(TEST_MESSAGE);

    String message = subject.getMessage("info_message", "<>\"");

    assertThat(message, is(TEST_MESSAGE));
  }
}
