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
package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.junit.Test;

public class VirtualPortTest {

  @Test
  public void getPhysicalResourceGroup() {
    PhysicalResourceGroup physicalGroup = new PhysicalResourceGroupFactory().create();
    PhysicalPort physicalPort = new PhysicalPortFactory().setPhysicalResourceGroup(physicalGroup).create();
    VirtualPort port = new VirtualPortFactory().setPhysicalPort(physicalPort).create();

    PhysicalResourceGroup group = port.getPhysicalResourceGroup();

    assertThat(group, is(physicalGroup));
  }

  @Test
  public void getPhysicalResourceGroup2() {
    VirtualPort port = new VirtualPortFactory().setPhysicalPortAdminGroup(null).setPhysicalPort(null).create();

    PhysicalResourceGroup group = port.getPhysicalResourceGroup();

    assertThat(group, nullValue());
  }

  @Test
  public void toStringShouldContainName() {
    VirtualPort port = new VirtualPortFactory().setManagerLabel("great port").create();

    assertThat(port.toString(), containsString("great port"));
  }

  @Test
  public void whenUserLabelIsNullShoulReturnMangerLabel() {
    VirtualPort port = new VirtualPortFactory().setManagerLabel("manager label").setUserLabel(null).create();

    assertThat(port.getUserLabel(), is("manager label"));
  }

  @Test
  public void whenUserLabelIsEmptyShoulReturnMangerLabel() {
    VirtualPort port = new VirtualPortFactory().setManagerLabel("manager label").setUserLabel("").create();

    assertThat(port.getUserLabel(), is("manager label"));
  }

  @Test
  public void whenUserLabelIsNotEmptyShoulReturnUserLabel() {
    VirtualPort port = new VirtualPortFactory().setManagerLabel("manager label").setUserLabel("user label").create();

    assertThat(port.getUserLabel(), is("user label"));
  }

  @Test
  public void getAdminGroupsShouldReturnAdminGroups() {
    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().setAdminGroup("urn:user1").create();
    VirtualPort subject = new VirtualPortFactory().setVirtualResourceGroup(vrg).setPhysicalPortAdminGroup("urn:user2").create();

    assertThat(subject.getAdminGroups(), hasSize(2));
    assertThat(subject.getAdminGroups(), hasItems("urn:user1", "urn:user2"));
  }

}