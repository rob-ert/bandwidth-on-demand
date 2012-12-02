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
package nl.surfnet.bod.web.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.VirtualPortJsonView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VirtualResourceGroupControllerTest {

  @InjectMocks
  private VirtualResourceGroupController subject;

  @Mock
  private VirtualResourceGroupService vrgServiceMock;

  @Test
  public void shouldFindPortsForVirtualResourceGroupId() {
    Security.setUserDetails(new RichUserDetailsFactory().addUserGroup("urn:group").create());

    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().setAdminGroup("urn:group")
        .addVirtualPorts(new VirtualPortFactory().create()).create();

    when(vrgServiceMock.find(2L)).thenReturn(vrg);

    Collection<VirtualPortJsonView> ports = subject.listForVirtualResourceGroup(2L);

    assertThat(ports, hasSize(1));
  }

  @Test
  public void whenGroupDoesNotExistPortShouldBeEmpty() {
    when(vrgServiceMock.find(1L)).thenReturn(null);

    Collection<VirtualPortJsonView> ports = subject.listForVirtualResourceGroup(1L);

    assertThat(ports, hasSize(0));
  }

  @Test
  public void whenUserIsNotAMemberOfGroupPortShouldBeEmpty() {
    Security.setUserDetails(new RichUserDetailsFactory().create());
    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().setAdminGroup("urn:group")
        .addVirtualPorts(new VirtualPortFactory().create()).create();

    when(vrgServiceMock.find(2L)).thenReturn(vrg);

    Collection<VirtualPortJsonView> ports = subject.listForVirtualResourceGroup(2L);

    assertThat(ports, hasSize(0));
  }

}
