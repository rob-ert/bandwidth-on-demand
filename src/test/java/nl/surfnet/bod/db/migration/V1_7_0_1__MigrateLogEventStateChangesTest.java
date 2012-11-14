package nl.surfnet.bod.db.migration;

import java.util.List;

import nl.surfnet.bod.domain.ReservationStatus;

import org.junit.Test;

import static nl.surfnet.bod.web.WebUtils.not;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class V1_7_0_1__MigrateLogEventStateChangesTest {

  private final V1_7_0_1__MigrateLogEventStateChanges subject = new V1_7_0_1__MigrateLogEventStateChanges();

  @Test
  public void shouldTranslateState() {
    String state = subject.translate("SCHEDULED");
    assertThat(state, is(ReservationStatus.AUTO_START.name().toUpperCase()));

    state = subject.translate("ScHeDuLeD");
    assertThat(state, is("ScHeDuLeD"));
  }

  @Test
  public void shouldNotTranslateStateDifferentCase() {
    String state = subject.translate("ScHeDuLeD");
    assertThat(state, is("ScHeDuLeD"));
  }

  @Test
  public void shouldNotTranslateOtherStates() {

    String translatedState;
    for (ReservationStatus state : ReservationStatus.values()) {

      if ((not("SCHEDULED".equals(state.name().toUpperCase())))) {
        translatedState = subject.translate(state.name().toUpperCase());

        assertThat(translatedState, is(state.name().toUpperCase()));
      }
    }
  }

  @Test
  public void shouldHandleNullOrEmptyWhenTranslatingState() {
    assertThat(subject.translate(null), nullValue());
    assertThat(subject.translate(""), is(""));
  }

  @Test
  public void shouldDoGarbageInIsGarbageOut() {
    assertThat(subject.translate(" "), is(" "));
    assertThat(subject.translate(";"), is(";"));
    assertThat(subject.translate("bla"), is("bla"));
  }

  @Test
  public void shouldTranslateDetailsWithSCHEDULED() {
    String translatedDetails = subject.translate("changed state from [RESERVED] to [SCHEDULED]");
    assertThat(translatedDetails, is("changed state from [RESERVED] to [AUTO_START]"));
  }

  @Test
  public void shouldSplitStates() {
    List<String> states = subject.splitStates("changed state from [RESERVED] to [CANCELLED]");

    assertThat(states, hasSize(2));
    assertThat(states.get(0), is(ReservationStatus.RESERVED.name().toUpperCase()));
    assertThat(states.get(1), is(ReservationStatus.CANCELLED.name().toUpperCase()));
  }

  @Test
  public void shouldSplitWithExtraIntroText() {
    List<String> states = subject.splitStates("Bla die bla changed state from [RESERVED] to [CANCELLED]");

    assertThat(states, hasSize(2));
    assertThat(states.get(0), is(ReservationStatus.RESERVED.name().toUpperCase()));
    assertThat(states.get(1), is(ReservationStatus.CANCELLED.name().toUpperCase()));
  }

  @Test
  public void shouldNotSplitWithoutSquareBrackets() {
    List<String> states = subject.splitStates("changed state from RESERVED to CANCELLED");

    assertThat(states, hasSize(0));
  }

  @Test
  public void shouldNotSplitWithoutIntroText() {
    List<String> states = subject.splitStates("[RESERVED] to [CANCELLED]");

    assertThat(states, hasSize(0));
  }

  @Test
  public void shouldNotSplitGarbage() {
    List<String> states = subject.splitStates("Bla");
    assertThat(states, hasSize(0));
  }

  @Test
  public void shouldNotSplitNullOrEmpty() {
    List<String> states = subject.splitStates(null);
    assertThat(states, hasSize(0));

    states = subject.splitStates("");
    assertThat(states, hasSize(0));
  }

}
