/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;

import nl.surfnet.bod.domain.EnniPort;
import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationEndPoint;
import nl.surfnet.bod.domain.UniPort;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.web.security.Security;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class NocService {

  private Logger logger = org.slf4j.LoggerFactory.getLogger(NocService.class);

  @Resource private ReservationService reservationService;
  @Resource private VirtualPortService virtualPortService;
  @Resource private PhysicalPortService physicalPortService;
  @Resource private TransactionOperations transactionOperations;

  @PersistenceContext
  private EntityManager entityManager;

  public Collection<Reservation> movePort(final PhysicalPort oldPort, final NbiPort nbiPort) {
    Preconditions.checkState(
        !TransactionSynchronizationManager.isActualTransactionActive(),
        "transaction cannot be active");

    final PhysicalPort newPort = PhysicalPort.create(nbiPort);
    final Collection<Reservation> reservations = getActiveReservations(oldPort);

    logger.info("Move a port with {} reservations.", reservations.size());

    cancelReservationsAndWait(reservations);

    return transactionOperations.execute(new TransactionCallback<Collection<Reservation>>() {
      @Override
      public Collection<Reservation> doInTransaction(TransactionStatus status) {
        if (oldPort instanceof UniPort) {
          copyPortProperties((UniPort) oldPort, (UniPort) newPort);
          swapVirtualPorts(oldPort, newPort);
        } else if (oldPort instanceof EnniPort) {
          copyPortProperties((EnniPort) oldPort, (EnniPort) newPort);
        }

        physicalPortService.save(newPort);
        physicalPortService.delete(oldPort.getId());

        Collection<Reservation> newReservations = makeNewReservations(reservations);

        Collection<Reservation> newReservationsWithId = new ArrayList<>();
        for (Reservation newReservation : newReservations) {
          reservationService.create(newReservation);
          newReservationsWithId.add(newReservation);
        }

        return newReservationsWithId;
      }

      private void swapVirtualPorts(final PhysicalPort oldPort, final PhysicalPort newPort) {
        Collection<VirtualPort> virtualPorts = virtualPortService.findAllForUniPort((UniPort) oldPort);
        switchVirtualPortsToNewPort((UniPort) newPort, virtualPorts);
      }

      private void copyPortProperties(UniPort oldPort, UniPort newPort) {
        ((UniPort) newPort).setPhysicalResourceGroup(((UniPort) oldPort).getPhysicalResourceGroup());
      }

      private void copyPortProperties(EnniPort oldPort, EnniPort newPort) {
         newPort.setInboundPeer(oldPort.getInboundPeer());
         newPort.setOutboundPeer(oldPort.getOutboundPeer());
         newPort.setVlanRanges(oldPort.getVlanRanges());
         newPort.setBodPortId(oldPort.getBodPortId());
      }
    });
  }

  private Collection<Reservation> makeNewReservations(Collection<Reservation> reservations) {
    return Collections2.transform(reservations, new Function<Reservation, Reservation>() {
      @Override
      public Reservation apply(Reservation oldRes) {
        Reservation newRes = new Reservation();
        newRes.setStartDateTime(oldRes.getStartDateTime());
        newRes.setEndDateTime(oldRes.getEndDateTime());
        newRes.setSourcePort(newReservationEndPoint(oldRes.getSourcePort()));
        newRes.setDestinationPort(newReservationEndPoint(oldRes.getDestinationPort()));
        newRes.setName(oldRes.getName());
        newRes.setBandwidth(oldRes.getBandwidth());
        newRes.setUserCreated(oldRes.getUserCreated());
        newRes.setProtectionType(oldRes.getProtectionType());

        return newRes;
      }
    });
  }

  private ReservationEndPoint newReservationEndPoint(ReservationEndPoint endPoint) {
    if (endPoint.getVirtualPort().isPresent()) {
      return new ReservationEndPoint(endPoint.getVirtualPort().get());
    }

    throw new AssertionError("Enni ports not supported");
  }

  private void switchVirtualPortsToNewPort(UniPort newPort, Collection<VirtualPort> virtualPorts) {
    for (VirtualPort vPort : virtualPorts) {
      vPort.setPhysicalPort(newPort);
      virtualPortService.save(vPort);
    }
  }


  private void cancelReservationsAndWait(Collection<Reservation> reservations) {
    List<Optional<Future<Long>>> futures = new ArrayList<>();

    for (Reservation reservation : reservations) {
      futures.add(reservationService.cancelWithReason(reservation, "A physical port, which the newReservation used was moved", Security.getUserDetails()));
    }

    for (Optional<Future<Long>> future : futures) {
      if (future.isPresent()) {
        try {
          // waiting for the cancel to complete
          future.get().get();
        } catch (InterruptedException | ExecutionException e) {
          logger.error("Failed to wait for a newReservation to terminate:", e);
        }
      }
    }

    for (Reservation reservation : reservations) {
      // refresh the reservations, have been changed in different thread
      entityManager.refresh(reservation);
    }
  }

  private Collection<Reservation> getActiveReservations(PhysicalPort port) {
    return reservationService.findActiveByPhysicalPort(port);
  }

  @VisibleForTesting
  void setTransactionOperations(TransactionOperations transactionOperations) {
    this.transactionOperations = transactionOperations;
  }
}
