/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.web.user;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.util.Orderings;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.TeamView;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;

@RequestMapping("/user")
@Controller
public class DashboardController {

  @Resource
  private VirtualPortService virtualPortService;

  @Resource
  private VirtualResourceGroupService virtualResourceGroupService;

  @Resource
  private ReservationService reservationService;

  @RequestMapping(method = RequestMethod.GET)
  public String index(Model model) {
    Collection<UserGroup> userGroups = Security.getUserDetails().getUserGroups();

    if (!Security.hasUserRole()) {
      model.addAttribute("userGroups", userGroups);

      return "noUserRole";
    }

    List<TeamView> views = getTeamViews(userGroups);

    model.addAttribute("requests",
        Orderings.vpRequestLinkOrdering().sortedCopy(virtualPortService.findRequestsForLastMonth(userGroups)));
    model.addAttribute("teams", views);
    model.addAttribute("canCreateReservation", Iterables.any(views, new Predicate<TeamView>() {
      @Override
      public boolean apply(TeamView input) {
        return input.isExisting() && input.getNumberOfPorts() > 1;
      }
    }));

    model.addAttribute("defaultDuration", ReservationFilterViewFactory.DEFAULT_FILTER_INTERVAL_STRING);
    return "index";
  }

  private List<TeamView> getTeamViews(Collection<UserGroup> userGroups) {
    Collection<VirtualResourceGroup> vrgs = virtualResourceGroupService.findAllForUser(Security.getUserDetails());

    final Collection<String> existingIds = Lists.newArrayList(Collections2.transform(vrgs,
        new Function<VirtualResourceGroup, String>() {
          @Override
          public String apply(VirtualResourceGroup group) {
            return group.getAdminGroup();
          }
        }));

    Collection<TeamView> existingTeams = Collections2.transform(vrgs, new Function<VirtualResourceGroup, TeamView>() {
      @Override
      public TeamView apply(VirtualResourceGroup group) {
        long active = reservationService.countActiveReservationsForGroup(group);
        long coming = reservationService.countComingReservationsForGroup(group);
        long elapsed = reservationService.countElapsedReservationsForGroup(group);
        return new TeamView(group, active, coming, elapsed);
      }
    });

    Collection<TeamView> newTeams = FluentIterable.from(userGroups).filter(new Predicate<UserGroup>() {
      @Override
      public boolean apply(UserGroup group) {
        return !existingIds.contains(group.getId());
      }
    }).transform(new Function<UserGroup, TeamView>() {
      @Override
      public TeamView apply(UserGroup group) {
        return new TeamView(group);
      }
    }).toImmutableList();

    return Ordering.natural().sortedCopy(Iterables.concat(existingTeams, newTeams));
  }
}
