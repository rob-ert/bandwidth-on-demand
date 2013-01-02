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
