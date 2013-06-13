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

  @Mock
  private LogEventService logEventService;

  @Test
  public void findVirtualResourceGroupsForUser() {
    String groupOfLoggedInUser = "urn:myfirstgroup";
    VirtualResourceGroup vGroup = new VirtualResourceGroupFactory().create();

    RichUserDetails loggedInUser = new RichUserDetailsFactory().addUserGroup(groupOfLoggedInUser).create();

    when(groupRepoMock.findByAdminGroupIn(Lists.newArrayList(groupOfLoggedInUser))).thenReturn(
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
    when(groupRepoMock.findByAdminGroupIn(Lists.newArrayList("urn:mygroup")))
        .thenReturn(Lists.newArrayList(vrg));

    Collection<VirtualResourceGroup> vrgs = subject.findByUserGroups(Lists.newArrayList(new UserGroupFactory().setId(
        "urn:mygroup").create()));

    assertThat(vrgs, contains(vrg));
  }
}
