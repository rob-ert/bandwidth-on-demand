package nl.surfnet.bod.nbi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.nbi.NbiOpenDracWsClient.OpenDracStatusTranslator;

import org.junit.Test;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleCreationResultT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleStatusT;

public class OpenDracStatusTranslatorTest {

  @Test
  public void failedShouldTranslateToFailed() {
    ReservationStatus status = OpenDracStatusTranslator.translate(ValidReservationScheduleCreationResultT.FAILED);

    assertThat(status, is(ReservationStatus.FAILED));
  }

  @Test
  public void succeededPartiallyShouldTranslateToScheduled() {
    ReservationStatus status = OpenDracStatusTranslator.translate(ValidReservationScheduleCreationResultT.SUCCEEDED_PARTIALLY);

    assertThat(status, is(ReservationStatus.SCHEDULED));
  }

  @Test
  public void unknownShouldTranslateToFailed() {
    ReservationStatus status = OpenDracStatusTranslator.translate(ValidReservationScheduleCreationResultT.UNKNOWN);

    assertThat(status, is(ReservationStatus.FAILED));
  }

  @Test
  public void executionTimedOutShouldTranslateToFailed() {
    ReservationStatus status = OpenDracStatusTranslator.translate(ValidReservationScheduleStatusT.EXECUTION_TIMED_OUT);

    assertThat(status, is(ReservationStatus.FAILED));
  }

  @Test
  public void executionSucceededShouldTranslateToSucceeded() {
    ReservationStatus status = OpenDracStatusTranslator.translate(ValidReservationScheduleStatusT.EXECUTION_SUCCEEDED);

    assertThat(status, is(ReservationStatus.SUCCEEDED));
  }
}
