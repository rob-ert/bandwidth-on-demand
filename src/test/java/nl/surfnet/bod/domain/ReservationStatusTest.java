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
package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class ReservationStatusTest {

  @Test
  public void someStatesShouldBeTransitionStates() {
    assertThat(ReservationStatus.RUNNING.isTransitionState(), is(true));

    assertThat(ReservationStatus.FAILED.isTransitionState(), is(false));
    assertThat(ReservationStatus.FAILED.isTransitionState(), is(false));
    assertThat(ReservationStatus.CANCELLED.isTransitionState(), is(false));

    assertThat(ReservationStatus.NOT_ACCEPTED.isTransitionState(), is(false));
    assertThat(ReservationStatus.TIMED_OUT.isTransitionState(), is(false));
  }

  @Test
  public void someStatesShouldBeEndStates() {
    assertThat(ReservationStatus.FAILED.isEndState(), is(true));
    assertThat(ReservationStatus.NOT_ACCEPTED.isEndState(), is(true));
    assertThat(ReservationStatus.TIMED_OUT.isEndState(), is(true));
    assertThat(ReservationStatus.CANCELLED.isEndState(), is(true));

    assertThat(ReservationStatus.RUNNING.isEndState(), is(false));
  }

  @Test
  public void forTheseStatesShouldDeletionBeAllowed() {
    assertThat(ReservationStatus.REQUESTED.isDeleteAllowed(), is(true));
    assertThat(ReservationStatus.RUNNING.isDeleteAllowed(), is(true));
    assertThat(ReservationStatus.AUTO_START.isDeleteAllowed(), is(true));
  }

  @Test
  public void forTheseStateShouldDeletionNotBeAllowed() {
    assertThat(ReservationStatus.CANCELLED.isDeleteAllowed(), is(false));
    assertThat(ReservationStatus.FAILED.isDeleteAllowed(), is(false));
    assertThat(ReservationStatus.NOT_ACCEPTED.isDeleteAllowed(), is(false));
    assertThat(ReservationStatus.TIMED_OUT.isDeleteAllowed(), is(false));
    assertThat(ReservationStatus.SUCCEEDED.isDeleteAllowed(), is(false));
  }

}
