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
package nl.surfnet.bod.service;

import static nl.surfnet.bod.matchers.DateMatchers.isAfterNow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import nl.surfnet.bod.domain.EnniPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationArchive;
import nl.surfnet.bod.domain.ReservationEndPoint;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.UpdatedReservationStatus;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ElementActionView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.AsyncResult;

@RunWith(MockitoJUnitRunner.class)
public class ReservationServiceTest {

  @InjectMocks
  private ReservationService subject;

  @Mock private ReservationRepo reservationRepoMock;
  @Mock private ReservationToNbi reservationToNbiMock;
  @Mock private NbiClient nbiClientMock;
  @Mock private LogEventService logEventService;

  @After
  public void tearDown() {
    DateTimeUtils.setCurrentMillisSystem();
  }

  @Test
  public void whenTheUserHasNoGroupsTheReservationsShouldBeEmpty() {
    Security.setUserDetails(new RichUserDetailsFactory().create());

    List<Reservation> reservations = subject.findEntries(0, 20, new Sort("id"));

    assertThat(reservations, hasSize(0));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void findEntriesShouldFilterOnUserGroups() {
    RichUserDetails richUserDetailsWithGroups = new RichUserDetailsFactory().addUserGroup("urn:mygroup").create();
    Security.setUserDetails(richUserDetailsWithGroups);

    PageImpl<Reservation> pageResult = new PageImpl<Reservation>(Lists.newArrayList(new ReservationFactory().create()));
    when(reservationRepoMock.findAll(any(Specification.class), any(Pageable.class))).thenReturn(pageResult);

    List<Reservation> reservations = subject.findEntries(0, 20, new Sort("id"));

    assertThat(reservations, hasSize(1));
  }

  @Test
  public void whenTheUserHasNoGroupsCountShouldBeZero() {
    RichUserDetails richUserDetailsWithoutGroups = new RichUserDetailsFactory().create();
    Security.setUserDetails(richUserDetailsWithoutGroups);

    long count = subject.countForUser(richUserDetailsWithoutGroups);

    assertThat(count, is(0L));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void countShouldFilterOnUserGroups() {
    RichUserDetails richUserDetailsWithGroups = new RichUserDetailsFactory().addUserGroup("urn:mygroup").create();
    Security.setUserDetails(richUserDetailsWithGroups);

    when(reservationRepoMock.count(any(Specification.class))).thenReturn(5L);

    long count = subject.countForUser(richUserDetailsWithGroups);

    assertThat(count, is(5L));
  }

  @Test(expected = IllegalStateException.class)
  public void reserveDifferentVirtualResrouceGroupsShouldGiveAnIllegalStateException() {
    VirtualResourceGroup vrg1 = new VirtualResourceGroupFactory().create();
    VirtualResourceGroup vrg2 = new VirtualResourceGroupFactory().create();
    VirtualPort source = new VirtualPortFactory().setVirtualResourceGroup(vrg1).create();
    VirtualPort destination = new VirtualPortFactory().setVirtualResourceGroup(vrg2).create();

    Reservation reservation = new ReservationFactory().setSourcePort(source).setDestinationPort(destination).create();

    subject.create(reservation);
  }

  @Test
  public void reserveSameVirtualResourceGroupsShouldBeFine() throws InterruptedException, ExecutionException {
    final Reservation reservation = new ReservationFactory().create();

    when(reservationRepoMock.save(reservation)).thenReturn(reservation);
    subject.create(reservation);

    verify(reservationToNbiMock).asyncReserve(reservation.getId(), true);
  }

  @Test
  public void reserveShouldFillStartTime() {
    final Reservation reservation = new ReservationFactory().setStartDateTime(null).create();

    when(reservationRepoMock.save(reservation)).thenReturn(reservation);
    subject.create(reservation);

    assertThat(reservation.getStartDateTime(), notNullValue());
    assertThat(reservation.getStartDateTime(), isAfterNow());
  }

  @Test(expected = IllegalStateException.class)
  public void updatingDifferentVirtualResourceGroupsShouldGiveAnIllegalStateException() {
    VirtualResourceGroup vrg1 = new VirtualResourceGroupFactory().create();
    VirtualResourceGroup vrg2 = new VirtualResourceGroupFactory().create();
    VirtualPort source = new VirtualPortFactory().setVirtualResourceGroup(vrg1).create();
    VirtualPort destination = new VirtualPortFactory().setVirtualResourceGroup(vrg2).create();

    Reservation reservation = new ReservationFactory().setSourcePort(source).setDestinationPort(destination).create();

    subject.updateStatus(reservation.getReservationId(), UpdatedReservationStatus.forNewStatus(ReservationStatus.AUTO_START));
  }

  @Test
  public void updateStatusShouldSaveNewStatus() {
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.RESERVED).create();
    when(reservationRepoMock.getByReservationIdWithPessimisticWriteLock(reservation.getReservationId())).thenReturn(reservation);

    subject.updateStatus(reservation.getReservationId(), UpdatedReservationStatus.forNewStatus(ReservationStatus.AUTO_START));

    assertThat(reservation.getStatus(), is(ReservationStatus.AUTO_START));
    verify(reservationRepoMock).saveAndFlush(reservation);
  }

  @Test
  public void updateStatusShouldTransitionCancellingReservationToCancelledWhenUpdatedToSucceeded() {
    // MTOSI just returns SUCCEEDED as final state. We need to manage switching it to CANCELLED if the reservation is CANCELLING.
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.CANCELLING).create();
    when(reservationRepoMock.getByReservationIdWithPessimisticWriteLock(reservation.getReservationId())).thenReturn(reservation);

    subject.updateStatus(reservation.getReservationId(), UpdatedReservationStatus.forNewStatus(ReservationStatus.SUCCEEDED));

    assertThat(reservation.getStatus(), is(ReservationStatus.CANCELLED));
  }

  @Test
  public void updateStatusShouldTransitionScheduledReservationToPassedEndTimeWhenUpdatedToSucceeded() {
    DateTime now = DateTime.now();
    // MTOSI just returns SUCCEEDED as final state. We need to manage switching
    // it to PASSED_END_TIME when reservation is not RUNNING.
    Reservation reservation = new ReservationFactory()
        .setStatus(ReservationStatus.SCHEDULED)
        .setStartDateTime(now.minusHours(2))
        .setEndDateTime(now.plusMinutes(10))
        .create();
    when(reservationRepoMock.getByReservationIdWithPessimisticWriteLock(reservation.getReservationId())).thenReturn(reservation);

    subject.updateStatus(reservation.getReservationId(), UpdatedReservationStatus.forNewStatus(ReservationStatus.SUCCEEDED));

    assertThat(reservation.getStatus(), is(ReservationStatus.PASSED_END_TIME));
  }

  @Test
  public void lost_reservation_in_requested_state_should_become_not_accepted_after_grace_period() {
    DateTime createdAt = DateTime.now();
    DateTimeUtils.setCurrentMillisFixed(createdAt.getMillis());
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.REQUESTED).create();
    when(reservationRepoMock.getByReservationIdWithPessimisticWriteLock(reservation.getReservationId())).thenReturn(reservation);

    DateTimeUtils.setCurrentMillisFixed(createdAt.plus(ReservationService.LOST_RESERVATION_GRACE_PERIOD).plusMillis(1).getMillis());
    subject.handleLostReservation(reservation.getReservationId());

    assertThat(reservation.getStatus(), is(ReservationStatus.NOT_ACCEPTED));
    assertThat(reservation.getFailedReason(), is("not accepted by NBI within reservation grace period"));
  }

  @Test
  public void lost_reservation_not_in_requested_state_after_creation_grace_period_is_unaffected() {
    DateTime createdAt = DateTime.now();
    DateTimeUtils.setCurrentMillisFixed(createdAt.getMillis());
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.RESERVED).create();
    when(reservationRepoMock.getByReservationIdWithPessimisticWriteLock(reservation.getReservationId())).thenReturn(reservation);

    DateTimeUtils.setCurrentMillisFixed(createdAt.plus(ReservationService.LOST_RESERVATION_GRACE_PERIOD).plusMillis(1).getMillis());

    subject.handleLostReservation(reservation.getReservationId());

    assertThat(reservation.getStatus(), is(ReservationStatus.RESERVED));
  }

  @Test
  public void lost_reservation_in_requested_state_should_stay_requested_within_grace_period() {
    DateTime createdAt = DateTime.now();
    DateTimeUtils.setCurrentMillisFixed(createdAt.getMillis());
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.REQUESTED).create();
    when(reservationRepoMock.getByReservationIdWithPessimisticWriteLock(reservation.getReservationId())).thenReturn(reservation);

    DateTimeUtils.setCurrentMillisFixed(createdAt.plus(ReservationService.LOST_RESERVATION_GRACE_PERIOD).getMillis());
    subject.handleLostReservation(reservation.getReservationId());

    assertThat(reservation.getStatus(), is(ReservationStatus.REQUESTED));
  }

  @Test
  public void running_lost_reservation_past_end_time_and_grace_period_becomes_succeeded() {
    DateTime now = DateTime.now();
    DateTimeUtils.setCurrentMillisFixed(now.getMillis());
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.RUNNING).setEndDateTime(now.minus(ReservationService.LOST_RESERVATION_GRACE_PERIOD).minusMillis(1)).create();
    when(reservationRepoMock.getByReservationIdWithPessimisticWriteLock(reservation.getReservationId())).thenReturn(reservation);

    subject.handleLostReservation(reservation.getReservationId());

    assertThat(reservation.getStatus(), is(ReservationStatus.SUCCEEDED));
  }

  @Test
  public void cancelAReservationAsAUserInGroupShouldChangeItsStatus() {
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.AUTO_START).create();
    RichUserDetails richUserDetails = new RichUserDetailsFactory()
        .addUserRole()
        .addUserGroup(reservation.getVirtualResourceGroup().get().getAdminGroup())
        .setDisplayname("Piet Puk")
        .create();
    Security.setUserDetails(richUserDetails);
    when(reservationToNbiMock.asyncTerminate(reservation.getId())).thenReturn(new AsyncResult<Long>(2L));

    subject.cancel(reservation, richUserDetails);

    assertThat(reservation.getStatus(), is(ReservationStatus.CANCELLING));
    assertThat(reservation.getCancelReason(), is("Cancelled by Piet Puk"));
    verify(reservationToNbiMock).asyncTerminate(reservation.getId());
  }

  @Test
  public void cancelAReservationAsAUserNotInGroupShouldNotChangeItsStatus() {
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.AUTO_START).create();
    RichUserDetails richUserDetails = new RichUserDetailsFactory().addUserRole().create();
    Security.setUserDetails(richUserDetails);

    Optional<Future<Long>> cancelFuture = subject.cancel(reservation, richUserDetails);

    assertFalse(cancelFuture.isPresent());

    assertThat(reservation.getStatus(), is(ReservationStatus.AUTO_START));
    verifyZeroInteractions(reservationRepoMock);
  }

  @Test
  public void cancelAReservationAsAManagerShouldNotInPrgShouldNotChangeItsStatus() {
    RichUserDetails richUserDetails = new RichUserDetailsFactory().addManagerRole().create();

    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.AUTO_START).create();

    Optional<Future<Long>> cancelFuture = subject.cancel(reservation, richUserDetails);

    assertFalse(cancelFuture.isPresent());
    assertThat(reservation.getStatus(), is(ReservationStatus.AUTO_START));

    verify(reservationRepoMock, never()).save(reservation);
    verify(nbiClientMock, never()).cancelReservation(any(String.class));
  }

  @Test
  public void cancelAReservationAsAManagerInSourcePortPrgShouldCallTerminate() throws InterruptedException,
      ExecutionException {
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.AUTO_START).create();

    RichUserDetails richUserDetails = new RichUserDetailsFactory().addManagerRole(
        reservation.getSourcePort().getUniPort().get().getPhysicalResourceGroup()).create();
    Security.setUserDetails(richUserDetails);

    when(
        reservationToNbiMock.asyncTerminate(reservation.getId())).thenReturn(new AsyncResult<Long>(2L));

    Optional<Future<Long>> cancelFuture = subject.cancel(reservation, richUserDetails);

    assertThat(cancelFuture.get().get(), is(2L));
    assertThat(reservation.getCancelReason(), is("Cancelled by Truus Visscher"));
    verify(reservationToNbiMock).asyncTerminate(reservation.getId());
  }

  @Test
  public void cancelAReservationAsAManagerInDestinationPortPrgShouldCallTerminate() throws InterruptedException,
      ExecutionException {
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.AUTO_START).create();
    reservation.getDestinationPort().getUniPort().get().getPhysicalResourceGroup().setAdminGroup("urn:different");

    RichUserDetails richUserDetails = new RichUserDetailsFactory().addManagerRole(
        reservation.getDestinationPort().getUniPort().get().getPhysicalResourceGroup()).create();
    Security.setUserDetails(richUserDetails);

    when(
        reservationToNbiMock.asyncTerminate(reservation.getId())).thenReturn(new AsyncResult<Long>(5L));

    Optional<Future<Long>> cancelFuture = subject.cancel(reservation, richUserDetails);

    assertThat(cancelFuture.get().get(), is(5L));
    verify(reservationToNbiMock).asyncTerminate(reservation.getId());
  }

  @Test
  public void cancelAReservationAsANocShouldCallTerminate() throws InterruptedException, ExecutionException {
    RichUserDetails richUserDetails = new RichUserDetailsFactory().addNocRole().create();

    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.AUTO_START).create();

    when(
        reservationToNbiMock.asyncTerminate(reservation.getId())).thenReturn(new AsyncResult<Long>(3L));

    Optional<Future<Long>> cancelFuture = subject.cancel(reservation, richUserDetails);

    assertThat(cancelFuture.get().get(), is(3L));
    verify(reservationToNbiMock).asyncTerminate(reservation.getId());
  }

  @Test
  public void cancelAReservationWithStatusFAILEDShouldNotChangeItsStatus() {
    RichUserDetails richUserDetails = new RichUserDetailsFactory().create();

    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.FAILED).create();

    Optional<Future<Long>> cancelFuture = subject.cancel(reservation, richUserDetails);

    assertFalse(cancelFuture.isPresent());
    assertThat(reservation.getStatus(), is(ReservationStatus.FAILED));

    verify(reservationRepoMock, never()).save(reservation);
  }

  @Test
  public void cancelAReservationWithStatusNOT_ACCEPTEDShouldNotChangeItsStatus() {
    RichUserDetails richUserDetails = new RichUserDetailsFactory().create();

    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.NOT_ACCEPTED).create();

    Optional<Future<Long>> cancelFuture = subject.cancel(reservation, richUserDetails);

    assertFalse(cancelFuture.isPresent());
    assertThat(reservation.getStatus(), is(ReservationStatus.NOT_ACCEPTED));

    verify(reservationRepoMock, never()).save(reservation);
  }

  @Test
  public void cancelAFailedReservationShouldNotChangeItsStatus() {
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.FAILED).create();
    subject.cancel(reservation, Security.getUserDetails());
    assertThat(reservation.getStatus(), is(ReservationStatus.FAILED));
    verifyZeroInteractions(reservationRepoMock);
  }

  @Test
  public void cancelANotExceptedReservationShouldNotChangeItsStatus() {
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.NOT_ACCEPTED).create();
    subject.cancel(reservation, Security.getUserDetails());
    assertThat(reservation.getStatus(), is(ReservationStatus.NOT_ACCEPTED));
    verifyZeroInteractions(reservationRepoMock);
  }

  @Test
  public void startTimeInThePastShouldBeCorrected() {
    DateTime startDateTime = DateTime.now().minusHours(2);
    Reservation reservation = new ReservationFactory().setStartDateTime(startDateTime).create();

    when(reservationRepoMock.save(reservation)).thenReturn(reservation);

    subject.create(reservation);

    assertTrue(reservation.getStartDateTime().isAfter(DateTime.now().minusSeconds(1)));
  }

  @Test
  public void startAndEndShouldBeInWholeMinutes() {
    DateTime startDateTime = DateTime.now().plusHours(2).withSecondOfMinute(1);
    DateTime endDateTime = DateTime.now().plusHours(2).withSecondOfMinute(1);

    Reservation reservation = new ReservationFactory().setStartDateTime(startDateTime).setEndDateTime(endDateTime)
        .create();

    when(reservationRepoMock.save(reservation)).thenReturn(reservation);

    subject.create(reservation);

    assertThat(reservation.getStartDateTime(), is(startDateTime.withSecondOfMinute(0).withMillisOfSecond(0)));
    assertThat(reservation.getEndDateTime(), is(endDateTime.withSecondOfMinute(0).withMillisOfSecond(0)));
  }

  @Test
  public void transformToFlattenedReservations() {
    List<Reservation> reservations = Lists.newArrayList();
    for (int i = 0; i < 10; i++) {
      reservations.add(new ReservationFactory().create());
    }

    final Collection<ReservationArchive> flattenedReservations = subject.transformToReservationArchives(reservations);

    assertThat(flattenedReservations, hasSize(10));
  }

  @Test
  public void cancel_should_be_allowed_when_user_created_reservation() {
    RichUserDetails userAllowed = new RichUserDetailsFactory().addUserRole().setUsername("name-id").create();
    RichUserDetails userDenied = new RichUserDetailsFactory().addUserRole().setUsername("some-other-name-id").create();

    Reservation reservation = new ReservationFactory()
      .setSourcePort(new ReservationEndPoint(new PhysicalPortFactory().createEnni(), Optional.<Integer>absent()))
      .setDestinationPort(new ReservationEndPoint(new PhysicalPortFactory().createEnni(), Optional.<Integer>absent()))
      .setUserCreated("name-id").create();

    ElementActionView cancelAllowed = subject.isCancelAllowed(reservation, userAllowed);
    assertThat(cancelAllowed.isAllowed(), is(true));

    ElementActionView cancelDenied = subject.isCancelAllowed(reservation, userDenied);
    assertThat(cancelDenied.isAllowed(), is(false));
  }

}