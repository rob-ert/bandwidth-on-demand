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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class ReservationControllerTest {

  @InjectMocks
  private ReservationController subject;

  @Mock
  private VirtualResourceGroupService virtualResourceGroupServiceMock;


  @Test
  public void newReservationShouldHaveDefaults() {
    RichUserDetails user = new RichUserDetailsFactory().create();
    Security.setUserDetails(user);
    Model model = new ModelStub();

    VirtualPort sourcePort = new VirtualPortFactory().create();
    VirtualPort destPort = new VirtualPortFactory().create();
    VirtualResourceGroup group = new VirtualResourceGroupFactory().addVirtualPorts(sourcePort, destPort).create();

    when(virtualResourceGroupServiceMock.findAllForUser(user)).thenReturn(Lists.newArrayList(group));

    subject.createForm(model);

    assertThat(model.asMap(), hasKey("reservation"));

    Reservation reservation = (Reservation) model.asMap().get("reservation");
    assertThat(reservation.getStartDate(), not(nullValue()));
    assertThat(reservation.getEndDate(), not(nullValue()));
    assertThat(reservation.getSourcePort(), is(sourcePort));
    assertThat(reservation.getDestinationPort(), is(destPort));
  }

}
