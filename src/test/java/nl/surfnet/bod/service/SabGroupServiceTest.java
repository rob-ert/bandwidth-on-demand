/**
 * Copyright (c) 2012, SURFnet BV
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.sabng.EntitlementsHandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class SabGroupServiceTest {

  private final static String SURFNET = "SURFnet";
  private final static String WESAIDSO = "WeSaidSo";
  private final static String NAME_ID = "testName";

  @InjectMocks
  private SabGroupService subject;

  @Mock
  private EntitlementsHandler sabNgEntitlementsHandlerMock;

  @Test
  public void shouldAddSabGroups() {

    List<String> institutes = Lists.newArrayList(SURFNET, WESAIDSO);
    when(sabNgEntitlementsHandlerMock.checkInstitutes(eq(NAME_ID))).thenReturn(institutes);

    Collection<UserGroup> groups = subject.getGroups(NAME_ID);
    assertThat(groups, hasSize(2));
    assertThat(groups, contains(
        new UserGroup(subject.composeGroupName(SURFNET),
            subject.composeName(SURFNET),
            subject.composeDescription(SURFNET)),
        new UserGroup(subject.composeGroupName(WESAIDSO),
            subject.composeName(WESAIDSO),
            subject.composeDescription(WESAIDSO))));
  }

  @Test
  public void shouldNotAddGroups() {
    Collection<UserGroup> groups = subject.getGroups(NAME_ID);
    assertThat(groups, hasSize(0));
  }

  @Test
  public void shouldComposeGroupName() {
    assertThat(subject.composeGroupName(NAME_ID), is(SabGroupService.GROUP_PREFIX + NAME_ID));
  }

  @Test
  public void shouldComposeName() {
    assertThat(subject.composeName(NAME_ID), is(SabGroupService.NAME_PREFIX + NAME_ID));
  }

  @Test
  public void shouldComposeDescription() {
    assertThat(subject.composeDescription(NAME_ID), is(SabGroupService.DESCRIPTION_PREFIX + NAME_ID));
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowOnNullArgumentInComposeGroupName() {
    subject.composeGroupName(null);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowOnNullArgumentInComposeName() {
    subject.composeName(null);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowOnNullArgumentInComposeDescription() {
    subject.composeDescription(null);
  }

}
