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

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import nl.surfnet.bod.AppConfiguration;
import nl.surfnet.bod.config.IntegrationDbConfiguration;
import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.repo.InstituteRepo;
import nl.surfnet.bod.repo.PhysicalPortRepo;
import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import nl.surfnet.bod.repo.VirtualPortRepo;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppConfiguration.class, IntegrationDbConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ActiveProfiles("opendrac-offline")
public class ReservationServiceTestIntegration {

  @Resource private ReservationService subject;
  @Resource private PhysicalPortRepo physicalPortRepo;
  @Resource private VirtualPortRepo virtualPortRepo;
  @Resource private InstituteRepo instituteRepo;
  @Resource private VirtualResourceGroupRepo virtualResourceGroupRepo;
  @Resource private PhysicalResourceGroupRepo physicalResourceGroupRepo;

  @Resource private PlatformTransactionManager txManager;

  @BeforeClass
  public static void cleanup() {
    DatabaseTestHelper.clearIntegrationDatabaseSkipBaseData();
  }

  @Test
  public void createAReservation() throws InterruptedException, ExecutionException, TimeoutException {
    final Reservation reservation = runIntransaction(new Callable<Reservation>() {
      @Override
      public Reservation call() {
        return createReservation();
      }
    });

    Future<Long> result = runIntransaction(new Callable<Future<Long>>() {
      @Override
      public Future<Long> call() {
        return subject.create(reservation);
      }
    });

    Reservation createReservation = subject.find(result.get(5, TimeUnit.SECONDS));
    assertThat(createReservation.getReservationId(), containsString("SCHEDULE-"));
  }

  private <T> T runIntransaction(Callable<T> block) {
    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setName("test");
    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus status = txManager.getTransaction(def);

    try {
      T result = block.call();
      txManager.commit(status);
      return result;
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private Reservation createReservation() {
    Institute institute = instituteRepo.findOne(1L);
    PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().withNoIds().setInstitute(institute).create();
    prg = physicalResourceGroupRepo.save(prg);

    Reservation reservation = new ReservationFactory().withNoIds().create();

    reservation.getSourcePort().getPhysicalPort().setPhysicalResourceGroup(prg);
    reservation.getDestinationPort().getPhysicalPort().setPhysicalResourceGroup(prg);

    physicalPortRepo.save(reservation.getSourcePort().getPhysicalPort());
    physicalPortRepo.save(reservation.getDestinationPort().getPhysicalPort());

    virtualResourceGroupRepo.save(reservation.getSourcePort().getVirtualResourceGroup());
    virtualResourceGroupRepo.save(reservation.getDestinationPort().getVirtualResourceGroup());

    virtualPortRepo.save(reservation.getSourcePort());
    virtualPortRepo.save(reservation.getDestinationPort());

    return reservation;
  }
}
