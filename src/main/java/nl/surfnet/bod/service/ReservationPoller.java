package nl.surfnet.bod.service;

import java.util.concurrent.ScheduledFuture;

import javax.annotation.PostConstruct;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is responisble for monitoring changes of a
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

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Value("${reservation.poller.cron.expression}")
  private String cronExpression;

  @Value("${reservation.poller.max.tries}")
  private int maxTries;

  @Autowired
  private ReservationService reservationService;

  private class ReservationStatusCheckTask implements Runnable {

    private long tries = 0;
    private Reservation reservation;

    public ReservationStatusCheckTask(final Reservation reservation) {
      this.reservation = reservation;
    }

    /**
     * Retrieves the actual {@link ReservationStatus} for the
     * {@link #reservation} and when it has changes, the new status will be
     * updated and persisted.
     * 
     * If the new reservationStatus is an
     * {@link ReservationService#isEndState(ReservationStatus)} the poller will
     * cancel itself and stops.
     * 
     * The number of times these logic is performed is limited by
     * {@link #maxTries}. Whenever this limit is reached the poller will also
     * cancel itself and stop polling.
     */
    public void run() {
      tries++;

      final ReservationStatus actualReservationStatus = reservationService.getStatus(reservation);

      log.debug(reservation.getReservationId() + " was [" + reservation.getStatus() + "] is now: "
          + actualReservationStatus);

      if (reservation.getStatus() != actualReservationStatus) {
        reservation.setStatus(actualReservationStatus);
        reservation = reservationService.update(reservation);
      }
      
      if (reservationService.isEndState(actualReservationStatus)) {
        schedule.cancel(false);
        log.info("Monitoring stops for reservation [" + reservation.getReservationId() + "] status is updated to: "
            + reservation.getStatus());
      }
      
      else {
        if (tries >= maxTries) {
          schedule.cancel(false);
          log.warn("Monitoring cancelled for reservation [" + reservation.getReservationId() + "]. MaxTries ["
              + maxTries + "] is reached");
        }
      }
    }
  }

  private TaskScheduler taskScheduler;
  private Trigger trigger;
  @SuppressWarnings("rawtypes")
  private ScheduledFuture schedule;

  @PostConstruct
  public void init() {
    init(cronExpression, maxTries);
  }

  void init(String cronExpression, int maxTries) {

    this.trigger = new CronTrigger(cronExpression);
    this.taskScheduler = new ConcurrentTaskScheduler();
    this.maxTries = maxTries;

    log.info("Init ReservationPoller, using cronExpression [" + cronExpression + "] and maxTries: " + maxTries);
  }

  /**
   * Starts a scheduler and updates the given {@link Reservation} when a change
   * in state is detected.
   * 
   * @param reservation
   *          The {@link Reservation} to monitor
   */
  public void monitorStatus(Reservation reservation) {
    schedule = taskScheduler.schedule(new ReservationStatusCheckTask(reservation), trigger);
    log.info("Start monitoring reservation [" + reservation.getReservationId() + "] for status change");
  }

  /**
   * @return true in case the poller is busy, false in case the poller is done.
   */
  public boolean isBusy() {
    return !schedule.isCancelled();
  }
}