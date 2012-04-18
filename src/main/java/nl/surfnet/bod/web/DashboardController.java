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
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

@RequestMapping("/user")
@Controller
public class DashboardController {

  @Autowired
  private VirtualResourceGroupService virtualResourceGroupService;

  @RequestMapping(method = RequestMethod.GET)
  public String index(Model model) {
    Collection<UserGroup> userGroups = Security.getUserDetails().getUserGroups();

    if (!Security.hasUserRole()) {
      model.addAttribute("userGroups", userGroups);

      return "noUserRole";
    }

    model.addAttribute("teams", getTeamViews(userGroups));

    return "index";
  }

  private List<TeamView> getTeamViews(Collection<UserGroup> userGroups) {
    Collection<VirtualResourceGroup> vrgs = virtualResourceGroupService.findAllForUser(Security.getUserDetails());

    final Collection<String> existingIds = Lists.newArrayList(
        Collections2.transform(
          vrgs,
          new Function<VirtualResourceGroup, String>() {
            @Override
            public String apply(VirtualResourceGroup group) {
              return group.getSurfconextGroupId();
            }
          }
        ));

    Collection<TeamView> existingTeams = Collections2.transform(vrgs,
        new Function<VirtualResourceGroup, TeamView>() {
          @Override
          public TeamView apply(VirtualResourceGroup group) {
            return new TeamView(group);
          }
        });


    Collection<TeamView> newTeams =
        Collections2.transform(
            Collections2.filter(userGroups,
                new Predicate<UserGroup>() {
                  @Override
                  public boolean apply(UserGroup group) {
                    return !existingIds.contains(group.getId());
                  }
                }
            ),
            new Function<UserGroup, TeamView>() {
              @Override
              public TeamView apply(UserGroup group) {
                return new TeamView(group);
              }
            }
        );

    return Ordering.natural().sortedCopy(Iterables.concat(existingTeams, newTeams));
  }

  public static class TeamView implements Comparable<TeamView> {
    private final String name;
    private final int numberOfPorts;
    private final String surfconextGroupId;
    private final int reservations;
    private final boolean existing;

    public TeamView(UserGroup group) {
      this.name = group.getName();
      this.surfconextGroupId = group.getId();
      this.numberOfPorts = 0;
      this.reservations = 0;
      this.existing = false;
    }

    public TeamView(VirtualResourceGroup group) {
      this.name = group.getName();
      this.numberOfPorts = group.getVirtualPortCount();
      this.surfconextGroupId = group.getSurfconextGroupId();
      this.reservations = 5;
      this.existing = true;
    }

    public String getName() {
      return name;
    }

    public int getNumberOfPorts() {
      return numberOfPorts;
    }

    public int getReservations() {
      return reservations;
    }

    public String getSurfconextGroupId() {
      return surfconextGroupId;
    }

    @Override
    public int compareTo(TeamView other) {
      return this.getName().compareTo(other.getName());
    }

    public boolean isExisting() {
      return existing;
    }

  }
}
