package nl.surfnet.bod.service;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.repo.InstituteRepo;
import nl.surfnet.bod.repo.PhysicalPortRepo;
import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.repo.VirtualPortRepo;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
import nl.surfnet.bod.support.ReservationFactory;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import static org.junit.Assert.fail;

@Service
public class ReservationServiceDbTestHelper {

  @Resource
  private ReservationService reservationService;
  @Resource
  private ReservationRepo reservationRepo;
  @Resource
  private VirtualResourceGroupRepo virtualResourceGroupRepo;
  @Resource
  private VirtualPortRepo virtualPortRepo;
  @Resource
  private PhysicalPortRepo physicalPortRepo;
  @Resource
  private InstituteRepo instituteRepo;
  @Resource
  private PhysicalResourceGroupRepo physicalResourceGroupRepo;
  @Resource
  private EntityManagerFactory entityManagerFactory;

  Reservation createReservation(DateTime startDateTime, DateTime endDateTime, ReservationStatus status) {
    Reservation reservation = new ReservationFactory().setStartDateTime(startDateTime).setEndDateTime(endDateTime)
        .setStatus(status).create();

    // Determine institute to use
    final Institute sourceInstitute = findInstituteToPreventUniqueKeyViolationInPhysicalResourceGroup();
    reservation.getSourcePort().getPhysicalResourceGroup().setInstitute(sourceInstitute);
    persistPhysicalResourceGroup(reservation.getSourcePort().getPhysicalResourceGroup());

    final Institute destinationInstitute = findInstituteToPreventUniqueKeyViolationInPhysicalResourceGroup();
    reservation.getDestinationPort().getPhysicalResourceGroup().setInstitute(destinationInstitute);
    persistPhysicalResourceGroup(reservation.getDestinationPort().getPhysicalResourceGroup());

    // Force save of vrg only once, since they all use the same reference
    reservation.getVirtualResourceGroup().setId(null);

    // Source port stuff
    reservation.getSourcePort().getPhysicalPort().setId(null);
    physicalPortRepo.save(reservation.getSourcePort().getPhysicalPort());

    virtualResourceGroupRepo.save(reservation.getSourcePort().getVirtualResourceGroup());

    reservation.getSourcePort().setId(null);
    virtualPortRepo.save(reservation.getSourcePort());

    // Destination port stuff
    reservation.getDestinationPort().getPhysicalPort().setId(null);
    physicalPortRepo.save(reservation.getDestinationPort().getPhysicalPort());

    virtualResourceGroupRepo.save(reservation.getDestinationPort().getVirtualResourceGroup());

    reservation.getDestinationPort().setId(null);
    virtualPortRepo.save(reservation.getDestinationPort());

    reservation.setId(null);
    return reservation;
  }
  
  Reservation saveReservation(Reservation reservation) {

    return reservationRepo.save(reservation);
  }

  Reservation createThroughService(Reservation reservation) {
    Future<Long> future = reservationService.create(reservation);
    Long reservationId = null;

    try {
      reservationId = future.get();
    }
    catch (InterruptedException | ExecutionException e) {
      fail(e.getMessage());
    }
    return reservationService.find(reservationId);
  }


  private Institute findInstituteToPreventUniqueKeyViolationInPhysicalResourceGroup(Long... instituteIdsInUse) {
    Iterator<Institute> instituteIterator = instituteRepo.findAll().iterator();

    Institute institute = null;
    do {
      institute = instituteIterator.next();
    }
    while (physicalResourceGroupRepo.findByInstituteId(institute.getId()) != null);
    return institute;
  }

  private PhysicalResourceGroup persistPhysicalResourceGroup(PhysicalResourceGroup group) {
    group.setId(null);
    return physicalResourceGroupRepo.save(group);
  }


  public void cleanUp() {
    EntityManager em = entityManagerFactory.createEntityManager();
    SQLQuery query = ((Session) em.getDelegate())
        .createSQLQuery("truncate physical_resource_group, virtual_resource_group, connection, log_event cascade;");
    query.executeUpdate();
  }
}