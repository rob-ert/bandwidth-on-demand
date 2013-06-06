/**
 * Copyright (c) 2012, SURFnet BV
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.repo.*;
import nl.surfnet.bod.support.ConnectionV1Factory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.ReservationFactory;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Optional;

@Component
@Transactional
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
  private ConnectionV1Repo connectionRepo;

  Reservation createReservation(DateTime startDateTime, DateTime endDateTime, ReservationStatus status,
      PhysicalResourceGroup sourceGroup, PhysicalResourceGroup destinationGroup) {

    Reservation reservation = new ReservationFactory()
      .withNoIds()
      .setStartDateTime(startDateTime)
      .setEndDateTime(endDateTime)
      .setStatus(status).create();

    reservation.getSourcePort().getPhysicalPort().setPhysicalResourceGroup(sourceGroup);
    reservation.getDestinationPort().getPhysicalPort().setPhysicalResourceGroup(destinationGroup);

    physicalPortRepo.save(reservation.getSourcePort().getPhysicalPort());
    virtualResourceGroupRepo.save(reservation.getSourcePort().getVirtualResourceGroup());
    virtualPortRepo.save(reservation.getSourcePort());

    physicalPortRepo.save(reservation.getDestinationPort().getPhysicalPort());
    virtualResourceGroupRepo.save(reservation.getDestinationPort().getVirtualResourceGroup());
    virtualPortRepo.save(reservation.getDestinationPort());

    return reservation;
  }

  Reservation createThroughService(Reservation reservation, boolean autoProvision) {
    Future<Long> future = reservationService.create(reservation, autoProvision, Optional.<NsiRequestDetails> absent());

    try {
      Long reservationId = future.get(2L, TimeUnit.SECONDS);
      return reservationService.find(reservationId);
    }
    catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new AssertionError(e);
    }
  }

  Reservation addConnectionToReservation(Reservation reservation) {
    ConnectionV1 connection = new ConnectionV1Factory().setReservation(reservation).create();
    connection = connectionRepo.saveAndFlush(connection);

    reservation.setConnectionV1(connection);
    return reservationRepo.saveAndFlush(reservation);
  }

  public PhysicalResourceGroup createAndPersistPhysicalResourceGroup(Long prgId) {
    Institute institute = instituteRepo.findOne(prgId);
    PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().setId(prgId).setInstitute(institute).create();

    return physicalResourceGroupRepo.save(prg);
  }

  /**
   * Force a new transactions so at the end of this method the update status is
   * committed to the database.
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Reservation updateStatusAndCommit(Reservation reservation, ReservationStatus status) {
    return reservationService.updateStatus(reservation, status);
  }

}