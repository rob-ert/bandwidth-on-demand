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

import java.util.EnumSet;
import java.util.Set;

/**
 * Enum representing the status of a {@link Reservation}.
 */
public enum ReservationStatus {

  REQUESTED, RESERVED, AUTO_START, SCHEDULED, RUNNING, SUCCEEDED, CANCELLED, FAILED, NOT_ACCEPTED, TIMED_OUT, CANCEL_FAILED;

  /**
   * All states which are considered as error states.
   */
  public static final Set<ReservationStatus> ERROR_STATES = EnumSet.of(FAILED, NOT_ACCEPTED, TIMED_OUT, CANCEL_FAILED);

  /**
   * All states that could transfer to a RUNNING state
   */
  public static final Set<ReservationStatus> COULD_START_STATES = EnumSet
      .of(REQUESTED, RESERVED, AUTO_START, SCHEDULED);

  /**
   * All states which are allowed to transition to an other state. All other
   * states will automatically be regarded as endStates.
   */
  public static final Set<ReservationStatus> TRANSITION_STATES = EnumSet.of(REQUESTED, RESERVED, AUTO_START, SCHEDULED,
      RUNNING);

  public static final ReservationStatus[] TRANSITION_STATES_AS_ARRAY = TRANSITION_STATES
      .toArray(new ReservationStatus[TRANSITION_STATES.size()]);

  /**
   * @return true if the reservationStatus is an endState, meaning no further
   *         state transitions are allowed. Returns false otherwise.
   */
  public boolean isEndState() {
    return !isTransitionState();
  }

  /**
   * @return true if the reservationStatus is an transitionState, meaning
   *         further state transitions are allowed. Returns false otherwise.
   */
  public boolean isTransitionState() {
    return TRANSITION_STATES.contains(this);
  }

  /**
   * @return true if a Reservation is allowed to be delete, only based on its
   *         state.
   *
   */
  public boolean isDeleteAllowed() {
    return isTransitionState();
  }

  public boolean isErrorState() {
    return ERROR_STATES.contains(this);
  }

}