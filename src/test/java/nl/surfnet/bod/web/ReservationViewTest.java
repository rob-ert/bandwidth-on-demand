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
package nl.surfnet.bod.web;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.web.view.ReservationView;

import org.junit.Test;

public class ReservationViewTest {

  @Test
  public void reservationViewShouldShowUserLabel() {
    VirtualPort sourcePort = new VirtualPortFactory().setManagerLabel("Label of boss").setUserLabel("My source label")
        .create();
    VirtualPort destPort = new VirtualPortFactory().setManagerLabel("Label of boss").setUserLabel("My dest label")
        .create();
    Reservation reservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort).create();

    ReservationView view = new ReservationView(reservation, false);

    assertThat(view.getSourcePort(), is("My source label"));
    assertThat(view.getDestinationPort(), is("My dest label"));
  }

}
