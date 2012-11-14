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
package nl.surfnet.bod.nbi;

import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.nbi.NbiOpenDracWsClient.OpenDracStatusTranslator;

import org.junit.Test;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleCreationResultT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleStatusT;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OpenDracStatusTranslatorTest {

  @Test
  public void failedShouldTranslateToFailed() {
    ReservationStatus status = OpenDracStatusTranslator.translate(ValidReservationScheduleCreationResultT.FAILED, true);

    assertThat(status, is(ReservationStatus.NOT_ACCEPTED));
  }

  @Test
  public void succeededPartiallyShouldTranslateTo() {
    ReservationStatus status =
        OpenDracStatusTranslator.translate(ValidReservationScheduleCreationResultT.SUCCEEDED_PARTIALLY, true);

    assertThat(status, is(ReservationStatus.AUTO_START));
  }

  @Test
  public void unknownShouldTranslateToFailed() {
    ReservationStatus status = OpenDracStatusTranslator.translate(ValidReservationScheduleCreationResultT.UNKNOWN, true);

    assertThat(status, is(ReservationStatus.FAILED));
  }

  @Test
  public void executionTimedOutShouldTranslateToFailed() {
    ReservationStatus status = OpenDracStatusTranslator.translate(ValidReservationScheduleStatusT.EXECUTION_TIMED_OUT);

    assertThat(status, is(ReservationStatus.TIMED_OUT));
  }

  @Test
  public void executionSucceededShouldTranslateToSucceeded() {
    ReservationStatus status = OpenDracStatusTranslator.translate(ValidReservationScheduleStatusT.EXECUTION_SUCCEEDED);

    assertThat(status, is(ReservationStatus.SUCCEEDED));
  }
}
