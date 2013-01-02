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
package nl.surfnet.bod.web;

import java.util.List;

import nl.surfnet.bod.support.ModelStub;

import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Iterables;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebUtilsTest {

  private final MessageSource messageSourceMock = mock(MessageSource.class);

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
  public void shouldAddFlashMessage() {
    RedirectAttributes redirectModel = new ModelStub();

    when(
        messageSourceMock.getMessage("info_message", new String[] { "<b>een</b>", "<b>twee</b>" },
            LocaleContextHolder.getLocale())).thenReturn("YES");

    WebUtils.addInfoFlashMessage(redirectModel, messageSourceMock, "info_message", "een", "twee");

    @SuppressWarnings("unchecked")
    List<String> infoMessages = (List<String>) redirectModel.getFlashAttributes().get(WebUtils.INFO_MESSAGES_KEY);

    assertThat(infoMessages, hasSize(1));
    assertThat(Iterables.getOnlyElement(infoMessages), is("YES"));
  }

  @Test
  public void shouldAddMessage() {
    Model model = new ModelStub();

    when(
        messageSourceMock.getMessage("info_message", new String[] { "<b>een</b>", "<b>twee</b>" },
            LocaleContextHolder.getLocale())).thenReturn("YES");

    WebUtils.addInfoMessage(model, messageSourceMock, "info_message", "een", "twee");

    @SuppressWarnings("unchecked")
    List<String> infoMessages = (List<String>) model.asMap().get(WebUtils.INFO_MESSAGES_KEY);

    assertThat(infoMessages, hasSize(1));
    assertThat(Iterables.getOnlyElement(infoMessages), is("YES"));
  }

  @Test
  public void shouldShortenAdminGroup() {
    assertThat(WebUtils.shortenAdminGroup("a:b:c:d:e"), is("e"));
    assertThat(WebUtils.shortenAdminGroup("b:c:d:e"), is("e"));
    assertThat(WebUtils.shortenAdminGroup("c:d:e"), is("e"));
    assertThat(WebUtils.shortenAdminGroup("d:e"), is("e"));
    assertThat(WebUtils.shortenAdminGroup("e"), is("e"));

    assertThat(WebUtils.shortenAdminGroup(":"), is(""));
    assertThat(WebUtils.shortenAdminGroup(null), nullValue());
    assertThat(WebUtils.shortenAdminGroup(""), is(""));
  }

  @Test
  public void shouldMapTeam() {
    assertThat(WebUtils.replaceSearchWith("bladieblateam:hierendaar", "team", "virtualResourceGroup.name"),
        is("bladieblavirtualResourceGroup.name:hierendaar"));
  }

  @Test
  public void shouldMapNull() {
    assertThat(WebUtils.replaceSearchWith("bladieblateamhierendaar", null, null), is("bladieblateamhierendaar"));
  }
}
