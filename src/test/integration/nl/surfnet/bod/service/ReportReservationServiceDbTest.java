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

import static nl.surfnet.bod.domain.ReservationStatus.AUTO_START;
import static nl.surfnet.bod.domain.ReservationStatus.REQUESTED;
import static nl.surfnet.bod.domain.ReservationStatus.RESERVED;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.AppComponents;
import nl.surfnet.bod.config.IntegrationDbConfiguration;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.UpdatedReservationStatus;
import nl.surfnet.bod.nbi.opendrac.NbiOpenDracOfflineClient;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.util.TestHelper;
import nl.surfnet.bod.util.TestHelper.TimeTraveller;
import nl.surfnet.bod.web.view.ReservationReportView;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = true)
@ContextConfiguration(classes = { AppComponents.class, IntegrationDbConfiguration.class })
@Transactional
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ActiveProfiles({"opendrac-offline", "idd-offline"})
public class ReportReservationServiceDbTest {
  private final static long AMOUNT_OF_RESERVATIONS = 9;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Resource private ReservationServiceDbTestHelper reservationHelper;
  @Resource private ReservationRepo reservationRepo;
  @Resource private NbiOpenDracOfflineClient nbiClient;
  @Resource private ReservationService subject;
  @Resource private ReportingService reportingService;

  private DateTime periodStart;
  private DateTime periodEnd;

  private Reservation reservationOnStartPeriodNSI;
  private Reservation reservationBeforeStartAndAfterEndPeriodNSI;
  private Reservation reservationBeforeStartAndOnEndPeriodGUI;
  private Reservation reservationAfterStartAndOnEndPeriodGUI;
  private Reservation reservationAfterStartAndOnEndPeriodNSI;
  private Reservation reservationAfterStartAndAfterEndPeriodNSI;

  private Reservation reservationBeforePeriodNSI;
  private Reservation reservationInPeriodGUI;
  private Reservation reservationAfterPeriodGUI;

  private final List<Long> reservationIds = new ArrayList<>();
  private final List<String> adminGroups = new ArrayList<>();

  private Interval reportInterval;

  private PhysicalResourceGroup prgSource;
  private PhysicalResourceGroup prgDestination;

  private boolean needsInit = true;

  @BeforeClass
  public static void init() {
    DatabaseTestHelper.clearIntegrationDatabaseSkipBaseData();
  }

  @BeforeTransaction
  public void setUp() {
    if (needsInit) {
      prgSource = reservationHelper.createAndPersistPhysicalResourceGroup(1L);
      prgDestination = reservationHelper.createAndPersistPhysicalResourceGroup(2L);
      needsInit = false;
    }

    periodStart = DateTime.now().plusDays(2).plusHours(1).withSecondOfMinute(0).withMillisOfSecond(0);
    periodEnd = periodStart.plusDays(1);
    reportInterval = new Interval(periodStart, periodEnd);

    logger.warn("Start of period [{}], end [{}]", periodStart, periodEnd);
    // Speed up setup time
    nbiClient.setShouldSleep(false);

    // Five (5) reservations in reporting period, 2 GUI and 3 NSI
    reservationAfterStartAndOnEndPeriodGUI = createAndSaveReservation(periodStart.plusHours(1), periodEnd, true, prgSource, prgDestination);
    assertThat(reservationAfterStartAndOnEndPeriodGUI.getStatus(), is(ReservationStatus.AUTO_START));
    reservationIds.add(reservationAfterStartAndOnEndPeriodGUI.getId());

    reservationInPeriodGUI = createAndSaveReservation(periodStart.plusHours(1), periodEnd.minusHours(1), true, prgSource, prgDestination);
    assertThat(reservationInPeriodGUI.getStatus(), is(ReservationStatus.AUTO_START));
    reservationIds.add(reservationInPeriodGUI.getId());

    reservationOnStartPeriodNSI = createAndSaveReservation(periodStart, periodEnd.plusDays(1), false, prgSource, prgDestination);
    assertThat(reservationOnStartPeriodNSI.getStatus(), is(ReservationStatus.RESERVED));
    reservationIds.add(reservationOnStartPeriodNSI.getId());

    reservationAfterStartAndOnEndPeriodNSI = createAndSaveReservation(periodStart.plusHours(1), periodEnd, false, prgSource, prgDestination);
    assertThat(reservationAfterStartAndOnEndPeriodNSI.getStatus(), is(ReservationStatus.RESERVED));
    reservationIds.add(reservationAfterStartAndOnEndPeriodNSI.getId());

    reservationAfterStartAndAfterEndPeriodNSI = createAndSaveReservation(periodStart.plusHours(1), periodEnd.plusDays(1), false, prgSource, prgDestination);
    assertThat(reservationAfterStartAndAfterEndPeriodNSI.getStatus(), is(ReservationStatus.RESERVED));
    reservationIds.add(reservationAfterStartAndAfterEndPeriodNSI.getId());

    // Two (2) reservations related to reporting period, 1 GUI and 1 NSI
    reservationBeforeStartAndOnEndPeriodGUI = createAndSaveReservation(periodStart.minusHours(1), periodEnd, true, prgSource, prgDestination);
    assertThat(reservationBeforeStartAndOnEndPeriodGUI.getStatus(), is(ReservationStatus.AUTO_START));
    reservationIds.add(reservationBeforeStartAndOnEndPeriodGUI.getId());

    reservationBeforeStartAndAfterEndPeriodNSI = createAndSaveReservation(periodStart.minusHours(1), periodEnd.plusDays(1), false, prgSource, prgDestination);
    assertThat(reservationBeforeStartAndAfterEndPeriodNSI.getStatus(), is(ReservationStatus.RESERVED));
    reservationIds.add(reservationBeforeStartAndAfterEndPeriodNSI.getId());

    // Two (2) reservations not related to reporting period, 1 GUI and 1 NSI
    reservationAfterPeriodGUI = createAndSaveReservation(periodEnd.plusHours(1), periodEnd.plusDays(1), true, prgSource, prgDestination);
    assertThat(reservationAfterPeriodGUI.getStatus(), is(ReservationStatus.AUTO_START));
    reservationIds.add(reservationAfterPeriodGUI.getId());

    reservationBeforePeriodNSI = createAndSaveReservation(periodStart.minusDays(1), periodStart.minusHours(1), false, prgSource, prgDestination);
    assertThat(reservationBeforePeriodNSI.getStatus(), is(ReservationStatus.RESERVED));
    reservationIds.add(reservationBeforePeriodNSI.getId());
  }

  @AfterTransaction
  public void teardown() {
    DatabaseTestHelper.clearIntegrationDatabaseSkipBaseData();
  }

  @Test
  public void checkSetup() {
    long amountOfReservations = reservationRepo.count();

    assertThat(amountOfReservations, is(AMOUNT_OF_RESERVATIONS));
    assertThat(reservationIds, hasSize((int) AMOUNT_OF_RESERVATIONS));
  }

  @Test
  public void shouldCountExistingStateInPeriodGUI() {
    long count = reportingService.countReservationsBetweenWhichHadStateInAdminGroups(periodStart, periodEnd, adminGroups, ReservationStatus.AUTO_START);

    assertThat(count, is(2L));
  }

  @Test
  public void shouldCountExistingStateInPeriodNSI() {
    long count = reportingService.countReservationsBetweenWhichHadStateInAdminGroups(periodStart, periodEnd, adminGroups, ReservationStatus.RESERVED);

    assertThat(count, is(5L));
  }

  @Test
  public void shouldCountExistingStateInPeriodGUIAndNSI() {
    long count = reportingService.countReservationsBetweenWhichHadStateInAdminGroups(periodStart, periodEnd, adminGroups, ReservationStatus.RESERVED, AUTO_START);

    assertThat(count, is(5L));
  }

  @Test
  public void shouldCountExistingStateBeforePeriodOnCornerGUI() {
    long count = reportingService.countReservationsBetweenWhichHadStateInAdminGroups(periodStart.minusDays(2), periodStart.minusHours(1), adminGroups, ReservationStatus.AUTO_START);

    assertThat(count, is(1L));
  }

  @Test
  public void shouldCountExistingStateBeforePeriodOnCornerNSI() {
    long count = reportingService.countReservationsBetweenWhichHadStateInAdminGroups(periodStart.minusDays(2), periodStart.minusHours(1), adminGroups, ReservationStatus.RESERVED);

    assertThat(count, is(3L));
  }

  @Test
  public void shouldCountExistingStateBeforePeriodOnCornerGUIAndNSI() {
    long count = reportingService.countReservationsBetweenWhichHadStateInAdminGroups(periodStart.minusDays(2), periodStart.minusHours(1), adminGroups, ReservationStatus.RESERVED, AUTO_START);

    assertThat(count, is(3L));
  }

  @Test
  public void shouldCountExistingStateBeforePeriodGUI() {
    long count = reportingService.countReservationsBetweenWhichHadStateInAdminGroups(periodStart.minusDays(2), periodStart.minusHours(2), adminGroups, ReservationStatus.AUTO_START);

    assertThat(count, is(0L));
  }

  @Test
  public void shouldCountExistingStateBeforePeriodNSI() {
    long count = reportingService.countReservationsBetweenWhichHadStateInAdminGroups(periodStart.minusDays(2), periodStart.minusHours(2), adminGroups, ReservationStatus.RESERVED);

    assertThat(count, is(1L));
  }

  @Test
  public void shouldCountExistingStateBeforePeriodGUIAndNSI() {
    long count = reportingService.countReservationsBetweenWhichHadStateInAdminGroups(periodStart.minusDays(2), periodStart.minusHours(2), adminGroups, ReservationStatus.RESERVED, AUTO_START);

    assertThat(count, is(1L));
  }

  @Test
  public void shouldCountExistingStateAfterPeriodGUI() {
    long count = reportingService.countReservationsBetweenWhichHadStateInAdminGroups(periodEnd.plusHours(1), periodEnd.plusDays(3), adminGroups, ReservationStatus.AUTO_START);

    assertThat(count, is(1L));
  }

  @Test
  public void shouldCountExistingStateAfterPeriodNSI() {
    long count = reportingService.countReservationsBetweenWhichHadStateInAdminGroups(periodEnd.plusHours(1), periodEnd.plusDays(3), adminGroups, ReservationStatus.RESERVED);

    assertThat(count, is(1L));
  }

  @Test
  public void shouldCountExistingStateAfterPeriodGUIAndNSI() {
    long count = reportingService.countReservationsBetweenWhichHadStateInAdminGroups(periodEnd.plusHours(1), periodEnd.plusDays(3), adminGroups, ReservationStatus.RESERVED, AUTO_START);

    assertThat(count, is(1L));
  }

  @Test
  public void shouldCountExsitingTransitionInPeriodNSI() {
    long count = reportingService.countReservationsWhichHadStateTransitionBetweenInAdminGroups(periodStart, periodEnd, REQUESTED, RESERVED, adminGroups);

    assertThat(count, is(5L));
  }

  @Test
  public void shouldCountNonExsitingTransitionInPeriod() {
    long count = reportingService.countReservationsWhichHadStateTransitionBetweenInAdminGroups(periodStart, periodEnd, REQUESTED, ReservationStatus.NOT_ACCEPTED, adminGroups);

    assertThat(count, is(0L));
  }

  @Test
  public void shouldFindActiveReservationsWithState() {
    long count = reportingService.countActiveReservationsBetweenWithState(reservationIds, periodStart, periodEnd, AUTO_START, adminGroups);

    assertThat(count, is(3L));

    subject.updateStatus(reservationInPeriodGUI.getReservationId(), UpdatedReservationStatus.forNewStatus(ReservationStatus.SUCCEEDED));
    count = reportingService.countActiveReservationsBetweenWithState(reservationIds, periodStart, periodEnd, AUTO_START, adminGroups);

    assertThat("Should count one less because of state change", count, is(2L));
  }

  @Test
  public void shouldNotFindActiveReservationsBecauseOfState() {
    long count = reportingService.countActiveReservationsBetweenWithState(reservationIds, periodStart, periodEnd, REQUESTED, adminGroups);

    assertThat(count, is(0L));
  }

  @Test
  public void shouldNotFindActiveReservationsBecauseBeforePeriod() {
    long count = reportingService.countActiveReservationsBetweenWithState(reservationIds, periodStart.minusDays(3), periodStart.minusDays(2), AUTO_START, adminGroups);

    assertThat(count, is(0L));
  }

  @Test
  public void shouldCountCreatesThroughNSI() {
    long count = reportingService.countReservationsCreatedThroughChannelNSIInAdminGroups(periodStart, periodEnd, adminGroups);

    assertThat(count, is(3L));
  }

  @Test
  public void shouldCountCancelsThroughNSI() {
    long count = reportingService.countReservationsCancelledThroughChannelNSIInAdminGroups(periodStart, periodEnd, adminGroups);

    assertThat(count, is(0L));
  }

  @Test
  public void shouldCountCreatesThroughGUI() {
    long count = reportingService.countReservationsCreatedThroughChannelGUIInAdminGroups(periodStart, periodEnd, adminGroups);

    assertThat(count, is(2L));
  }

  @Test
  public void shouldCountCancelsThroughGUI() {
    long count = reportingService.countReservationsCancelledThroughChannelGUInAdminGroups(periodStart, periodEnd, adminGroups);

    assertThat(count, is(0L));
  }

  @Test
  public void shouldFindReservationIdsBeforeInAdminGroupsWithState() {
    List<Long> reservationIds = reportingService.findReservationIdsBeforeOrOnInAdminGroupsWithState(periodStart, adminGroups, ReservationStatus.TRANSITION_STATES_AS_ARRAY);

    assertThat(reservationIds, hasSize(4));
    assertThat(reservationIds, hasItems(reservationOnStartPeriodNSI.getId(), reservationBeforeStartAndOnEndPeriodGUI .getId(), reservationBeforeStartAndAfterEndPeriodNSI.getId(), reservationBeforePeriodNSI.getId()));
  }

  @Test
  public void shouldFindReservationIdsWichHadStateBetweenNSI() {
    List<Long> reservationIds = reportingService.findReservationIdsInAdminGroupsWhichHadStateBetween(periodStart, periodEnd, adminGroups, ReservationStatus.RESERVED);

    assertThat(reservationIds, hasItems(reservationAfterStartAndOnEndPeriodGUI.getId(), reservationInPeriodGUI.getId(), reservationOnStartPeriodNSI.getId(), reservationAfterStartAndOnEndPeriodNSI.getId(), reservationAfterStartAndAfterEndPeriodNSI.getId()));
  }

  @Test
  public void shouldFindReservationIdsWichHadStateBetweenGUI() {
    List<Long> reservationIds = reportingService.findReservationIdsInAdminGroupsWhichHadStateBetween(periodStart, periodEnd, adminGroups, ReservationStatus.AUTO_START);

    assertThat(reservationIds, hasSize(2));
    assertThat(reservationIds, hasItems(reservationInPeriodGUI.getId(), reservationAfterStartAndOnEndPeriodGUI.getId()));
  }

  @Test
  public void shouldFindReservationIdsWichHadStateBetweenGUIAndNSI() {
    List<Long> reservationIds = reportingService.findReservationIdsInAdminGroupsWhichHadStateBetween(periodStart, periodEnd, adminGroups, ReservationStatus.AUTO_START, RESERVED);

    assertThat(reservationIds, hasItems(reservationInPeriodGUI.getId(), reservationAfterStartAndOnEndPeriodGUI.getId(), reservationOnStartPeriodNSI.getId(), reservationAfterStartAndOnEndPeriodNSI.getId(), reservationAfterStartAndAfterEndPeriodNSI.getId()));
  }

  @Test
  public void shouldFindReservationidsWichHadStateBeforePeriodGUIAndNSI() {
    List<Long> reservationIds = reportingService.findReservationIdsInAdminGroupsWhichHadStateBetween(periodStart.minusDays(2), periodStart.minusMinutes(1), adminGroups, ReservationStatus.AUTO_START, RESERVED);

    assertThat(reservationIds, hasSize(3));
    assertThat(reservationIds, hasItems(reservationBeforePeriodNSI.getId(), reservationBeforeStartAndAfterEndPeriodNSI .getId(), reservationBeforeStartAndOnEndPeriodGUI.getId()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotSearchOnRequested() {
    reportingService.findReservationIdsInAdminGroupsWhichHadStateBetween(periodStart, periodEnd, adminGroups,
        ReservationStatus.REQUESTED);
  }

  @Test
  public void shouldFindProtectionTypesInIds() {
    reservationAfterPeriodGUI.setProtectionType(ProtectionType.UNPROTECTED);
    reservationRepo.saveAndFlush(reservationAfterPeriodGUI);

    long count = reportingService.countReservationsForIdsWithProtectionType(reservationIds, ProtectionType.PROTECTED);
    assertThat(count, is(8L));

    count = reportingService.countReservationsForIdsWithProtectionType(reservationIds, ProtectionType.UNPROTECTED);
    assertThat(count, is(1L));

    count = reportingService.countReservationsForIdsWithProtectionType(reservationIds, ProtectionType.REDUNDANT);
    assertThat(count, is(0L));
  }

  @Test
  public void shouldFindSuccesfulReservationRequests() {
    List<Long> reservationIds = reportingService.findSuccessfulReservationRequestsInAdminGroups(periodStart, periodEnd, adminGroups);

    assertThat(reservationIds, hasItems(reservationOnStartPeriodNSI.getId(), reservationInPeriodGUI.getId(),
        reservationAfterStartAndOnEndPeriodGUI.getId(), reservationAfterStartAndOnEndPeriodNSI.getId(), reservationAfterStartAndAfterEndPeriodNSI.getId()));
  }

  @Test
  public void shouldFindReservationIdsStartBeforeAndEndInOrAfter() {
    List<Long> reservationIds = reportingService.findReservationIdsStartBeforeAndEndInOrAfter(periodStart, periodEnd);
    assertThat(reservationIds, hasItems(reservationAfterStartAndOnEndPeriodGUI.getId(), reservationInPeriodGUI.getId(),
        reservationOnStartPeriodNSI.getId(), reservationAfterStartAndOnEndPeriodNSI.getId(), reservationAfterStartAndAfterEndPeriodNSI.getId(),
        reservationBeforeStartAndOnEndPeriodGUI.getId(), reservationBeforeStartAndAfterEndPeriodNSI.getId()));
  }

  @Test
  public void shouldFindReservationsIdsShiftPeriodEarlierCornerCase() {
    List<Long> reservationIds = reportingService.findReservationIdsStartBeforeAndEndInOrAfter(periodStart.minusDays(1), periodStart.minusHours(1));
    assertThat(reservationIds, hasSize(3));
    assertThat(reservationIds, hasItems(reservationBeforePeriodNSI.getId(), reservationBeforeStartAndOnEndPeriodGUI
        .getId(), reservationBeforeStartAndAfterEndPeriodNSI.getId()));
  }

  @Test
  public void shouldFindReservationsIdsShiftPeriodEarlier() {
    List<Long> reservationIds = reportingService.findReservationIdsStartBeforeAndEndInOrAfter(periodStart.minusDays(1), periodStart.minusHours(1).minusMinutes(1));
    assertThat(reservationIds, hasSize(1));
    assertThat(reservationIds, hasItems(reservationBeforePeriodNSI.getId()));
  }

  @Test
  public void shouldVerifyNoActiveReservation() {
    ReservationReportView reservationReport = new ReservationReportView(periodStart, periodEnd);
    reportingService.determineActiveRunningReservations(reservationReport, adminGroups);

    assertThat(reservationReport.getAmountRunningReservationsSucceeded(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsFailed(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsStillScheduled(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsStillRunning(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsNeverProvisioned(), is(0L));

    assertThat(reservationReport.getTotalActiveReservations(), is(0L));
  }

  @Test
  public void shouldVerifyActiveReservationsSucceeded() {
    updateStatus(periodStart, reservationBeforeStartAndOnEndPeriodGUI, ReservationStatus.RUNNING);
    updateStatus(periodEnd, reservationBeforeStartAndOnEndPeriodGUI, ReservationStatus.SUCCEEDED);

    ReservationReportView reservationReport = new ReservationReportView(periodStart, periodEnd);
    reportingService.determineActiveRunningReservations(reservationReport, adminGroups);

    assertThat(reservationReport.getAmountRunningReservationsSucceeded(), is(1L));
    assertThat(reservationReport.getAmountRunningReservationsFailed(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsStillScheduled(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsStillRunning(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsNeverProvisioned(), is(0L));

    assertThat(reservationReport.getTotalActiveReservations(), is(1L));
  }

  @Test
  public void shouldVerifyActiveReservationsStillScheduled() {
    updateStatus(periodStart, reservationAfterStartAndAfterEndPeriodNSI, ReservationStatus.SCHEDULED);

    ReservationReportView reservationReport = new ReservationReportView(periodStart, periodEnd);
    reportingService.determineActiveRunningReservations(reservationReport, adminGroups);

    assertThat(reservationReport.getAmountRunningReservationsSucceeded(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsFailed(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsStillScheduled(), is(1L));
    assertThat(reservationReport.getAmountRunningReservationsStillRunning(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsNeverProvisioned(), is(0L));

    assertThat(reservationReport.getTotalActiveReservations(), is(1L));
  }

  @Test
  public void shouldVerifyActiveReservationsStillRunning() {
    updateStatus(periodStart, reservationBeforeStartAndOnEndPeriodGUI, ReservationStatus.RUNNING);

    ReservationReportView reservationReport = new ReservationReportView(periodStart, periodEnd);
    reportingService.determineActiveRunningReservations(reservationReport, adminGroups);

    assertThat(reservationReport.getAmountRunningReservationsSucceeded(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsFailed(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsStillScheduled(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsStillRunning(), is(1L));
    assertThat(reservationReport.getAmountRunningReservationsNeverProvisioned(), is(0L));

    assertThat(reservationReport.getTotalActiveReservations(), is(1L));
  }

  @Test
  public void shouldVerifyActiveReservationsFailed() {
    reservationBeforeStartAndOnEndPeriodGUI = updateStatus(periodStart, reservationBeforeStartAndOnEndPeriodGUI, ReservationStatus.RUNNING);
    reservationBeforeStartAndOnEndPeriodGUI = updateStatus(periodStart.plusSeconds(1), reservationBeforeStartAndOnEndPeriodGUI, ReservationStatus.FAILED);

    ReservationReportView reservationReport = new ReservationReportView(periodStart, periodEnd);
    reportingService.determineActiveRunningReservations(reservationReport, adminGroups);

    assertThat(reservationReport.getAmountRunningReservationsSucceeded(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsFailed(), is(1L));
    assertThat(reservationReport.getAmountRunningReservationsStillScheduled(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsStillRunning(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsNeverProvisioned(), is(0L));

    assertThat(reservationReport.getTotalActiveReservations(), is(1L));
  }

  @Test
  public void shouldVerifyActiveReservationsNeverProvisioned() {
    reservationAfterStartAndOnEndPeriodNSI = updateStatus(reservationAfterStartAndOnEndPeriodNSI.getEndDateTime().get(), reservationAfterStartAndOnEndPeriodNSI, ReservationStatus.PASSED_END_TIME);
    assertThat(reservationAfterStartAndOnEndPeriodNSI.getStatus(), is(ReservationStatus.PASSED_END_TIME));

    ReservationReportView reservationReport = new ReservationReportView(periodStart, periodEnd);
    reportingService.determineActiveRunningReservations(reservationReport, adminGroups);

    assertThat(reservationReport.getAmountRunningReservationsSucceeded(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsFailed(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsStillScheduled(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsStillRunning(), is(0L));
    assertThat(reservationReport.getAmountRunningReservationsNeverProvisioned(), is(1L));

    assertThat(reservationReport.getTotalActiveReservations(), is(1L));
  }

  @Test
  public void shouldVerifyNocReport() {
    ReservationReportView reportView = reportingService.determineReportForNoc(reportInterval);

    assertThat(reportView.getAmountRequestsCancelFailed(), is(0L));
    assertThat(reportView.getAmountRequestsCancelSucceeded(), is(0L));
    assertThat(reportView.getAmountRequestsCreatedFailed(), is(0L));
    assertThat(reportView.getAmountRequestsCreatedSucceeded(), is(5L));
    assertThat(reportView.getAmountRequestsModifiedFailed(), is(0L));
    assertThat(reportView.getAmountRequestsModifiedSucceeded(), is(0L));
    assertThat(reportView.getAmountRequestsThroughGUI(), is(2L));
    assertThat(reportView.getAmountRequestsThroughNSI(), is(3L));
    assertThat(reportView.getAmountReservationsProtected(), is(8L));
    assertThat(reportView.getAmountReservationsUnprotected(), is(0L));
    assertThat(reportView.getAmountReservationsRedundant(), is(0L));

    assertThat(reportView.getAmountRunningReservationsSucceeded(), is(0L));
    assertThat(reportView.getAmountRunningReservationsFailed(), is(0L));
    assertThat(reportView.getAmountRunningReservationsStillRunning(), is(0L));
    assertThat(reportView.getAmountRunningReservationsStillScheduled(), is(0L));
    assertThat(reportView.getAmountRunningReservationsNeverProvisioned(), is(0L));

    assertThat(reportView.getTotalAmountRequestsCancelled(), is(0L));
    assertThat(reportView.getTotalAmountRequestsCreated(), is(5L));
    assertThat(reportView.getTotalAmountRequestsModified(), is(0L));
    assertThat(reportView.getTotalRequests(), is(5L));
    assertThat(reportView.getTotalReservations(), is(8L));
    assertThat(reportView.getTotalActiveReservations(), is(0L));
  }

  private Reservation createAndSaveReservation(
      final DateTime start, final DateTime end, final boolean autoProvision, final PhysicalResourceGroup sourceGroup, final PhysicalResourceGroup destinationGroup) {

    return TestHelper.<Reservation> runAtSpecificTime(start, new TimeTraveller<Reservation>() {
      @Override
      public Reservation apply() throws Exception {
        Reservation reservation = reservationHelper.createReservation(start, end, ReservationStatus.REQUESTED, sourceGroup, destinationGroup);
        reservation = reservationHelper.createThroughService(reservation, autoProvision);

        // No auto provision indicates NSI reservation, so add connection to it
        if (!autoProvision) {
          reservation = reservationHelper.addConnectionToReservation(reservation);
        }

        return reservation;
      }
    });
  }

  private Reservation updateStatus(final DateTime dateTime, final Reservation reservation, final ReservationStatus status) {
    return TestHelper.<Reservation> runAtSpecificTime(dateTime, new TimeTraveller<Reservation>() {
      @Override
      public Reservation apply() throws Exception {
        return reservationHelper.updateStatusAndCommit(reservation, status);
      }
    });
  }

}