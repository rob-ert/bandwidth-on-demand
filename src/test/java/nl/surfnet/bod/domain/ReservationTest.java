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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import nl.surfnet.bod.support.ReservationFactory;

import org.joda.time.LocalDateTime;
import org.junit.Test;

public class ReservationTest {

  @Test
  public void toStringShouldContainPrimaryKey() {
    Reservation reservation = new ReservationFactory().setId(24L).create();

    String toString = reservation.toString();

    assertThat(toString, containsString("id=24"));
  }

  @Test
  public void shouldSetStartDateTimeTimeFirst() {
    Reservation reservation = new ReservationFactory().create();

    LocalDateTime now = LocalDateTime.now();

    reservation.setStartTime(now.toLocalTime());
    reservation.setStartDate(now.toLocalDate());

    assertThat(reservation.getStartDateTime(), is(now));
  }

  @Test
  public void shouldSetStartDateTimeDateFirst() {
    Reservation reservation = new ReservationFactory().create();

    LocalDateTime now = LocalDateTime.now();

    reservation.setStartDate(now.toLocalDate());
    reservation.setStartTime(now.toLocalTime());

    assertThat(reservation.getStartDateTime(), is(now));
  }

  @Test
  public void shouldSetEndDateTimeTimeFirst() {
    Reservation reservation = new ReservationFactory().create();

    LocalDateTime now = LocalDateTime.now();

    reservation.setEndTime(now.toLocalTime());
    reservation.setEndDate(now.toLocalDate());

    assertThat(reservation.getEndDateTime(), is(now));
  }

  @Test
  public void shouldSetEndDateTimeDateFirst() {
    Reservation reservation = new ReservationFactory().create();

    LocalDateTime now = LocalDateTime.now();

    reservation.setEndDate(now.toLocalDate());
    reservation.setEndTime(now.toLocalTime());

    assertThat(reservation.getEndDateTime(), is(now));
  }

  @Test
  public void shouldSetNullStartDate() {
    Reservation reservation = new ReservationFactory().create();

    reservation.setStartDate(null);
    assertThat(reservation.getStartDate(), nullValue());
    assertThat(reservation.getStartTime(), nullValue());
    assertThat(reservation.getStartDateTime(), nullValue());

    assertThat(reservation.getEndDate(), notNullValue());
    assertThat(reservation.getEndTime(), notNullValue());
    assertThat(reservation.getEndDateTime(), notNullValue());
  }

  @Test
  public void shouldSetNullStartTime() {
    Reservation reservation = new ReservationFactory().create();

    reservation.setStartTime(null);
    assertThat(reservation.getStartDate(), nullValue());
    assertThat(reservation.getStartTime(), nullValue());
    assertThat(reservation.getStartDateTime(), nullValue());

    assertThat(reservation.getEndDate(), notNullValue());
    assertThat(reservation.getEndTime(), notNullValue());
    assertThat(reservation.getEndDateTime(), notNullValue());
  }

  @Test
  public void shouldSetNullEndDate() {
    Reservation reservation = new ReservationFactory().create();

    reservation.setEndDate(null);

    assertThat(reservation.getEndDate(), nullValue());
    assertThat(reservation.getEndTime(), nullValue());
    assertThat(reservation.getEndDateTime(), nullValue());

    assertThat(reservation.getStartDate(), notNullValue());
    assertThat(reservation.getStartTime(), notNullValue());
    assertThat(reservation.getStartDateTime(), notNullValue());
  }

  @Test
  public void shouldSetNullEndTime() {
    Reservation reservation = new ReservationFactory().create();

    reservation.setEndTime(null);

    assertThat(reservation.getEndDate(), nullValue());
    assertThat(reservation.getEndTime(), nullValue());
    assertThat(reservation.getEndDateTime(), nullValue());

    assertThat(reservation.getStartDate(), notNullValue());
    assertThat(reservation.getStartTime(), notNullValue());
    assertThat(reservation.getStartDateTime(), notNullValue());
  }

}
