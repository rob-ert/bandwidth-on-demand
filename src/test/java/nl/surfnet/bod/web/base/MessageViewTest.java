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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import nl.surfnet.bod.util.MessageRetriever;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MessageViewTest {
  private static final String TITLE_KEY = "info_key";
  private static final String MESSAGE_KEY = "message_key";

  private static final String TITLE_VALUE = "some title";
  private static final String MESSAGE_VALUE = "some subject";

  @Mock
  private MessageRetriever messageRetriever;

  @Test
  public void shouldSetInfoMessageWithArguments() {
    when(messageRetriever.getMessage(eq(TITLE_KEY), any(String[].class))).thenReturn(TITLE_VALUE);
    when(messageRetriever.getMessage(eq(MESSAGE_KEY), any(String[].class))).thenReturn(MESSAGE_VALUE);

    MessageView messageView = MessageView.createInfoMessage(messageRetriever, TITLE_KEY, MESSAGE_KEY, "argument");

    assertThat(messageView.getHeader(), is(TITLE_VALUE));
    assertThat(messageView.getMessage(), is(MESSAGE_VALUE));
  }

  @Test
  public void shouldSetInfoMessageWithoutArguments() {
    when(messageRetriever.getMessage(eq(TITLE_KEY), (String[]) anyVararg())).thenReturn(TITLE_VALUE);
    when(messageRetriever.getMessage(eq(MESSAGE_KEY), (String[]) anyVararg())).thenReturn(MESSAGE_VALUE);

    MessageView messageView = MessageView.createInfoMessage(messageRetriever, TITLE_KEY, MESSAGE_KEY);

    assertThat(messageView.getHeader(), is(TITLE_VALUE));
    assertThat(messageView.getMessage(), is(MESSAGE_VALUE));
  }

}
