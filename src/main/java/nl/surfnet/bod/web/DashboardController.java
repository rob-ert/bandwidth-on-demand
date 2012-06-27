/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.web;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.util.Orderings;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.TeamView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

@RequestMapping("/user")
@Controller
public class DashboardController {

  @Autowired
  private VirtualPortService virtualPortService;

  @Autowired
  private VirtualResourceGroupService virtualResourceGroupService;

  @Autowired
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
            return group.getSurfconextGroupId();
          }
        }));

    Collection<TeamView> existingTeams = Collections2.transform(vrgs, new Function<VirtualResourceGroup, TeamView>() {
      @Override
      public TeamView apply(VirtualResourceGroup group) {
        long total = reservationService.countForVirtualResourceGroup(group);
        long scheduled = reservationService.countScheduledReservationForVirtualResourceGroup(group);
        long active = reservationService.countActiveReservationForVirtualResourceGroup(group);
        return new TeamView(group, active, scheduled, total);
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
