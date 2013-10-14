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
package nl.surfnet.bod.web.manager;

import static nl.surfnet.bod.util.Orderings.VP_REQUEST_LINK_ORDERING;

import java.util.Collection;

import javax.annotation.Resource;

import com.google.common.base.Optional;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ManagerStatisticsView;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller("managerDashboardController")
@RequestMapping("/manager")
public class DashboardController {

  @Resource
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Resource
  private VirtualPortService virtualPortService;

  @Resource
  private PhysicalPortService physicalPortService;

  @Resource
  private ReservationService reservationService;

  @RequestMapping(method = RequestMethod.GET)
  public String index(Model model) {
    Optional<Long> groupId = WebUtils.getSelectedPhysicalResourceGroupId();

    if (!groupId.isPresent()) {
      return "redirect:/";
    }

    PhysicalResourceGroup group = physicalResourceGroupService.find(groupId.get());
    Collection<VirtualPortRequestLink> requests = virtualPortService.findPendingRequests(group);

    model.addAttribute("prg", group);
    model.addAttribute("requests", VP_REQUEST_LINK_ORDERING.sortedCopy(requests));

    model.addAttribute("stats", determineStatistics(Security.getUserDetails()));
    model.addAttribute("defaultDuration", ReservationFilterViewFactory.DEFAULT_FILTER_INTERVAL_STRING);

    return "manager/index";
  }

  ManagerStatisticsView determineStatistics(RichUserDetails manager) {
    ReservationFilterViewFactory reservationFilterViewFactory = new ReservationFilterViewFactory();
    PhysicalResourceGroup managerPrg = physicalResourceGroupService.find(manager.getSelectedRole()
        .getPhysicalResourceGroupId().get());

    long countPhysicalPorts = physicalPortService.countAllocatedForPhysicalResourceGroup(managerPrg);

    long countVirtualPorts = virtualPortService.countForManager(manager.getSelectedRole());

    long countElapsedReservations = reservationService.countForFilterAndManager(manager, reservationFilterViewFactory
        .create(ReservationFilterViewFactory.ELAPSED));

    long countActiveReservations = reservationService.countForFilterAndManager(manager, reservationFilterViewFactory
        .create(ReservationFilterViewFactory.ACTIVE));

    long countComingReservations = reservationService.countForFilterAndManager(manager, reservationFilterViewFactory
        .create(ReservationFilterViewFactory.COMING));

    return new ManagerStatisticsView(countPhysicalPorts, countVirtualPorts, countElapsedReservations,
        countActiveReservations, countComingReservations);
  }

}