package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class ReservationStatusTest {

  @Test
  public void someStatesShouldBeTransitionStates() {
    assertThat(ReservationStatus.PREPARING.isTransitionState(), is(true));
    assertThat(ReservationStatus.RUNNING.isTransitionState(), is(true));

    assertThat(ReservationStatus.FAILED.isTransitionState(), is(false));
    assertThat(ReservationStatus.CANCELLED.isTransitionState(), is(false));
  }

  @Test
  public void someStatesShouldBeEndStates() {
    assertThat(ReservationStatus.FAILED.isEndState(), is(true));
    assertThat(ReservationStatus.CANCELLED.isEndState(), is(true));

    assertThat(ReservationStatus.PREPARING.isEndState(), is(false));
    assertThat(ReservationStatus.RUNNING.isEndState(), is(false));
  }

}
