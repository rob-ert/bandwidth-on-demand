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
package nl.surfnet.bod.web.appmanager;

import static nl.surfnet.bod.web.appmanager.DashboardController.INSTITUTES_PART;
import static nl.surfnet.bod.web.appmanager.DashboardController.PAGE_URL;
import static nl.surfnet.bod.web.appmanager.DashboardController.SEARCH_INDEX_PART;
import static nl.surfnet.bod.web.appmanager.DashboardController.SHIBBOLETH_INFO_PART;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.Locale;

import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.TextSearchIndexer;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.WebUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class DashboardControllerTest {

  @InjectMocks
  private DashboardController subject;

  @Mock
  private TextSearchIndexer textSearchIndexer;

  @Mock
  private InstituteService instituteService;

  @Mock
  private MessageSource messageSource;

  @Mock
  private Environment bodEnvironment;

  private final Model model = new ModelStub();

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
  }

  @Test
  public void testIndex() throws Exception {
    String url = "/" + PAGE_URL;

    mockMvc.perform(get(url)).andExpect(status().isOk()).andExpect(view().name("appmanager/index")).andExpect(
        model().attribute("refresh_searchindex_url", DashboardController.PAGE_URL + SEARCH_INDEX_PART)).andExpect(
        model().attribute("refresh_institutes_url", DashboardController.PAGE_URL + INSTITUTES_PART)).andExpect(
        model().attribute("show_shibboleth_info_url", DashboardController.PAGE_URL + SHIBBOLETH_INFO_PART));
  }

  @Test
  public void testIndexSearchDatabase() throws Exception {
    when(
        messageSource.getMessage(eq("info_dev_refresh"), aryEq(new String[] { "<b>Search database indexes</b>" }),
            any(Locale.class))).thenReturn("re index text");

    String url = "/" + PAGE_URL + SEARCH_INDEX_PART;
    mockMvc.perform(get(url)) //
        .andExpect(status().isMovedTemporarily()) //
        .andExpect(view().name("redirect:/" + DashboardController.PAGE_URL))//
        .andExpect(flash().attributeCount(1))//
        .andExpect(flash().attribute(WebUtils.INFO_MESSAGES_KEY, Lists.newArrayList("re index text")));

    verify(textSearchIndexer).indexDatabaseContent();
  }

  @Test
  public void testRefreshInstitutes() throws Exception {
    String url = "/" + PAGE_URL + INSTITUTES_PART;

    when(
        messageSource
            .getMessage(eq("info_dev_refresh"), aryEq(new String[] { "<b>Institutes</b>" }), any(Locale.class)))
        .thenReturn("institute text");

    mockMvc.perform(get(url)) //
        .andExpect(status().isMovedTemporarily()) //
        .andExpect(view().name("redirect:/" + DashboardController.PAGE_URL)) //
        .andExpect(flash().attributeCount(1)) //
        .andExpect(flash().attribute(WebUtils.INFO_MESSAGES_KEY, Lists.newArrayList("institute text")));

    verify(instituteService).refreshInstitutes();
  }

  @Test
  public void testShowShibbolethInfo() throws Exception {
    String url = "/" + PAGE_URL + SHIBBOLETH_INFO_PART;

    mockMvc.perform(get(url)).andExpect(status().isOk()).andExpect(view().name("shibbolethinfo"));
  }

}
