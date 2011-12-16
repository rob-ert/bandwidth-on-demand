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
package nl.surfnet.bod.nbi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import javax.xml.bind.JAXBException;

import nl.surfnet.bod.nbi.generated.TerminationPoint;

import org.junit.Before;
import org.junit.Test;

public class NbiOfflineClientTest {

  private NbiOfflineClient subject = new NbiOfflineClient();

  @Before
  public void initOfflineClient() throws JAXBException {
    subject = new NbiOfflineClient();
    subject.init();
  }

  @Test
  public void shouldGivePortsBackWithDummyInName() {
    List<TerminationPoint> allPorts = subject.findAllPorts();

    assertThat(allPorts, hasSize(greaterThan(0)));
  }

  @Test
  public void allPortsShouldHaveAName() {
    List<TerminationPoint> allPorts = subject.findAllPorts();

    for (TerminationPoint terminationPoint : allPorts) {
      assertThat(terminationPoint.getPortDetail().getName(), containsString("_dummy"));
    }
  }

}
