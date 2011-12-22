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
package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class VirtualResourceGroupServiceTest {

  private VirtualResourceGroupService virtualResourceGroupService;

  private VirtualResourceGroupRepo groupRepoMock;

  @Before
  public void init() {
    groupRepoMock = mock(VirtualResourceGroupRepo.class);
    virtualResourceGroupService = new VirtualResourceGroupService();
    virtualResourceGroupService.setVirtualResourceGroupRepo(groupRepoMock);
  }

  @Test
  public void findVirtualResourceGroupsForUser() {
    String groupOfLoggedInUser = "urn:myfirstgroup";
    VirtualResourceGroup vGroup = new VirtualResourceGroupFactory().create();

    RichUserDetails loggedInUser = new RichUserDetailsFactory().addUserGroup(groupOfLoggedInUser).create();

    when(groupRepoMock.findBySurfConnextGroupNameIn(Lists.newArrayList(groupOfLoggedInUser))).thenReturn(
        ImmutableList.of(vGroup));

    Collection<VirtualResourceGroup> groups = virtualResourceGroupService.findAllForUser(loggedInUser);

    assertThat(groups, hasSize(1));
    assertThat(groups, contains(vGroup));
  }
}
