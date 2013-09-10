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
package nl.surfnet.bod.web.noc;

import static nl.surfnet.bod.web.WebUtils.DELETE;
import static nl.surfnet.bod.web.WebUtils.FILTER_SELECT;
import static nl.surfnet.bod.web.WebUtils.ID_KEY;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;

import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.base.MessageManager;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.VirtualPortView;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@Controller("nocVirtualPortController")
@RequestMapping("/noc/virtualports")
public class VirtualPortController extends AbstractSearchableSortableListController<VirtualPortView, VirtualPort> {

  public static final String PAGE_URL = "/noc/virtualports";

  @Resource private VirtualPortService virtualPortService;
  @Resource private ReservationService reservationService;
  @Resource private MessageManager messageManager;
  @Resource private NsiHelper nsiHelper;

  @Override
  protected AbstractFullTextSearchService<VirtualPort> getFullTextSearchableService() {
    return virtualPortService;
  }

  @Override
  protected String listUrl() {
    return "/noc/virtualports/list";
  }

  @Override
  protected List<VirtualPort> list(int firstPage, int maxItems, Sort sort, Model model) {
    return virtualPortService.findEntries(firstPage, maxItems, sort);
  }

  @Override
  protected long count(Model model) {
    return virtualPortService.count();
  }

  @Override
  protected String getDefaultSortProperty() {
    return "managerLabel";
  }

  @Override
  protected List<String> translateSortProperty(String sortProperty) {
    if (sortProperty.equals("physicalResourceGroup")) {
      return ImmutableList.of("physicalPort.physicalResourceGroup");
    }

    return super.translateSortProperty(sortProperty);
  }

  @Override
  protected List<Long> getIdsOfAllAllowedEntries(Model model, Sort sort) {
    VirtualPortView filter = WebUtils.getAttributeFromModel(FILTER_SELECT, model);
    return virtualPortService.findIdsForUserUsingFilter(Security.getUserDetails(), filter, sort);
  }

  @Override
  protected List<? extends VirtualPortView> transformToView(List<? extends VirtualPort> entities, RichUserDetails user) {
    return Lists.transform(entities, fromNocVirtualportToVirtualportView);
  }

  @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
  public String delete(@RequestParam(ID_KEY) Long id, @RequestParam(value = PAGE_KEY, required = false) Integer page,
      RedirectAttributes redirectAttributes) {

    VirtualPort virtualPort = virtualPortService.find(id);

    if (virtualPort == null || !Security.getSelectedRole().isNocRole()) {
      return "redirect:" + PAGE_URL;
    }

    virtualPortService.delete(virtualPort, Security.getUserDetails());

    messageManager.addInfoFlashMessage(redirectAttributes, "info_virtualport_deleted", virtualPort.getManagerLabel());

    return "redirect:" + PAGE_URL;
  }

  public final Function<VirtualPort, VirtualPortView> fromNocVirtualportToVirtualportView = new Function<VirtualPort, VirtualPortView>() {
    @Override
    public VirtualPortView apply(VirtualPort port) {
      long counter = reservationService.findAllActiveByVirtualPort(port).size();
      return new VirtualPortView(port, nsiHelper, Optional.of(counter));
    }
  };

  @VisibleForTesting
  void setMessageManager(MessageManager messageManager) {
    this.messageManager = messageManager;
  }

}
