package nl.surfnet.bod.service;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.web.push.EndPoints;
import nl.surfnet.bod.web.push.Event;
import nl.surfnet.bod.web.push.Events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is responsible for monitoring changes of a
 * {@link Reservation#getStatus()}. A scheduler is started upon the call to
 * {@link #monitorStatus(Reservation)}, whenever a state change is detected the
 * new state will be updated in the specific {@link Reservation} object and will
 * be persisted. The scheduler will be cancelled afterwards.
 * 
 * @author Franky
 * 
 */
@Component
@Transactional
public class ReservationPoller {

  @Value("${reservation.poll.interval.seconds}")
  private int pollIntervalInSeconds;

  @Value("${reservation.poll.max.tries}")
  private int maxTries;

  private final Logger log = LoggerFactory.getLogger(getClass());

  private TaskScheduler taskScheduler;
  private Trigger trigger;

  @Autowired
  private ReservationService reservationService;

  @Autowired
  private EndPoints endPoints;

  @PostConstruct
  public void init() {
    init(pollIntervalInSeconds, maxTries);
  }

  void init(int pollerIntervalInSeconds, int maxAmountOfTries) {
    maxTries = maxAmountOfTries;
    pollIntervalInSeconds = pollerIntervalInSeconds;

    this.trigger = new PeriodicTrigger(pollIntervalInSeconds, TimeUnit.SECONDS);
    this.taskScheduler = new ConcurrentTaskScheduler();

    log.info("Init ReservationPoller, polling every [{}]  seconds with maxTries: {} ", pollIntervalInSeconds, maxTries);

    // Just force logging
    isMonitoringDisabled();

  }

  /**
   * Starts a scheduler and updates the given {@link Reservation}s when a change
   * in state is detected.
   * 
   * @param reservation
   *          The {@link Reservation} to monitor
   * @return
   */
  public void monitorStatus(Reservation... reservations) {
    monitorStatus(null, reservations);
  }

  /**
   * Starts a scheduler and updates the given {@link Reservation}s when a change
   * in state is detected. Will only schedule a new task if there is currently
   * no task for the reservation running.
   * 
   * @param stopStatus
   *          {@link ReservationStatus} The monitoring will stop when this state
   *          is reached or when an
   *          {@link ReservationStatus#isEndState(ReservationStatus)} is
   *          reached.
   * 
   * @param reservation
   *          The {@link Reservation} to monitor
   * @return
   */
  public synchronized void monitorStatus(ReservationStatus stopStatus, Reservation... reservations) {

    for (Reservation reservation : reservations) {
      if (!reservation.isMonitored()) {
        ReservationStatusCheckTask checkTask = new ReservationStatusCheckTask(stopStatus, reservation);

        ScheduledFuture<?> schedule = taskScheduler.schedule(checkTask, trigger);

        checkTask.setSchedule(schedule);
      }
      else {
        log.debug("Skipping task, is already scheduled for reservation {}", reservation);
      }
    }
  }

  /**
   * Starts a scheduler at a specific time and updates the given
   * {@link Reservation}s when a change in state is detected. Will only schedule
   * a new task if there is currently no task for the reservation running.
   * 
   * @param startTime
   *          The time the trigger should start
   * 
   * @param reservation
   *          The {@link Reservation} to monitor
   */
  public synchronized void monitorStatusWIthSpecificStart(Date startTime, Reservation reservation) {

    if (!reservation.isMonitored()) {
      ReservationStatusCheckTask checkTask = new ReservationStatusCheckTask(reservation);

      ScheduledFuture<?> schedule = taskScheduler.scheduleAtFixedRate(checkTask, startTime,
          (pollIntervalInSeconds * 1000L));

      checkTask.setSchedule(schedule);
    }
    else {
      log.debug("Skipping task, is already scheduled for reservation {}", reservation);
    }
  }

  /**
   * Checks if monitoring is disabled.
   * 
   * @return true is no monitoring is allowed, false otherwise.
   */
  boolean isMonitoringDisabled() {
    boolean disabled = maxTries < 0;

    if (disabled) {
      log.warn("Monitoring of reservations is disabled, because maxTries is negative");
    }

    return disabled;
  }

  private class ReservationStatusCheckTask implements Runnable {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private long tries = 0;
    private Reservation reservation;
    private ReservationStatus stopStatus;

    @SuppressWarnings("rawtypes")
    private volatile ScheduledFuture schedule;

    /**
     * Monitors the given {@link Reservation} until an endState is reached
     * 
     * @param reservation
     *          The Reservation to monitor
     */
    public ReservationStatusCheckTask(final Reservation reservation) {
      this(null, reservation);
    }

    /**
     * Monitors the given {@link Reservation} until the specified stopStatus is
     * reached or an endState;
     * 
     * @param stopStatus
     *          When this state is reached, the monitoring will stop
     * @param reservation
     *          The Reservation to monitor
     */
    public ReservationStatusCheckTask(ReservationStatus stopStatus, final Reservation reservation) {
      this.stopStatus = stopStatus;
      this.reservation = reservation;

      reservation.setMonitored(true);
    }

    public synchronized void setSchedule(ScheduledFuture<?> schedule) {
      this.schedule = schedule;
    }

    /**
     * Retrieves the actual {@link ReservationStatus} for the
     * {@link #reservation} and when it has changes, the new status will be
     * updated and persisted.
     * 
     * If the new reservationStatus is an
     * {@link ReservationService#isEndState(ReservationStatus)} or when the
     * given state is reached, the poller will cancel itself and stops.
     * 
     * The number of times these logic is performed is limited by
     * {@link #maxTries}. Whenever this limit is reached the poller will also
     * cancel itself and stop polling.
     * 
     * If {@link #maxTries} is negative, monitoring will be disabled.
     */
    public void run() {
      log.info("Start monitoring reservation [{}] for state change", reservation.getReservationId());

      if (isMonitoringDisabled()) {
        reservation.setMonitored(false);
        return;
      }
      tries++;

      final ReservationStatus oldReservationStatus = reservation.getStatus();
      final ReservationStatus actualReservationStatus = reservationService.getStatus(reservation);

      log.debug("Reservation [{}] was [{}] is now: {} ", new String[] { reservation.getReservationId(),
          reservation.getStatus().name(), actualReservationStatus.name() });

      if (reservation.getStatus() != actualReservationStatus) {
        reservation.setStatus(actualReservationStatus);
        reservation = reservationService.update(reservation);
      }

      if ((actualReservationStatus == stopStatus) || reservationService.isEndState(actualReservationStatus)) {
        stopPolling(oldReservationStatus);
        log.info("Monitoring stops for reservation [{}] status is updated to: {} ", reservation.getReservationId(),
            reservation.getStatus());
      }
      else {
        if (tries >= maxTries) {
          stopPolling(oldReservationStatus);
          log.warn("Monitoring cancelled for reservation [{}]. MaxTries [{}] is reached",
              reservation.getReservationId(), maxTries);
        }
      }
    }

    /**
     * Cancels the schedule, since this task runs in a separate thread it is not
     * guaranteed that the schedule is already set when we need it. Therefore
     * wait until it is available. This will only block this thread at the end
     * of its flow. The flag on the {@link Reservation#isMonitored()}
     * will be set to false, since we are done. Raises an event
     * {@link Events#createReservationStatusChangedEvent(Reservation, ReservationStatus)}
     */
    private void stopPolling(ReservationStatus reservationStatus) {
      while (schedule == null) {
        log.debug("Waiting for schedule to be available");
      }

      schedule.cancel(false);
      reservation.setMonitored(false);

      Event reservationStatusChangedEvent = Events.createReservationStatusChangedEvent(reservation, reservationStatus);
      endPoints.broadcast(reservationStatusChangedEvent);
    }
  }
}