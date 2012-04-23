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
package nl.surfnet.bod.web.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.service.GroupService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.support.*;
import nl.surfnet.bod.web.security.Security.RoleEnum;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

@RunWith(MockitoJUnitRunner.class)
public class RichUserDetailsServiceTest {

  @InjectMocks
  private RichUserDetailsService subject;
  @Mock
  private GroupService groupServiceMock;
  @Mock
  private PhysicalResourceGroupService prgServiceMock;
  @Mock
  private VirtualResourceGroupService vrgServiceMock;

  @Before
  public void init() {
    subject.setNocEngineerGroupId("urn:noc-engineer");
  }

  @Test
  public void userWithoutTeamsShouldGetUserAuthority() {
    RichUserDetails userDetails = subject.loadUserDetails(new PreAuthenticatedAuthenticationToken(new RichPrincipal(
        "urn:alanvdam", "Alan van Dam", "alan@test.com"), "N/A"));

    assertThat(userDetails.getNameId(), is("urn:alanvdam"));
    assertThat(userDetails.getDisplayName(), is("Alan van Dam"));

    assertThat(userDetails.getAuthorities(), hasSize(1));
    assertThat(Iterables.getOnlyElement(userDetails.getAuthorities()).getAuthority(), is(RoleEnum.USER.name()));
  }

  @Test
  public void aUserWithOneTeamShouldGetTheUserAuthority() {
    ImmutableList<UserGroup> userGroups = listOf(new UserGroupFactory().setId("urn:klimaat-onderzoekers").create());
    when(groupServiceMock.getGroups("urn:alanvdam")).thenReturn(userGroups);
    when(vrgServiceMock.findByUserGroups(userGroups)).thenReturn(listOf(new VirtualResourceGroupFactory().create()));

    RichUserDetails userDetails = subject.loadUserDetails(new PreAuthenticatedAuthenticationToken(new RichPrincipal(
        "urn:alanvdam", "Alan van Dam", "alan@test.com"), "N/A"));

    assertThat(userDetails.getNameId(), is("urn:alanvdam"));
    assertThat(userDetails.getDisplayName(), is("Alan van Dam"));

    assertThat(userDetails.getAuthorities(), hasSize(1));
    assertThat(userDetails.getAuthorities().iterator().next().getAuthority(), is(Security.RoleEnum.USER.name()));
  }

  @Test
  public void aNocEngineerShouldGetAuthoritiesNocAndUser() {
    when(groupServiceMock.getGroups("urn:alanvdam")).thenReturn(
        listOf(new UserGroupFactory().setId("urn:noc-engineer").create()));

    RichUserDetails userDetails = subject.loadUserDetails(createToken("urn:alanvdam"));

    assertThat(userDetails.getNameId(), is("urn:alanvdam"));
    assertThat(userDetails.getDisplayName(), is("Alan van Dam"));

    assertThat(userDetails.getBodRoles(), hasSize(2));
    assertThat(userDetails.getAuthorities(), hasSize(2));

    assertThat(userDetails.getAuthorities(),
        Matchers.<GrantedAuthority> hasItem(hasProperty("authority", is(RoleEnum.NOC_ENGINEER.name()))));

    assertThat(userDetails.getAuthorities(),
        Matchers.<GrantedAuthority> hasItem(hasProperty("authority", is(RoleEnum.USER.name()))));
  }

  @Test
  public void aManagerShouldGetAuthoritiesManagerAndUser() {
    UserGroup userGroup = new UserGroupFactory().setId("urn:ict-manager").create();

    when(groupServiceMock.getGroups("urn:alanvdam")).thenReturn(listOf(userGroup));

    when(prgServiceMock.hasRelatedPhysicalResourceGroup(userGroup)).thenReturn(true);

    when(prgServiceMock.findByAdminGroup(userGroup.getId())).thenReturn(
        listOf(new PhysicalResourceGroupFactory().create()));

    RichUserDetails userDetails = subject.loadUserDetails(createToken("urn:alanvdam"));

    assertThat(userDetails.getAuthorities(), hasSize(2));
    assertThat(userDetails.getAuthorities(),
        Matchers.<GrantedAuthority>hasItem(hasProperty("authority", is(RoleEnum.ICT_MANAGER.name()))));
    assertThat(userDetails.getAuthorities(),
        Matchers.<GrantedAuthority>hasItem(hasProperty("authority", is(RoleEnum.USER.name()))));
  }

  @Test
  public void shouldUpdateVirtualResourceGroupsIfChangedInSurfconext() {
    ImmutableList<UserGroup> userGroups = listOf(new UserGroupFactory().setId("urn:nameGroup").setName("new name")
        .create(), new UserGroupFactory().setId("urn:descGroup").setDescription("new desc").create());

    VirtualResourceGroup vrgNewName = new VirtualResourceGroupFactory().setName("old name").create();
    VirtualResourceGroup vrgNewDesc = new VirtualResourceGroupFactory().setDescription("old desc").create();

    when(groupServiceMock.getGroups("urn:alanvdam")).thenReturn(userGroups);
    when(vrgServiceMock.findBySurfconextGroupId("urn:nameGroup")).thenReturn(vrgNewName);
    when(vrgServiceMock.findBySurfconextGroupId("urn:descGroup")).thenReturn(vrgNewDesc);

    subject.loadUserDetails(createToken("urn:alanvdam"));

    assertThat(vrgNewName.getName(), is("new name"));
    assertThat(vrgNewDesc.getDescription(), is("new desc"));
    verify(vrgServiceMock).update(vrgNewDesc);
    verify(vrgServiceMock).update(vrgNewName);
  }

  @Test
  public void shouldSetDefaultSelectedRole() {
    UserGroup userGroup = new UserGroupFactory().setId("urn:nameGroup").setName("new name").create();

    PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().create();

    when(groupServiceMock.getGroups("urn:alanvdam")).thenReturn(listOf(userGroup));
    when(prgServiceMock.hasRelatedPhysicalResourceGroup(userGroup)).thenReturn(true);
    when(prgServiceMock.findByAdminGroup(userGroup.getId())).thenReturn(listOf(prg));

    RichUserDetails userDetails = subject.loadUserDetails(new PreAuthenticatedAuthenticationToken(new RichPrincipal(
        "urn:alanvdam", "Alan van Dam", "alan@test.com"), "N/A"));

    assertThat(userDetails.getBodRoles(), hasSize(2));
    assertThat(userDetails.getSelectedRole().getRole(), is(RoleEnum.ICT_MANAGER));
  }

  @Test
  public void shouldContainUserGroupData() {
    UserGroup userGroup = new UserGroupFactory().setId("urn:nameGroup").setName("new name").create();
    RichUserDetails userDetails = new RichUserDetailsFactory().addUserGroup(userGroup).create();

    Institute institute = new InstituteFactory().create();
    PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().setInstitute(institute).create();

    when(groupServiceMock.getGroups("urn:alanvdam")).thenReturn(listOf(userGroup));
    when(prgServiceMock.findByAdminGroup(userGroup.getId())).thenReturn(listOf(prg));
    // Force role Manager
    when(prgServiceMock.hasRelatedPhysicalResourceGroup(userGroup)).thenReturn(true);

    Collection<BodRole> roles = subject.determineRoles(userDetails.getUserGroups());

    assertThat(roles, hasSize(2));

    BodRole managerRole = Iterables.find(roles, new Predicate<BodRole>() {
      @Override
      public boolean apply(BodRole role) {
        return role.getRole() == RoleEnum.ICT_MANAGER;
      }
    });

    assertThat(managerRole.getInstituteName(), is(institute.getName()));
    assertThat(managerRole.getPhysicalResourceGroupId(), is(prg.getId()));
  }

  @Test
  public void shouldBeNocRole() {
    UserGroup userGroup = new UserGroupFactory().setId(subject.getNocEngineerGroupId()).setName("new name").create();

    Collection<BodRole> roles = subject.determineRoles(listOf(userGroup));

    assertThat(roles, hasSize(2));
    assertThat(roles, Matchers.<BodRole> hasItems(hasProperty("role", is(RoleEnum.NOC_ENGINEER))));
    assertThat(roles, Matchers.<BodRole> hasItems(hasProperty("role", is(RoleEnum.NEW_USER))));
  }

  @Test
  public void shouldBeManagerRole() {
    UserGroup userGroup = new UserGroupFactory().create();
    PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().setAdminGroup(userGroup.getId()).create();

    when(prgServiceMock.findByAdminGroup(userGroup.getId())).thenReturn(listOf(prg));
    when(prgServiceMock.hasRelatedPhysicalResourceGroup(userGroup)).thenReturn(true);

    Collection<BodRole> roles = subject.determineRoles(listOf(userGroup));

    assertThat(roles, hasSize(2));

    assertThat(roles, Matchers.<BodRole> hasItems(hasProperty("role", is(RoleEnum.ICT_MANAGER))));
    assertThat(roles, Matchers.<BodRole> hasItems(hasProperty("role", is(RoleEnum.NEW_USER))));
  }

  @Test
  public void shouldBeUserRole() {
    UserGroup userGroup = new UserGroupFactory().setName("new name").create();
    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();

    when(vrgServiceMock.findByUserGroups(listOf(userGroup))).thenReturn(listOf(vrg));

    Collection<BodRole> roles = subject.determineRoles(listOf(userGroup));

    assertThat(roles, hasSize(1));
    assertThat(Security.RoleEnum.USER, is(Iterables.getOnlyElement(roles).getRole()));
  }

  @Test
  public void shouldAddUserRoleOnlyOnce() {
    UserGroup userGroup1 = new UserGroupFactory().setName("new name one").create();
    UserGroup userGroup2 = new UserGroupFactory().setName("new name two").create();
    VirtualResourceGroup vrg1 = new VirtualResourceGroupFactory().create();
    VirtualResourceGroup vrg2 = new VirtualResourceGroupFactory().create();

    when(vrgServiceMock.findByUserGroups(anyCollection())).thenReturn(listOf(vrg1, vrg2));

    Collection<BodRole> roles = subject.determineRoles(listOf(userGroup1, userGroup2));

    assertThat(roles, hasSize(1));
    assertThat(Iterables.getOnlyElement(roles).getRole(), is(Security.RoleEnum.USER));
  }

  @Test
  public void shouldAddTwoManagerRolesBecauseOfDifferentInstitutes() {
    UserGroup userGroup = new UserGroupFactory().create();
    PhysicalResourceGroup prg1 = new PhysicalResourceGroupFactory().setAdminGroup(userGroup.getId()).create();
    PhysicalResourceGroup prg2 = new PhysicalResourceGroupFactory().setAdminGroup(userGroup.getId()).create();

    when(prgServiceMock.findByAdminGroup(userGroup.getId())).thenReturn(listOf(prg1, prg2));
    when(prgServiceMock.hasRelatedPhysicalResourceGroup(userGroup)).thenReturn(true);

    Collection<BodRole> roles = subject.determineRoles(listOf(userGroup));

    assertThat(roles, hasSize(3));

    Collection<BodRole> managerRoles = Collections2.filter(roles, new Predicate<BodRole>() {
      @Override
      public boolean apply(BodRole role) {
        return role.getRole() == RoleEnum.ICT_MANAGER;
      }
    });
    assertThat(managerRoles, hasSize(2));

    assertThat(Iterables.get(managerRoles, 0).getPhysicalResourceGroupId(), not(Iterables.get(managerRoles, 1)
        .getPhysicalResourceGroupId()));
  }

  private static <E> ImmutableList<E> listOf(E... elements) {
    return ImmutableList.copyOf(elements);
  }

  private static Authentication createToken(String nameId) {
    return new PreAuthenticatedAuthenticationToken(new RichPrincipal(nameId, "Alan van Dam", "alan@test.com"), "N/A");
  }
}
