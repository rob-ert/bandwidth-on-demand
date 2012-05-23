package nl.surfnet.bod.service;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.repo.ReservationRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReservationToNbi {

  @Autowired
  private NbiClient nbiClient;
  @Autowired
  private ReservationRepo reservationRepo;
  @Autowired
  private ReservationEventPublisher reservationEventPublisher;

  @Async
  public void submitNewReservation(Long reservationId) {
    final Reservation reservation = reservationRepo.findOne(reservationId);
    final ReservationStatus orgStatus = reservation.getStatus();

    Reservation reservationWithReservationId = nbiClient.createReservation(reservation);

    reservationRepo.save(reservationWithReservationId);

    publishStatusChanged(reservationWithReservationId, orgStatus);
  }

  private void publishStatusChanged(final Reservation reservation, final ReservationStatus originalStatus) {
    if (originalStatus == reservation.getStatus()) {
      return;
    }

    ReservationStatusChangeEvent createEvent = new ReservationStatusChangeEvent(originalStatus, reservation);
    reservationEventPublisher.notifyListeners(createEvent);
  }
}