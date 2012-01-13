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
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.UserGroupFactory;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalResourceGroupServiceTest {

  @InjectMocks
  private PhysicalResourceGroupService subject;

  @Mock
  private PhysicalResourceGroupRepo groupRepoMock;

  @Mock
  private InstituteService instituteServiceMock;

  private Institute instituteOne = new InstituteFactory().setId(1L).setName("oneInst").create();
  private Institute instituteTwo = new InstituteFactory().setId(2L).setName("twoInst").create();
  private PhysicalResourceGroup physicalResourceGroupOne = new PhysicalResourceGroupFactory()
      .setInstitute(instituteOne).setName("onePrg").create();
  private PhysicalResourceGroup physicalResourceGroupTwo = new PhysicalResourceGroupFactory()
      .setInstitute(instituteTwo).setName("twoPrg").create();
  private List<PhysicalResourceGroup> physicalResourceGroups = ImmutableList.of(physicalResourceGroupOne,
      physicalResourceGroupTwo);

  @Test
  public void findGroupsForUser() {
    RichUserDetails loggedInUser = new RichUserDetailsFactory().addUserGroup("urn:myfirstgroup").create();

    when(groupRepoMock.findByAdminGroupIn(listOf("urn:myfirstgroup"))).thenReturn(listOf(physicalResourceGroupOne));

    Collection<PhysicalResourceGroup> groups = subject.findAllForUser(loggedInUser);

    assertThat(groups, hasSize(1));
    assertThat(groups, contains(physicalResourceGroupOne));
  }

  @Test
  public void findGroupsForUserWithoutUserGroups() {
    RichUserDetails loggedInUser = new RichUserDetailsFactory().create();

    Collection<PhysicalResourceGroup> groups = subject.findAllForUser(loggedInUser);

    assertThat(groups, hasSize(0));
  }

  private static <E> ImmutableList<E> listOf(E element) {
    return ImmutableList.of(element);
  }

  @Test
  public void shouldFillInstitute() {
    when(groupRepoMock.findOne(1L)).thenReturn(physicalResourceGroupOne);
    when(instituteServiceMock.findInstitute(1L)).thenReturn(instituteOne);

    PhysicalResourceGroup foundResourceGroup = subject.find(1L);

    assertThat(foundResourceGroup.getInstitute(), is((instituteOne)));

    assertThat(foundResourceGroup.getInstituteId(), is(instituteOne.getId()));
  }

  @Test
  public void shoudFillInstitutesFindAll() {
    when(groupRepoMock.findAll()).thenReturn(physicalResourceGroups);
    when(instituteServiceMock.findInstitute(1L)).thenReturn(instituteOne);
    when(instituteServiceMock.findInstitute(2L)).thenReturn(instituteTwo);

    List<PhysicalResourceGroup> prgs = subject.findAll();

    Iterator<PhysicalResourceGroup> it = prgs.iterator();
    assertThat(it.next().getInstitute(), is(instituteOne));
    assertThat(it.next().getInstitute(), is(instituteTwo));
  }

  @Test
  public void shoudFillInstitutesFindEntries() {
    Page<PhysicalResourceGroup> pageResult = new PageImpl<PhysicalResourceGroup>(physicalResourceGroups);
    when(groupRepoMock.findAll(new PageRequest(1, 1))).thenReturn(pageResult);

    when(instituteServiceMock.findInstitute(1L)).thenReturn(instituteOne);
    when(instituteServiceMock.findInstitute(2L)).thenReturn(instituteTwo);

    List<PhysicalResourceGroup> prgs = subject.findEntries(1, 1);

    Iterator<PhysicalResourceGroup> it = prgs.iterator();
    assertThat(it.next().getInstitute(), is(instituteOne));
    assertThat(it.next().getInstitute(), is(instituteTwo));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldFillInstitutesFindAllForAdminGroups() {
    UserGroup userGroupOne = new UserGroupFactory().setId("oneUser").create();
    UserGroup userGroupTwo = new UserGroupFactory().setId("twoUser").create();

    Collection<UserGroup> groups = ImmutableList.of(userGroupOne, userGroupTwo);

    when(groupRepoMock.findByAdminGroupIn(anyCollection())).thenReturn(physicalResourceGroups);
    when(instituteServiceMock.findInstitute(1L)).thenReturn(instituteOne);
    when(instituteServiceMock.findInstitute(2L)).thenReturn(instituteTwo);

    Collection<PhysicalResourceGroup> prgs = subject.findAllForAdminGroups(groups);

    Iterator<PhysicalResourceGroup> it = prgs.iterator();
    assertThat(it.next().getInstitute(), is(instituteOne));
    assertThat(it.next().getInstitute(), is(instituteTwo));
  }

  @Test
  public void shouldFillInstitutesFindAllForUser() {
    RichUserDetails user = new RichUserDetailsFactory().addUserGroup("urn:myfirstgroup").create();

    when(groupRepoMock.findByAdminGroupIn(ImmutableList.of("urn:myfirstgroup"))).thenReturn(physicalResourceGroups);
    when(instituteServiceMock.findInstitute(1L)).thenReturn(instituteOne);
    when(instituteServiceMock.findInstitute(2L)).thenReturn(instituteTwo);

    Collection<PhysicalResourceGroup> prgs = subject.findAllForUser(user);

    Iterator<PhysicalResourceGroup> it = prgs.iterator();
    assertThat(it.next().getInstitute(), is(instituteOne));
    assertThat(it.next().getInstitute(), is(instituteTwo));
  }

  @Test
  public void testFillInstitutesFindByName() {

    when(groupRepoMock.findByName("onePrg")).thenReturn(physicalResourceGroupOne);
    when(instituteServiceMock.findInstitute(1L)).thenReturn(instituteOne);

    PhysicalResourceGroup prg = subject.findByName("onePrg");
    assertThat(prg.getInstitute(), is(instituteOne));
  }

}