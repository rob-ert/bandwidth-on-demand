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
package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.VirtualPortFactory;

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

}
