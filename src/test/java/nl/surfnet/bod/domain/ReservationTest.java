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
import static org.hamcrest.Matchers.is;

import java.util.Calendar;

import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.junit.Before;
import org.junit.Test;

public class ReservationTest {

  private Long id;

  private Integer version;

  private VirtualResourceGroup virtualResourceGroup;

  private ReservationStatus reservationStatus;

  private VirtualPort sourcePort;

  private VirtualPort endPort;

  private Calendar startTimeStamp;

  private Calendar endTimeStamp;

  private String user;

  @Before
  public void setUp() {

    id = 1l;
    version = 1;
    reservationStatus = ReservationStatus.PENDING;
    sourcePort = new VirtualPortFactory().setPhysicalPort(new PhysicalPortFactory().setName("startPort").create())
        .create();
    endPort = new VirtualPortFactory().setPhysicalPort(new PhysicalPortFactory().setName("endPort").create()).create();
    startTimeStamp = Calendar.getInstance();
    endTimeStamp = Calendar.getInstance();
    endTimeStamp.add(Calendar.DAY_OF_MONTH, 1);
    user = "SurfUser";

    virtualResourceGroup = new VirtualResourceGroupFactory().addVirtualPorts(sourcePort, endPort).create();
  }

  @Test
  public void testSetters() {
    Reservation reservation = new ReservationFactory().setEndPort(endPort).setEndTimeStamp(endTimeStamp.getTime())
        .setId(id).setReservationStatus(reservationStatus).setSourcePort(sourcePort)
        .setStartTimeStamp(startTimeStamp.getTime()).setVersion(version).setVirtualResourceGroup(virtualResourceGroup)
        .setUser(user).create();

    assertThat(reservation.getDestinationPort(), is(endPort));
    assertThat(reservation.getEndDate(), is(endTimeStamp.getTime()));
    assertThat(reservation.getId(), is(id));
    assertThat(reservation.getStatus(), is(reservationStatus));
    assertThat(reservation.getSourcePort(), is(sourcePort));
    assertThat(reservation.getStartDate(), is(startTimeStamp.getTime()));
    assertThat(reservation.getVersion(), is(version));
    assertThat(reservation.getVirtualResourceGroup(), is(virtualResourceGroup));
    assertThat(reservation.getUser(), is(user));
  }

}
