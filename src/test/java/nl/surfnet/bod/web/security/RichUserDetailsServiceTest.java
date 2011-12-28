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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.service.GroupService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.UserGroupFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class RichUserDetailsServiceTest {

  @InjectMocks
  private RichUserDetailsService subject;
  @Mock
  private GroupService groupServiceMock;
  @Mock
  private PhysicalResourceGroupService prgServiceMock;

  @Before
  public void init() {
    subject.setNocEngineerGroupId("urn:noc-engineer");
  }

  @Test
  public void aNormalUser() {
    RichUserDetails userDetails = subject.loadUserDetails(new PreAuthenticatedAuthenticationToken(new RichPrincipal(
        "urn:alanvdam", "Alan van Dam"), "N/A"));

    assertThat(userDetails.getNameId(), is("urn:alanvdam"));
    assertThat(userDetails.getDisplayName(), is("Alan van Dam"));
    assertThat(userDetails.getAuthorities(), hasSize(0));
  }

  @Test
  public void aNocEngineer() {
    when(groupServiceMock.getGroups("urn:alanvdam")).thenReturn(
        listOf(new UserGroupFactory().setId("urn:noc-engineer").create()));

    RichUserDetails userDetails = subject.loadUserDetails(createToken("urn:alanvdam"));

    assertThat(userDetails.getNameId(), is("urn:alanvdam"));
    assertThat(userDetails.getDisplayName(), is("Alan van Dam"));
    assertThat(userDetails.getAuthorities(), hasSize(1));
    assertThat(userDetails.getAuthorities().iterator().next().getAuthority(), is("NOC_ENGINEER"));
  }

  @Test
  public void aIctManager() {
    ImmutableList<UserGroup> adminGroups = listOf(new UserGroupFactory().setId("urn:ict-manager").create());
    when(groupServiceMock.getGroups("urn:alanvdam")).thenReturn(adminGroups);
    when(prgServiceMock.findAllForAdminGroups(adminGroups)).thenReturn(
        listOf(new PhysicalResourceGroupFactory().create()));

    RichUserDetails userDetails = subject.loadUserDetails(createToken("urn:alanvdam"));

    assertThat(userDetails.getAuthorities(), hasSize(1));
    assertThat(userDetails.getAuthorities().iterator().next().getAuthority(), is("ICT_MANAGER"));
  }

  private static <E> ImmutableList<E> listOf(E element) {
    return ImmutableList.of(element);
  }

  private static Authentication createToken(String nameId) {
    return new PreAuthenticatedAuthenticationToken(new RichPrincipal(nameId, "Alan van Dam"), "N/A");
  }
}
