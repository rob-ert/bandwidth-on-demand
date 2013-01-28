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

import static nl.surfnet.bod.web.DevelopmentController.ERROR_PART;
import static nl.surfnet.bod.web.DevelopmentController.MESSAGES_PART;
import static nl.surfnet.bod.web.DevelopmentController.PAGE_URL;
import static nl.surfnet.bod.web.DevelopmentController.ROLES_PART;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.Locale;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

@Ignore ("Should refactor message handling first")
@RunWith(MockitoJUnitRunner.class)
public class DevelopmentControllerTest {

  @InjectMocks
  private DevelopmentController subject;

  @Mock
  private ReloadableResourceBundleMessageSource messageSourceMock;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
  }

  @Test
  public void refreshMessagesShouldClearMessageSourceCache() throws Exception {
    String url = "/" + PAGE_URL + MESSAGES_PART;

    when(
        messageSourceMock.getMessage(eq("info_dev_refresh"), aryEq(new String[] { "<b>Messages</b>" }),
            any(Locale.class))).thenReturn("refresh messages test");

    mockMvc.perform(get(url).header("Referer", "/test"))//
        .andExpect(status().isMovedTemporarily())//
        .andExpect(view().name("redirect:/test"));

    verify(messageSourceMock).clearCache();
  }

  @Test
  public void refreshRolesShouldRefresh() throws Exception {
    String url = "/" + PAGE_URL + ROLES_PART;

    when(
        messageSourceMock.getMessage(eq("info_dev_refresh"), aryEq(new String[] { "<b>Roles</b>" }), any(Locale.class)))
        .thenReturn("roles test");

    assertThat(SecurityContextHolder.getContext(), notNullValue());

    mockMvc.perform(get(url).header("Referer", "/test"))//
        .andExpect(status().isMovedTemporarily())//
        .andExpect(view().name("redirect:/t"));

    assertThat(SecurityContextHolder.getContext(), nullValue());
  }

  @Test(expected = NestedServletException.class)
  public void error() throws Exception {
    String url = "/" + PAGE_URL + ERROR_PART;
    mockMvc.perform(get(url));
  }
}
