/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.service;

import java.util.Collection;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.web.security.Security;

@Service
public class NocService {

  @Resource
  private ReservationService reservationService;

  @Resource
  private VirtualPortService virtualPortService;

  @Resource
  private PhysicalPortService physicalPortService;

  @Resource
  private LogEventService logEventService;

  @Transactional
  public Collection<Reservation> movePort(PhysicalPort oldPort, PhysicalPort newPort) {
    Collection<VirtualPort> virtualPorts = virtualPortService.findAllForPhysicalPort(oldPort);

    Collection<Reservation> reservations = getActiveReservations(oldPort);

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

  private Collection<Reservation> makeNewReserations(Collection<Reservation> reservations) {
    return Collections2.transform(reservations, new Function<Reservation, Reservation>() {
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

  private void cancelReservations(Collection<Reservation> reservations) {
    for (Reservation reservation : reservations) {
      reservationService.cancelWithReason(reservation, "A physical port, which the reservation used was moved",
          Security.getUserDetails());
    }
  }

  private Collection<Reservation> getActiveReservations(PhysicalPort port) {
    return reservationService.findActiveByPhysicalPort(port);
  }
}
