/**
 * Copyright (c) 2012, SURFnet BV
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
package nl.surfnet.bod.web;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import nl.surfnet.bod.domain.*;
import org.junit.Before;
import org.junit.Test;

import nl.surfnet.bod.support.ConnectionV1Factory;
import nl.surfnet.bod.support.ConnectionV2Factory;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.view.ElementActionView;
import nl.surfnet.bod.web.view.ReservationView;
import org.ogf.schemas.nsi._2013._04.connection.types.LifecycleStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.ProvisionStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType;

public class ReservationViewTest {

  private ElementActionView dummyElementActionView;


  @Before
  public void before(){
    dummyElementActionView = new ElementActionView(false, "too_hot_outside");
  }

  @Test
  public void reservationViewShouldShowUserLabel() {
    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();

    VirtualPort sourcePort = new VirtualPortFactory().setVirtualResourceGroup(vrg).setManagerLabel("Label of boss")
        .setUserLabel("My source label").create();
    VirtualPort destPort = new VirtualPortFactory().setVirtualResourceGroup(vrg).setManagerLabel("Label of boss")
        .setUserLabel("My dest label").create();
    Reservation reservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort).create();

    ReservationView view = new ReservationView(reservation, new ElementActionView(false), new ElementActionView(false));

    assertThat(view.getSourcePort().getUserLabel(), is("My source label"));
    assertThat(view.getSourcePort().getManagerLabel(), is("Label of boss"));

    assertThat(view.getDestinationPort().getUserLabel(), is("My dest label"));
    assertThat(view.getDestinationPort().getManagerLabel(), is("Label of boss"));
  }

  @Test
  public void shouldShowDisallowedActionAndReason() {
    Reservation reservation = new ReservationFactory().create();
    ElementActionView disallowedAction = new ElementActionView(false, "too_hot_outside");

    ReservationView reservationView = new ReservationView(reservation, disallowedAction, disallowedAction);

    assertThat(reservationView.isDeleteAllowedForSelectedRole(), is(false));
    assertThat(reservationView.getDeleteReasonKey(), is("too_hot_outside"));
  }

  @Test
  public void shouldShowAllowedActionNoReason() {
    Reservation reservation = new ReservationFactory().create();
    ElementActionView disallowedAction = new ElementActionView(true);

    ReservationView reservationView = new ReservationView(reservation, disallowedAction, disallowedAction);

    assertThat(reservationView.isDeleteAllowedForSelectedRole(), is(true));
    assertThat(reservationView.getDeleteReasonKey(), nullValue());
  }

  @Test
  public void shouldNotFillConnectionFieldsWhenNoConnectionPresent(){
    Reservation reservation = new ReservationFactory().create();
    ReservationView reservationView = new ReservationView(reservation, dummyElementActionView, dummyElementActionView);

    assertThat(reservationView.getConnectionId(), nullValue());
    assertThat(reservationView.getReservationState(), nullValue());
    assertThat(reservationView.getProvisionState(), nullValue());
    assertThat(reservationView.getLifeCycleState(), nullValue());
    assertThat(reservationView.getDataPlaneActive(), nullValue());

  }

  @Test
  public void shouldOnlyFillConnectionStateWhenV1ConnectionPresent(){
    String id = "foo";
    ConnectionV1 connectionV1 = new ConnectionV1Factory().setConnectionId(id).create();

    Reservation reservation = new ReservationFactory().setConnectionV1(connectionV1).create();
    ReservationView reservationView = new ReservationView(reservation, dummyElementActionView, dummyElementActionView);

    assertThat(reservationView.getConnectionId(), is(id));
    assertThat(reservationView.getReservationState(), nullValue());
    assertThat(reservationView.getProvisionState(), nullValue());
    assertThat(reservationView.getLifeCycleState(), nullValue());
    assertThat(reservationView.getDataPlaneActive(), nullValue());
  }

  @Test
  public void shouldFillNsiV2FieldsWhenV2ConnectionPresent(){
    String id = "foo";

    final ReservationStateEnumType reservationState = ReservationStateEnumType.RESERVE_CHECKING;
    final ProvisionStateEnumType provisionState = ProvisionStateEnumType.PROVISIONED;
    final LifecycleStateEnumType lifecycleState = LifecycleStateEnumType.CREATED;
    final boolean dataPlaneActive = true;

    ConnectionV2 connectionV2 = new ConnectionV2Factory()
      .setReservationState(reservationState)
      .setProvisionState(provisionState)
      .setLifecycleState(lifecycleState)
      .setDataPlaneActive(dataPlaneActive)
      .setConnectionId(id).create();


    Reservation reservation = new ReservationFactory().setConnectionV2(connectionV2).create();
    ReservationView reservationView = new ReservationView(reservation, dummyElementActionView, dummyElementActionView);

    assertThat(reservationView.getConnectionId(), is(id));
    assertThat(reservationView.getReservationState(), is(reservationState.name()));
    assertThat(reservationView.getProvisionState(), is(provisionState.name()));
    assertThat(reservationView.getLifeCycleState(), is(lifecycleState.name()));
    assertThat(reservationView.getDataPlaneActive(), is(dataPlaneActive));
  }

}
