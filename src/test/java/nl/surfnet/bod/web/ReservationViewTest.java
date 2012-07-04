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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.view.ElementActionView;
import nl.surfnet.bod.web.view.ReservationView;

import org.junit.Test;

public class ReservationViewTest {

  @Test
  public void reservationViewShouldShowUserLabel() {
    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();

    VirtualPort sourcePort = new VirtualPortFactory().setVirtualResourceGroup(vrg).setManagerLabel("Label of boss")
        .setUserLabel("My source label").create();
    VirtualPort destPort = new VirtualPortFactory().setVirtualResourceGroup(vrg).setManagerLabel("Label of boss")
        .setUserLabel("My dest label").create();
    Reservation reservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort).create();

    ReservationView view = new ReservationView(reservation, new ElementActionView(false));

    assertThat(view.getSourcePort().getUserLabel(), is("My source label"));
    assertThat(view.getSourcePort().getManagerLabel(), is("Label of boss"));

    assertThat(view.getDestinationPort().getUserLabel(), is("My dest label"));
    assertThat(view.getDestinationPort().getManagerLabel(), is("Label of boss"));
  }

  @Test
  public void shouldShowDisallowedActionAndReason() {
    Reservation reservation = new ReservationFactory().create();
    ElementActionView disallowedAction = new ElementActionView(false, "too_hot_outside");

    ReservationView reservationView = new ReservationView(reservation, disallowedAction);

    assertThat(reservationView.isDeleteAllowedForSelectedRole(), is(false));
    assertThat(reservationView.getDeleteReasonKey(), is("too_hot_outside"));
  }

  @Test
  public void shouldShowAllowedActionNoReason() {
    Reservation reservation = new ReservationFactory().create();
    ElementActionView disallowedAction = new ElementActionView(true);

    ReservationView reservationView = new ReservationView(reservation, disallowedAction);

    assertThat(reservationView.isDeleteAllowedForSelectedRole(), is(true));
    assertThat(reservationView.getDeleteReasonKey(), nullValue());
  }

}
