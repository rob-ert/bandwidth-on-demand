package nl.surfnet.bod.nbi.onecontrol;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.annotation.Resource;
import javax.persistence.NoResultException;

import com.google.common.base.Optional;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("onecontrol")
/**
 * Continuously polls for reservations that need to be aligned
 */
public class ReservationsAligner implements SmartLifecycle {

  private final Logger log = LoggerFactory.getLogger(ReservationsAligner.class);
  private volatile boolean started = false;

  private static String POISON_PILL = "TOXIC";

  @Resource
  private NbiOneControlClient nbiOneControlClient;

  @Resource
  private ReservationService reservationService;

  private BlockingQueue<String> reservationIds = new ArrayBlockingQueue<>(1000);

  public void align() throws InterruptedException{
    for (;;) {
      try {
        String reservationId = reservationIds.take();
        if (POISON_PILL.equals(reservationId)) {
          break;
        }
        log.info("Picking up reservation {}", reservationId);
        Optional<ReservationStatus> reservationStatus = nbiOneControlClient.getReservationStatus(reservationId);
        if (reservationStatus.isPresent()) {

          log.info("Retrieved status of reservation {}, issuing update.", reservationId);

          try {
            reservationService.updateStatus(reservationId, reservationStatus.get());
          } catch (NoResultException e) {
            // apparently the reservation did not exist
            log.debug("Ignoring unknown reservation with id {}", reservationId);
          }
        }
      }
      catch (Exception e) {
        log.error("Exception occurred while updating a reservation", e);
      }
    }
  }

  @Override
  public void start() {
    started = true;
    log.info("Starting OneControl Reservations Aligner...");
    Thread alignerThread = new Thread(){
      @Override
      public void run() {
        try {
          align();
        } catch (InterruptedException e) {
          log.debug("OneControl Reservations Aligner exiting");
        } finally {
          started = false;
        }
      }
    };
    alignerThread.setName("Onecontrol Reservation Aligner");
    alignerThread.setDaemon(true);
    alignerThread.start();
  }

  @Override
  public void stop() {
    add(POISON_PILL);
  }

  @Override
  public boolean isRunning() {
    return started;
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable callback) {
    stop();
    callback.run();
  }

  @Override
  public int getPhase() { // start late, destroy early
    return Integer.MAX_VALUE;
  }

  /**
   *
   * @throws IllegalStateException when the backing queue is full
   */
  public void add(String reservationId){
    reservationIds.add(reservationId);
  }

}
