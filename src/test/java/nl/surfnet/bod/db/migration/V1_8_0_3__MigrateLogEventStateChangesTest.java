/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.db.migration;

import java.util.List;

import nl.surfnet.bod.domain.ReservationStatus;

import org.junit.Test;

import static nl.surfnet.bod.web.WebUtils.not;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class V1_8_0_3__MigrateLogEventStateChangesTest {

  private final V1_8_0_3__MigrateLogEventStateChanges subject = new V1_8_0_3__MigrateLogEventStateChanges();

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
