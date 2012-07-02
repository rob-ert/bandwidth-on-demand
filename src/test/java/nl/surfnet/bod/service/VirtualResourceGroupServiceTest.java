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
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.UserGroupFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class VirtualResourceGroupServiceTest {

  @InjectMocks
  private VirtualResourceGroupService subject;

  @Mock
  private VirtualResourceGroupRepo groupRepoMock;

  @SuppressWarnings(value = "unused")
  @Mock
  private LogEventService logEventService;

  @Test
  public void findVirtualResourceGroupsForUser() {
    String groupOfLoggedInUser = "urn:myfirstgroup";
    VirtualResourceGroup vGroup = new VirtualResourceGroupFactory().create();

    RichUserDetails loggedInUser = new RichUserDetailsFactory().addUserGroup(groupOfLoggedInUser).create();

    when(groupRepoMock.findBySurfconextGroupIdIn(Lists.newArrayList(groupOfLoggedInUser))).thenReturn(
        ImmutableList.of(vGroup));

    Collection<VirtualResourceGroup> groups = subject.findAllForUser(loggedInUser);

    assertThat(groups, hasSize(1));
    assertThat(groups, contains(vGroup));
  }

  @Test
  public void findVirtualResourceGroupsWhenUserHasNoGroups() {
    RichUserDetails loggedInUser = new RichUserDetailsFactory().create();

    Collection<VirtualResourceGroup> groups = subject.findAllForUser(loggedInUser);

    assertThat(groups, hasSize(0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void findEntriesWithMaxResultZeroShouldGiveAnException() {
    subject.findEntries(1, 0, new Sort("id"));
  }

  @Test
  public void findEntries() {
    VirtualResourceGroup group = new VirtualResourceGroupFactory().create();

    when(groupRepoMock.findAll(any(PageRequest.class))).thenReturn(
        new PageImpl<VirtualResourceGroup>(Lists.newArrayList(group)));

    List<VirtualResourceGroup> groups = subject.findEntries(5, 10, null);

    assertThat(groups, contains(group));
  }

  @Test
  public void countShouldCount() {
    when(groupRepoMock.count()).thenReturn(2L);

    long count = subject.count();

    assertThat(count, is(2L));
  }

  @Test
  public void delete() {
    VirtualResourceGroup group = new VirtualResourceGroupFactory().create();

    subject.delete(group);

    verify(groupRepoMock).delete(group);
  }

  @Test
  public void update() {
    VirtualResourceGroup group = new VirtualResourceGroupFactory().create();

    subject.update(group);

    verify(groupRepoMock).save(group);
  }

  @Test
  public void findByUserGroupsShouldBeEmptyListIfCalledWithNoGroups() {
    Collection<VirtualResourceGroup> vrgs = subject.findByUserGroups(Collections.<UserGroup> emptyList());

    assertThat(vrgs, hasSize(0));

    verifyZeroInteractions(groupRepoMock);
  }

  @Test
  public void findByUserGroupsShouldMatchingGroups() {
    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();
    when(groupRepoMock.findBySurfconextGroupIdIn(Lists.newArrayList("urn:mygroup")))
        .thenReturn(Lists.newArrayList(vrg));

    Collection<VirtualResourceGroup> vrgs = subject.findByUserGroups(Lists.newArrayList(new UserGroupFactory().setId(
        "urn:mygroup").create()));

    assertThat(vrgs, contains(vrg));
  }
}
