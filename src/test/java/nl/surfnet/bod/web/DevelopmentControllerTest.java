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
package nl.surfnet.bod.web;

import static nl.surfnet.bod.web.DevelopmentController.MESSAGES_PART;
import static nl.surfnet.bod.web.DevelopmentController.PAGE_URL;
import static nl.surfnet.bod.web.DevelopmentController.ROLES_PART;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.base.MessageManager;
import nl.surfnet.bod.web.base.MessageRetriever;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(MockitoJUnitRunner.class)
public class DevelopmentControllerTest {

  @InjectMocks
  private DevelopmentController subject;

  @Mock
  private MessageRetriever messageRetrieverMock;
  @Mock
  private ReloadableResourceBundleMessageSource messageSourceMock;
  @Mock
  private Environment environmentMock;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
    subject.setMessageManager(new MessageManager(messageRetrieverMock));
  }

  @Test
  public void refreshMessagesShouldClearMessageSourceCache() throws Exception {
    when(environmentMock.isDevelopment()).thenReturn(true);
    when(messageRetrieverMock.getMessageWithBoldArguments("info_dev_refresh", "Messages")).thenReturn("correctMessage");

    mockMvc.perform(get("/" + PAGE_URL + MESSAGES_PART).header("Referer", "/referer_test"))
      .andExpect(status().isMovedTemporarily())
      .andExpect(flash().attribute("infoMessages", hasItem("correctMessage")))
      .andExpect(view().name("redirect:/referer_test"));

    verify(messageSourceMock).clearCache();
  }

  @Test
  public void refreshMessagesShouldNotRefreshWhenNotInDevMode() throws Exception {
    when(environmentMock.isDevelopment()).thenReturn(false);

    mockMvc.perform(get("/" + PAGE_URL + MESSAGES_PART).header("Referer", "/referer_test"))
      .andExpect(status().isMovedTemporarily())
      .andExpect(flash().attribute("infoMessages", nullValue()))
      .andExpect(view().name("redirect:/referer_test"));

    verify(messageSourceMock, never()).clearCache();
  }

  @Test
  public void refreshRolesShouldRefresh() throws Exception {
    Security.setUserDetails(new RichUserDetailsFactory().create());

    when(environmentMock.isDevelopment()).thenReturn(true);
    when(messageRetrieverMock.getMessageWithBoldArguments("info_dev_refresh", "Roles/User")).thenReturn("correctMessage");

    assertThat(Security.getUserDetails(), notNullValue());

    mockMvc.perform(get("/" + PAGE_URL + ROLES_PART).header("Referer", "/test"))
      .andExpect(status().isMovedTemporarily())
      .andExpect(flash().attribute("infoMessages", hasItem("correctMessage")))
      .andExpect(view().name("redirect:/test"));

    assertThat(Security.getUserDetails(), nullValue());
  }

  @Test
  public void refreshRolesShouldPreserveNameIdRequestParameter() throws Exception {
    Security.setUserDetails(new RichUserDetailsFactory().create());

    when(environmentMock.isDevelopment()).thenReturn(true);

    assertThat(Security.getUserDetails(), notNullValue());

    mockMvc.perform(get("/" + PAGE_URL + ROLES_PART + "?nameId=urn:hanst").header("Referer", "/test"))
      .andExpect(status().isMovedTemporarily())
      .andExpect(view().name("redirect:/test?nameId=urn:hanst"));

    assertThat(Security.getUserDetails(), nullValue());
  }

  @Test
  public void refreshRolesShouldNotRefreshWhenNotInDevMode() throws Exception {
    RichUserDetails user = new RichUserDetailsFactory().create();
    Security.setUserDetails(user);

    when(environmentMock.isDevelopment()).thenReturn(false);

    mockMvc.perform(get("/" + PAGE_URL + ROLES_PART).header("Referer", "/test"))
      .andExpect(status().isMovedTemporarily())
      .andExpect(flash().attribute("infoMessages", nullValue()))
      .andExpect(view().name("redirect:/test"));

    assertThat(Security.getUserDetails(), is(user));
  }
}