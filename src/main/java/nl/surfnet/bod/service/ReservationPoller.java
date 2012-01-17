package nl.surfnet.bod.service;

import java.util.concurrent.ScheduledFuture;

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

/**
 * This class is responisble for monitoring changes of a
 * {@link Reservation#getStatus()}. A scheduler is started upon the call to
 * {@link #getStatus(Reservation)}, whenever a state change is detected the new
 * state will be updated in the specific {@link Reservation} object and will be
 * persisted. The scheduler will be cancelled afterwards.
 * 
 * @author Franky
 * 
 */
@Component
public class ReservationPoller {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Value("${reservation.poller.cron.expression}")
  private String reservationPollerCronExpression = "* * * * * *";

  @Value("${reservation.poller.max.tries}")
  private long maxTries = 3;

  @Autowired
  private ReservationService reservationService;

  private class ReservationStatusCheckTask implements Runnable {

    private final Reservation reservation;
    private long tries = 0;

    public ReservationStatusCheckTask(final Reservation reservation) {
      this.reservation = reservation;
    }

    public void run() {
      tries++;

      final ReservationStatus currentReservationStatus = reservationService.getStatus(reservation);

      log.debug(reservation.getReservationId() + " was [" + reservation.getStatus() + "] is now: "
          + currentReservationStatus);

      if (reservation.getStatus() != currentReservationStatus) {

        reservation.setStatus(currentReservationStatus);
        reservationService.update(reservation);

        schedule.cancel(false);
        log.info("Reservation [" + reservation.getReservationId() + "] status is updated to: "
            + reservation.getStatus());
      }
      else {
        if (tries >= maxTries) {
          schedule.cancel(false);
          log.warn("Monitoring of reservation [" + reservation.getReservationId() + "] cancelled. MaxTries ["
              + maxTries + " is reached");
        }
      }

    }
  }

  final private TaskScheduler taskScheduler;
  final private Trigger trigger;
  private ScheduledFuture schedule;

  public ReservationPoller() {
    this.taskScheduler = new ConcurrentTaskScheduler();
    // Using interval from properties
    this.trigger = new CronTrigger(reservationPollerCronExpression);
  }

  /**
   * Starts a scheduler and updates the given {@link Reservation} when a change
   * in state is detected.
   * 
   * @param reservation
   *          The {@link Reservation} to monitor
   */
  public void getStatus(Reservation reservation) {
    schedule = taskScheduler.schedule(new ReservationStatusCheckTask(reservation), trigger);
  }

  /**
   * @return true in case the poller is busy, false in case the poller is done.
   */
  public boolean isBusy() {
    return !schedule.isCancelled();
  }
}