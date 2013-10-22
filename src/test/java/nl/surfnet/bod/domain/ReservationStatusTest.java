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
package nl.surfnet.bod.domain;

import static nl.surfnet.bod.domain.ReservationStatus.AUTO_START;
import static nl.surfnet.bod.domain.ReservationStatus.CANCELLED;
import static nl.surfnet.bod.domain.ReservationStatus.CANCELLING;
import static nl.surfnet.bod.domain.ReservationStatus.CANCEL_FAILED;
import static nl.surfnet.bod.domain.ReservationStatus.FAILED;
import static nl.surfnet.bod.domain.ReservationStatus.NOT_ACCEPTED;
import static nl.surfnet.bod.domain.ReservationStatus.PASSED_END_TIME;
import static nl.surfnet.bod.domain.ReservationStatus.REQUESTED;
import static nl.surfnet.bod.domain.ReservationStatus.RESERVED;
import static nl.surfnet.bod.domain.ReservationStatus.RUNNING;
import static nl.surfnet.bod.domain.ReservationStatus.SCHEDULED;
import static nl.surfnet.bod.domain.ReservationStatus.SUCCEEDED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableList;

import nl.surfnet.bod.util.Transition;

import org.junit.Test;

public class ReservationStatusTest {

  @Test
  public void someStatesShouldBeTransitionStates() {
    assertThat(RUNNING.isTransitionState(), is(true));
    assertThat(CANCELLING.isTransitionState(), is(true));

    assertThat(FAILED.isTransitionState(), is(false));
    assertThat(FAILED.isTransitionState(), is(false));
    assertThat(CANCELLED.isTransitionState(), is(false));

    assertThat(NOT_ACCEPTED.isTransitionState(), is(false));
    assertThat(PASSED_END_TIME.isTransitionState(), is(false));
  }

  @Test
  public void someStatesShouldBeEndStates() {
    assertThat(FAILED.isEndState(), is(true));
    assertThat(NOT_ACCEPTED.isEndState(), is(true));
    assertThat(PASSED_END_TIME.isEndState(), is(true));
    assertThat(CANCELLED.isEndState(), is(true));

    assertThat(RUNNING.isEndState(), is(false));
  }

  @Test
  public void forTheseStatesShouldDeletionBeAllowed() {
    assertThat(REQUESTED.isDeleteAllowed(), is(false));
    assertThat(RESERVED.isDeleteAllowed(), is(true));
    assertThat(RUNNING.isDeleteAllowed(), is(true));
    assertThat(AUTO_START.isDeleteAllowed(), is(true));
  }

  @Test
  public void forTheseStateShouldDeletionNotBeAllowed() {
    assertThat(CANCELLED.isDeleteAllowed(), is(false));
    assertThat(FAILED.isDeleteAllowed(), is(false));
    assertThat(NOT_ACCEPTED.isDeleteAllowed(), is(false));
    assertThat(PASSED_END_TIME.isDeleteAllowed(), is(false));
    assertThat(SUCCEEDED.isDeleteAllowed(), is(false));
  }

  @Test
  public void allowedDirectTransitions() {
    assertThat(RESERVED.canDirectlyTransitionTo(AUTO_START), is(true));
    assertThat(RESERVED.canDirectlyTransitionTo(CANCEL_FAILED), is(true));
    assertThat(CANCEL_FAILED.canDirectlyTransitionTo(AUTO_START), is(false));
  }

  @Test
  public void directTransitionPath() {
    assertThat(RESERVED.transitionPath(AUTO_START), is(ImmutableList.of(new Transition<>(RESERVED, AUTO_START))));
  }

  @Test
  public void multistepTransitionPath() {
    assertThat(SCHEDULED.transitionPath(RUNNING), is(ImmutableList.of(new Transition<>(SCHEDULED, AUTO_START), new Transition<>(AUTO_START, RUNNING))));
  }

  @Test
  public void impossibleTransitionPath() {
    assertThat(RUNNING.transitionPath(AUTO_START), is(empty()));
  }
}
