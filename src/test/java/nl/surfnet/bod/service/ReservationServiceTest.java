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
package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.support.ReservationFactory;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class ReservationServiceTest {

  @InjectMocks
  private ReservationService subject;

  @Mock
  private ReservationRepo reservationRepoMock;

  @Test
  public void whenTheUserHasNoGroupsTheReservationsShouldBeEmpty() {
    RichUserDetails richUserDetailsWithoutGroups = new RichUserDetailsFactory().create();
    Security.setUserDetails(richUserDetailsWithoutGroups);

    Collection<Reservation> reservations = subject.findEntries(0, 20);

    assertThat(reservations, hasSize(0));
  }

  @Test
  public void findEntriesShouldFilterOnUserGroups() {
    RichUserDetails richUserDetailsWithoutGroups = new RichUserDetailsFactory().addUserGroup("urn:mygroup").create();
    Security.setUserDetails(richUserDetailsWithoutGroups);

    PageImpl<Reservation> pageResult = new PageImpl<Reservation>(Lists.newArrayList(new ReservationFactory().create()));
    when(reservationRepoMock.findAll(any(Specification.class), any(Pageable.class))).thenReturn(pageResult);

    Collection<Reservation> reservations = subject.findEntries(0, 20);

    assertThat(reservations, hasSize(1));
  }

  @Test
  public void whenTheUserHasNoGroupsCountShouldBeZero() {
    RichUserDetails richUserDetailsWithoutGroups = new RichUserDetailsFactory().create();
    Security.setUserDetails(richUserDetailsWithoutGroups);

    long count = subject.count();

    assertThat(count, is(0L));
  }

  @Test(expected = IllegalStateException.class)
  public void differentVirtualResrouceGroupsShouldGiveAnIllegalStateException() {
    VirtualResourceGroup vrg1 = new VirtualResourceGroupFactory().create();
    VirtualResourceGroup vrg2 = new VirtualResourceGroupFactory().create();
    VirtualPort source = new VirtualPortFactory().setVirtualResourceGroup(vrg1).create();
    VirtualPort destination = new VirtualPortFactory().setVirtualResourceGroup(vrg2).create();

    Reservation reservation = new ReservationFactory().setVirtualResourceGroup(vrg1).setSourcePort(source)
        .setDestinationPort(destination).create();

    subject.save(reservation);
  }

}
