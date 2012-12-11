package nl.surfnet.bod.service;

import static nl.surfnet.bod.domain.ReservationStatus.RUNNING;
import static nl.surfnet.bod.domain.ReservationStatus.SCHEDULED;
import static nl.surfnet.bod.domain.ReservationStatus.TRANSITION_STATES;
import static nl.surfnet.bod.domain.ReservationStatus.TRANSITION_STATES_AS_ARRAY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.web.view.ReportIntervalView;
import nl.surfnet.bod.web.view.ReservationReportView;

import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;

@Service
public class ReportingService {
  
  @Resource
  private ReservationService reservationService;
  
  @Resource
  private LogEventService logEventService;
  
  public ReservationReportView determineReport(ReportIntervalView selectedInterval, Collection<String> adminGroups) {
    ReservationReportView reservationReport = new ReservationReportView(selectedInterval.getInterval().getStart(),
        selectedInterval.getInterval().getEnd());

    determineReservationRequestsForGroups(reservationReport, adminGroups);
    determineReservationsInAdminGroupsForProtectionType(reservationReport, adminGroups);
    determineActiveRunningReservations(reservationReport, adminGroups);

    return reservationReport;
  }

  private void determineReservationRequestsForGroups(ReservationReportView reservationReport,
      Collection<String> adminGroups) {
    final DateTime start = reservationReport.getPeriodStart();
    final DateTime end = reservationReport.getPeriodEnd();

    // ReservationRequests
    reservationReport.setAmountRequestsCreatedSucceeded(reservationService
        .countSuccesfullReservationRequestsInAdminGroups(start, end, adminGroups));
    reservationReport.setAmountRequestsCreatedFailed(reservationService.countFailedReservationRequestsInAdminGroups(
        start, end, adminGroups));

    // No modify requests yet, init on zero
    reservationReport.setAmountRequestsModifiedSucceeded(0l);
    reservationReport.setAmountRequestsModifiedFailed(0l);

    reservationReport.setAmountRequestsCancelSucceeded(reservationService
        .countReservationsWithEndStateBetweenInAdminGroups(start, end, adminGroups, ReservationStatus.CANCELLED));

    reservationReport.setAmountRequestsCancelFailed(reservationService
        .countReservationsWithEndStateBetweenInAdminGroups(start, end, adminGroups,
            ReservationStatus.CANCEL_FAILED));

    // Actual Reservations by channel
    reservationReport.setAmountRequestsThroughGUI(reservationService
        .countReservationsCreatedThroughChannelGUIInAdminGroups(start, end, adminGroups));

    reservationReport.setAmountRequestsThroughNSI(reservationReport.getTotalRequests()
        - reservationReport.getAmountRequestsThroughGUI());
  }

  @VisibleForTesting
  void determineReservationsInAdminGroupsForProtectionType(ReservationReportView reservationReport,
      Collection<String> adminGroups) {
    final DateTime start = reservationReport.getPeriodStart();
    final DateTime end = reservationReport.getPeriodEnd();

    List<Long> reservationIds = new ArrayList<>();

    for (Long id : reservationService.findReservationIdsBeforeInAdminGroupsWithState(start, adminGroups,
        TRANSITION_STATES_AS_ARRAY)) {
      LogEvent logEvent = logEventService.findLatestStateChangeForReservationIdBeforeInAdminGroups(id, start,
          adminGroups);
      if (TRANSITION_STATES.contains(logEvent.getNewReservationStatus())) {
        reservationIds.add(id);
      }
    }

    reservationIds.addAll(reservationService.findSuccessfullReservationRequestsInAdminGroups(start, end, adminGroups));

    reservationReport.setAmountReservationsProtected(reservationService
        .countReservationsForIdsWithProtectionTypeAndCreatedBefore(reservationIds, ProtectionType.PROTECTED));

    reservationReport.setAmountReservationsUnprotected(reservationService
        .countReservationsForIdsWithProtectionTypeAndCreatedBefore(reservationIds, ProtectionType.UNPROTECTED));

    reservationReport.setAmountReservationsRedundant(reservationService
        .countReservationsForIdsWithProtectionTypeAndCreatedBefore(reservationIds, ProtectionType.REDUNDANT));
  }

  @VisibleForTesting
  void determineActiveRunningReservations(ReservationReportView reservationReport, Collection<String> adminGroups) {
    final DateTime start = reservationReport.getPeriodStart();
    final DateTime end = reservationReport.getPeriodEnd();

    reservationReport.setAmountRunningReservationsSucceeded(reservationService
        .countRunningReservationsInAdminGroupsSucceeded(start, end, adminGroups));

    reservationReport.setAmountRunningReservationsFailed(reservationService
        .countRunningReservationsInAdminGroupsFailed(start, end, adminGroups));

    reservationReport.setAmountRunningReservationsStillRunning(reservationService
        .countActiveReservationsBetweenWithState(start, end, RUNNING, adminGroups));

    reservationReport.setAmounRunningReservationsStillScheduled(reservationService
        .countActiveReservationsBetweenWithState(start, end, SCHEDULED, adminGroups));

    reservationReport.setAmountRunningReservationsNeverProvisioned(reservationService
        .countActiveReservationsBetweenWithState(start, end, ReservationStatus.TIMED_OUT, adminGroups));
  }

}
