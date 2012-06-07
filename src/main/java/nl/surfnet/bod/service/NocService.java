package nl.surfnet.bod.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

@Service
public class NocService {

  @Autowired
  private ReservationService reservationService;

  @Autowired
  private VirtualPortService virtualPortService;

  @Autowired
  private PhysicalPortService physicalPortService;

  @Transactional
  public Collection<Reservation> movePort(PhysicalPort oldPort, PhysicalPort newPort) {
    Collection<VirtualPort> virtualPorts = virtualPortService.findAllForPhysicalPort(oldPort);

    Set<Reservation> reservations = getActiveReservations(virtualPorts);

    cancelReservations(reservations);

    saveNewPort(oldPort, newPort);

    switchVirtualPortsToNewPort(newPort, virtualPorts);

    unallocateOldPort(oldPort);

    Collection<Reservation> newReservations = makeNewReserations(reservations);

    for (Reservation reservation : newReservations) {
      reservationService.create(reservation);
    }

    return newReservations;
  }

  private Collection<Reservation> makeNewReserations(Set<Reservation> reservations) {
    return Collections2.transform(reservations,
        new Function<Reservation, Reservation>() {
          @Override
          public Reservation apply(Reservation oldRes) {
            Reservation newRes = new Reservation();
            newRes.setStartDateTime(oldRes.getStartDateTime());
            newRes.setEndDateTime(oldRes.getEndDateTime());
            newRes.setSourcePort(oldRes.getSourcePort());
            newRes.setDestinationPort(oldRes.getDestinationPort());
            newRes.setName(oldRes.getName());
            newRes.setBandwidth(oldRes.getBandwidth());
            newRes.setUserCreated(oldRes.getUserCreated());

            return newRes;
          }
        });
  }

  private void unallocateOldPort(PhysicalPort oldPort) {
    physicalPortService.delete(oldPort);
  }

  private void switchVirtualPortsToNewPort(PhysicalPort newPort, Collection<VirtualPort> virtualPorts) {
    for (VirtualPort vPort : virtualPorts) {
      vPort.setPhysicalPort(newPort);
      virtualPortService.save(vPort);
    }
  }

  private void saveNewPort(PhysicalPort oldPort, PhysicalPort newPort) {
    newPort.setPhysicalResourceGroup(oldPort.getPhysicalResourceGroup());
    physicalPortService.save(newPort);
  }

  private void cancelReservations(Set<Reservation> reservations) {
    for (Reservation reservation : reservations) {
      reservationService.cancel(reservation, Security.getUserDetails());
    }
  }

  private HashSet<Reservation> getActiveReservations(Collection<VirtualPort> virtualPorts) {
    return Sets.newHashSet(Iterables.concat(Iterables.transform(virtualPorts,
        new Function<VirtualPort, Collection<Reservation>>() {
          @Override
          public Collection<Reservation> apply(VirtualPort vp) {
            return reservationService.findActiveByVirtualPort(vp);
          }
        })));
  }
}
