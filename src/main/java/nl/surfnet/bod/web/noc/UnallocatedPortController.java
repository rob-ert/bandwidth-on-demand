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

import static com.google.common.base.Strings.isNullOrEmpty;
import static nl.surfnet.bod.web.WebUtils.DATA_LIST;
import static nl.surfnet.bod.web.WebUtils.FILTER_LIST;
import static nl.surfnet.bod.web.WebUtils.FILTER_SELECT;
import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
import static nl.surfnet.bod.web.WebUtils.MAX_PAGES_KEY;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;
import static nl.surfnet.bod.web.WebUtils.calculateMaxPages;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;

import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.util.Functions;
import nl.surfnet.bod.web.noc.PhysicalPortController.PhysicalPortFilter;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.PhysicalPortView;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/noc/physicalports/free")
public class UnallocatedPortController {

  @Resource private PhysicalPortService physicalPortService;

  @RequestMapping(method = RequestMethod.GET)
  public String list(
    @RequestParam(value = PAGE_KEY, required = false) Integer page,
    @RequestParam(value = "sort", required = false) String sortProperty,
    @RequestParam(value = "order", required = false) String sortDirection,
    Model model) {

    Order order = prepareSortOptions(sortProperty, sortDirection, model);

    model.addAttribute(FILTER_SELECT, PhysicalPortFilter.UN_ALLOCATED);
    model.addAttribute(FILTER_LIST, PhysicalPortFilter.getAvailableFilters());
    model.addAttribute(MAX_PAGES_KEY, calculateMaxPages(count(model)));
    model.addAttribute(DATA_LIST, transformToView(list(calculateFirstPage(page), MAX_ITEMS_PER_PAGE, order), Security.getUserDetails()));

    return "noc/physicalports/unallocated/list";
  }

  private Order prepareSortOptions(String sort, String order, Model model) {
    String sortProperty = sortProperty(sort);
    Direction sortDirection = sortDirection(order);

    model.addAttribute("sortProperty", sortProperty);
    model.addAttribute("sortDirection", sortDirection);

    return new Order(sortDirection, sortProperty);
  }

  private Direction sortDirection(String order) {
    if (Strings.isNullOrEmpty(order)) {
      return getDefaultSortOrder();
    }

    try {
      return Direction.fromString(order);
    } catch (IllegalArgumentException e) {
      return getDefaultSortOrder();
    }
  }

  private Direction getDefaultSortOrder() {
    return Direction.ASC;
  }

  private String sortProperty(String property) {
    return isNullOrEmpty(property) ? getDefaultSortProperty() : property;
  }

  private List<PhysicalPortView> transformToView(Collection<NbiPort> unallocatedPorts, RichUserDetails user) {
    return Functions.transformUnallocatedPhysicalPorts(unallocatedPorts);
  }

  private List<NbiPort> list(int firstResult, int maxItems, Order order) {
    Collection<NbiPort> ports = physicalPortService.findUnallocated();

    Ordering<NbiPort> ordering;
    switch (order.getProperty()) {
    case "nocLabel":
      ordering = NOC_LABEL_ORDERING;
      break;
    case "bodPortId":
      ordering = BOD_PORT_ID_ORDERING;
      break;
    case "nmsPortId":
      ordering = NMS_PORT_ID_ORDERING;
      break;
    case "interfaceType":
      ordering = INTERFACE_TYPE_ORDERING;
      break;
    default:
      ordering = NOC_LABEL_ORDERING;
      break;
    }

    if (order.getDirection() == Direction.DESC) {
      ordering = ordering.reverse();
    }

    return FluentIterable.from(ordering.sortedCopy(ports)).skip(firstResult).limit(maxItems).toList();
  }

  protected long count(Model model) {
    return physicalPortService.countUnallocated();
  }

  private String getDefaultSortProperty() {
    return "nocLabel";
  }

  private static final Ordering<NbiPort> BOD_PORT_ID_ORDERING = new Ordering<NbiPort>() {
    @Override
    public int compare(NbiPort left, NbiPort right) {
      return left.getSuggestedBodPortId().compareTo(right.getSuggestedBodPortId());
    }
  };
  private static final Ordering<NbiPort> NMS_PORT_ID_ORDERING = new Ordering<NbiPort>() {
    @Override
    public int compare(NbiPort left, NbiPort right) {
      return left.getNmsPortId().compareTo(right.getNmsPortId());
    }
  };
  private static final Ordering<NbiPort> NOC_LABEL_ORDERING = new Ordering<NbiPort>() {
    @Override
    public int compare(NbiPort left, NbiPort right) {
      return left.getSuggestedNocLabel().compareTo(right.getSuggestedNocLabel());
    }
  };
  private static final Ordering<NbiPort> INTERFACE_TYPE_ORDERING = new Ordering<NbiPort>() {
    @Override
    public int compare(NbiPort left, NbiPort right) {
      return left.getInterfaceType().compareTo(right.getInterfaceType());
    }
  };
}
