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

import javax.annotation.Resource;

import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.TextSearchIndexer;
import nl.surfnet.bod.util.MessageManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller("appManagerDashboardController")
@RequestMapping("/" + DashboardController.PAGE_URL)
public class DashboardController {

  public static final String PAGE_URL = "appmanager";
  private static final String REFRESH_PART = "/refresh/";
  private static final String SHOW_PART = "/show/";

  static final String SEARCH_INDEX_PART = REFRESH_PART + "searchindex";
  static final String INSTITUTES_PART = REFRESH_PART + "institutes";
  static final String SHIBBOLETH_INFO_PART = SHOW_PART + "shibbolethattribs";

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource
  private TextSearchIndexer textSearchIndexer;

  @Resource
  private InstituteService instituteService;

  @Resource
  private MessageManager messageManager;

  @RequestMapping(method = RequestMethod.GET)
  public String index(Model model) {
    model.addAttribute("refresh_searchindex_url", PAGE_URL + SEARCH_INDEX_PART);
    model.addAttribute("refresh_institutes_url", PAGE_URL + INSTITUTES_PART);
    model.addAttribute("show_shibboleth_info_url", PAGE_URL + SHIBBOLETH_INFO_PART);

    return "appmanager/index";
  }

  @RequestMapping(value = SEARCH_INDEX_PART)
  public String indexSearchDatabase(RedirectAttributes model) {
    textSearchIndexer.indexDatabaseContent();

    logger.info("Re indexing search database");
    messageManager.addInfoFlashMessage(model, "info_dev_refresh", "Search database indexes");

    return "redirect:/" + PAGE_URL;
  }

  @RequestMapping(value = INSTITUTES_PART)
  public String refreshInstitutes(RedirectAttributes model) {
    instituteService.refreshInstitutes();

    logger.info("Manually refreshing institutes");
    messageManager.addInfoFlashMessage(model, "info_dev_refresh", "Institutes");

    return "redirect:/" + PAGE_URL;
  }

  @RequestMapping(value = SHIBBOLETH_INFO_PART)
  public String showShibbolethInfo() {
    return "shibbolethinfo";
  }

  void setMessageManager(MessageManager messageManager) {
    this.messageManager = messageManager;
  }
}