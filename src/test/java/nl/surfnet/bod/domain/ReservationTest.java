/**
 * Copyright (c) 2012, 2013 SURFnet BV
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

import static nl.surfnet.bod.matchers.OptionalMatchers.isAbsent;
import static nl.surfnet.bod.matchers.OptionalMatchers.isPresent;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.joda.time.DateTime;
import org.junit.Test;

public class ReservationTest {

  @Test
  public void toStringShouldContainPrimaryKey() {
    Reservation reservation = new ReservationFactory().setId(24L).create();

    String toString = reservation.toString();

    assertThat(toString, containsString("id=24"));
  }

  @Test
  public void toStringShouldIndicateWhenPrimaryKeyIsNotSet() {
    Reservation reservation = new ReservationFactory().withNoIds().create();

    String toString = reservation.toString();

    assertFalse(toString.contains("id=null"));
  }

  @Test
  public void shouldSetStartDateTimeTimeFirst() {
    Reservation reservation = new ReservationFactory().create();

    DateTime now = DateTime.now();

    reservation.setStartTime(now.toLocalTime());
    reservation.setStartDate(now.toLocalDate());

    assertThat(reservation.getStartDateTime(), is(now));
  }

  @Test
  public void shouldSetStartDateTimeDateFirst() {
    Reservation reservation = new ReservationFactory().create();

    DateTime now = DateTime.now();

    reservation.setStartDate(now.toLocalDate());
    reservation.setStartTime(now.toLocalTime());

    assertThat(reservation.getStartDateTime(), is(now));
  }

  @Test
  public void shouldSetEndDateTimeTimeFirst() {
    Reservation reservation = new ReservationFactory().create();

    DateTime now = DateTime.now();

    reservation.setEndTime(now.toLocalTime());
    reservation.setEndDate(now.toLocalDate());

    assertThat(reservation.getEndDateTime(), isPresent(now));
  }

  @Test
  public void shouldSetEndDateTimeDateFirst() {
    Reservation reservation = new ReservationFactory().create();

    DateTime now = DateTime.now();

    reservation.setEndDate(now.toLocalDate());
    reservation.setEndTime(now.toLocalTime());

    assertThat(reservation.getEndDateTime(), isPresent(now));
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
    Reservation subject = new ReservationFactory().create();

    subject.setEndDate(null);

    assertThat(subject.getEndDate(), nullValue());
    assertThat(subject.getEndTime(), nullValue());
    assertThat(subject.getEndDateTime(), isAbsent());

    assertThat(subject.getStartDate(), notNullValue());
    assertThat(subject.getStartTime(), notNullValue());
    assertThat(subject.getStartDateTime(), notNullValue());
  }

  @Test
  public void shouldSetNullEndTime() {
    Reservation subject = new ReservationFactory().setEndDateTime(null).create();

    assertThat(subject.getEndDate(), nullValue());
    assertThat(subject.getEndTime(), nullValue());
    assertThat(subject.getEndDateTime(), isAbsent());

    assertThat(subject.getStartDate(), notNullValue());
    assertThat(subject.getStartTime(), notNullValue());
    assertThat(subject.getStartDateTime(), notNullValue());
  }

  @Test(expected = IllegalStateException.class)
  public void sourcePortVirtualResourceGroupDiffersFromDestinationPortVirtualResourceGroup() {
    VirtualResourceGroup virtualResourceGroup = new VirtualResourceGroupFactory().setAdminGroup("urn:different")
        .create();
    VirtualPort port = new VirtualPortFactory().setVirtualResourceGroup(virtualResourceGroup).create();

    new ReservationFactory().setSourcePort(port).create();
  }

  @Test(expected = IllegalStateException.class)
  public void destinationPortVirtualResourceGroupDiffersFromSourcePortVirtualResourceGroup() {
    VirtualResourceGroup virtualResourceGroup = new VirtualResourceGroupFactory().setAdminGroup("urn:different")
        .create();
    VirtualPort port = new VirtualPortFactory().setVirtualResourceGroup(virtualResourceGroup).create();

    new ReservationFactory().setDestinationPort(port).create();
  }

  @Test
  public void determineAdminGroupsWithDifferentPhysicalResourceGroups() {
    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().setAdminGroup("urn:vrg").create();
    VirtualPort source = new VirtualPortFactory().setVirtualResourceGroup(vrg).setPhysicalPortAdminGroup("urn:prg1")
        .create();
    VirtualPort destination = new VirtualPortFactory().setVirtualResourceGroup(vrg).setPhysicalPortAdminGroup(
        "urn:prg2").create();

    Reservation subject = new ReservationFactory().setSourcePort(source).setDestinationPort(destination).create();

    assertThat(subject.getAdminGroups(), hasSize(3));
    assertThat(subject.getAdminGroups(), hasItems("urn:prg1", "urn:prg2", "urn:vrg"));
  }

  @Test
  public void determineAdminGroupsForTheSamePhysicalResourceGroup() {
    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().setAdminGroup("urn:vrg").create();
    VirtualPort source = new VirtualPortFactory().setVirtualResourceGroup(vrg).setPhysicalPortAdminGroup("urn:prg")
        .create();
    VirtualPort destination = new VirtualPortFactory().setVirtualResourceGroup(vrg)
        .setPhysicalPortAdminGroup("urn:prg").create();

    Reservation subject = new ReservationFactory().setSourcePort(source).setDestinationPort(destination).create();

    assertThat(subject.getAdminGroups(), hasSize(2));
    assertThat(subject.getAdminGroups(), hasItems("urn:prg", "urn:vrg"));
  }

}
