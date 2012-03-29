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
import nl.surfnet.bod.support.PhysicalPortFactory;

import org.junit.Test;

public class PhysicalPortTest {

  @Test
  public void toStringShouldContainName() {
    PhysicalPort port = new PhysicalPortFactory().setNocLabel("pooooort").create();

    assertThat(port.toString(), containsString("pooooort"));
  }

  @Test
  public void whenManagerLabelIsEmptyShouldReturnNocLabel() {
    PhysicalPort port = new PhysicalPortFactory().setNocLabel("Noc label").setManagerLabel(null).create();

    assertThat(port.getManagerLabel(), is("Noc label"));
    assertThat(port.hasManagerLabel(), is(false));
  }

  @Test
  public void whenManagerLabelIsSet() {
    PhysicalPort port = new PhysicalPortFactory().setNocLabel("Noc label").setManagerLabel("Manager label").create();

    assertThat(port.getManagerLabel(), is("Manager label"));
    assertThat(port.hasManagerLabel(), is(true));
  }
}
