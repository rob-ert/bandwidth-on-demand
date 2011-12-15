package nl.surfnet.bod.service;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.repo.ReservationRepo;

@Service
@Transactional
public class ReservationService {

  @Autowired
  ReservationRepo reservationRepo;

  public void save(Reservation reservation) {
    reservationRepo.save(reservation);
  }

  public Reservation find(Long id) {
    return reservationRepo.findOne(id);
  }

  public Collection<Reservation> findEntries(int firstResult, int maxResults) {
    return reservationRepo.findAll(new PageRequest(firstResult / maxResults, maxResults)).getContent();
  }

  public long count() {
    return reservationRepo.count();
  }

  public Reservation update(Reservation reservation) {
    return reservationRepo.save(reservation);
  }

  public void delete(Reservation reservation) {
    reservationRepo.delete(reservation);
  }

}
