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

import static nl.surfnet.bod.web.WebUtils.FILTER_LIST;
import static nl.surfnet.bod.web.WebUtils.FILTER_SELECT;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;

import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.util.Functions;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.noc.PhysicalPortController.PhysicalPortFilter;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.view.PhysicalPortView;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/noc/physicalports/unaligned")
public class UnalignedPortController extends AbstractSearchableSortableListController<PhysicalPortView, PhysicalPort> {

  @Resource private PhysicalPortService physicalPortService;
  @Resource private VirtualPortService virtualPortService;
  @Resource private ReservationService reservationService;
  @Resource private NsiHelper nsiHelper;

  @RequestMapping(method = RequestMethod.GET)
  @Override
  public String list(@RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order, Model model) {

    addUnalignedPortFilter(model);
    return super.list(page, sort, order, model);
  }

  @Override
  @RequestMapping(value = "search", method = RequestMethod.GET)
  public String search(Integer page, String sort, String order, String search, Model model) {
    addUnalignedPortFilter(model);
    return super.search(page, sort, order, search, model);
  }

  private void addUnalignedPortFilter(Model model) {
    model.addAttribute(FILTER_SELECT, PhysicalPortFilter.UN_ALIGNED);
    model.addAttribute(FILTER_LIST, PhysicalPortFilter.getAvailableFilters());
  }

  @Override
  protected List<? extends PhysicalPortView> transformToView(List<? extends PhysicalPort> entities, RichUserDetails user) {
    return Functions.transformUnalignedPhysicalPorts(entities, virtualPortService, reservationService, nsiHelper);
  }

  @Override
  protected String listUrl() {
    return "noc/physicalports/unaligned/list";
  }

  @Override
  protected List<PhysicalPort> list(int firstPage, int maxItems, Sort sort, Model model) {
    return physicalPortService.findUnalignedPhysicalPorts(firstPage, maxItems, sort);
  }

  @Override
  protected long count(Model model) {
    return physicalPortService.countUnalignedPhysicalPorts();
  }

  @Override
  protected List<Long> getIdsOfAllAllowedEntries(Model model, Sort sort) {
    return physicalPortService.findUnalignedIds(Optional.ofNullable(sort));
  }

  @Override
  protected AbstractFullTextSearchService<PhysicalPort> getFullTextSearchableService() {
    return physicalPortService;
  }

  @Override
  protected String getDefaultSortProperty() {
    return "nocLabel";
  }

}
