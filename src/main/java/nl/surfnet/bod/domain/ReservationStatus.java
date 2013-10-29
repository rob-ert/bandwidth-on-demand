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

import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import nl.surfnet.bod.util.Transition;

/**
 * Enum representing the status of a {@link Reservation}.
 */
public enum ReservationStatus {

  REQUESTED, RESERVED, AUTO_START, SCHEDULED, RUNNING, SUCCEEDED, CANCELLING, CANCELLED, FAILED, NOT_ACCEPTED, PASSED_END_TIME, CANCEL_FAILED;

  /**
   * All states which are considered as error states.
   */
  public static final Set<ReservationStatus> ERROR_STATES = EnumSet.of(FAILED, NOT_ACCEPTED, CANCEL_FAILED);

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
    return this != REQUESTED && isTransitionState();
  }

  public boolean isErrorState() {
    return ERROR_STATES.contains(this);
  }

  private static Transition<ReservationStatus> t(ReservationStatus from, ReservationStatus to) {
    return new Transition<>(from, to);
  }

  private static final Set<Transition<ReservationStatus>> VALID_TRANSITIONS
    = ImmutableSet.<Transition<ReservationStatus>> builder()
      .add(t(REQUESTED, RESERVED))
      .add(t(RESERVED, AUTO_START))
      .add(t(RESERVED, SCHEDULED))
      .add(t(RESERVED, CANCELLING))
      .add(t(AUTO_START, RUNNING))
      .add(t(AUTO_START, CANCELLING))
      .add(t(SCHEDULED, AUTO_START))
      .add(t(SCHEDULED, PASSED_END_TIME))
      .add(t(SCHEDULED, CANCELLING))
      .add(t(RUNNING, SUCCEEDED))
      .add(t(RUNNING, CANCELLING))
      .add(t(CANCELLING, CANCELLED))
      .build();

  private static final ImmutableMap<Transition<ReservationStatus>, ImmutableList<Transition<ReservationStatus>>> TRANSITION_PATHS
    = ImmutableMap.<Transition<ReservationStatus>, ImmutableList<Transition<ReservationStatus>>> builder()
      .put(t(REQUESTED, AUTO_START), path(REQUESTED, RESERVED, AUTO_START))
      .put(t(REQUESTED, SCHEDULED), path(REQUESTED, RESERVED, SCHEDULED))
      .put(t(REQUESTED, RUNNING), path(REQUESTED, RESERVED, AUTO_START, RUNNING))
      .put(t(REQUESTED, SUCCEEDED), path(REQUESTED, RESERVED, AUTO_START, RUNNING, SUCCEEDED))
      .put(t(REQUESTED, CANCELLING), path(REQUESTED, RESERVED, CANCELLING))
      .put(t(REQUESTED, CANCELLED), path(REQUESTED, RESERVED, CANCELLING, CANCELLED))
      .put(t(REQUESTED, PASSED_END_TIME), path(REQUESTED, RESERVED, SCHEDULED, PASSED_END_TIME))
      .put(t(RESERVED, RUNNING), path(RESERVED, AUTO_START, RUNNING))
      .put(t(RESERVED, SUCCEEDED), path(RESERVED, AUTO_START, RUNNING, SUCCEEDED))
      .put(t(RESERVED, PASSED_END_TIME), path(RESERVED, SCHEDULED, PASSED_END_TIME))
      .put(t(RESERVED, CANCELLED), path(RESERVED, CANCELLING, CANCELLED))
      .put(t(AUTO_START, SUCCEEDED), path(AUTO_START, RUNNING, SUCCEEDED))
      .put(t(AUTO_START, CANCELLED), path(AUTO_START, CANCELLING, CANCELLED))
      .put(t(SCHEDULED, RUNNING), path(SCHEDULED, AUTO_START, RUNNING))
      .put(t(SCHEDULED, SUCCEEDED), path(SCHEDULED, AUTO_START, RUNNING, SUCCEEDED))
      .put(t(SCHEDULED, CANCELLED), path(SCHEDULED, CANCELLING, CANCELLED))
      .put(t(RUNNING, CANCELLED), path(RUNNING, CANCELLING, CANCELLED))
      .build();

  /**
   * All states that could transfer to a RUNNING state. Should be consistent with {@link #canTransition(ReservationStatus)}.
   */
  public static final Set<ReservationStatus> COULD_START_STATES;
  static {
    COULD_START_STATES = EnumSet.noneOf(ReservationStatus.class);
    for (ReservationStatus status: ReservationStatus.values()) {
      if (status.canTransition(RUNNING)) {
        COULD_START_STATES.add(status);
      }
    }
  }

  /**
   * All states which are allowed to transition to an other state. All other
   * states will automatically be regarded as endStates.
   */
  public static final Set<ReservationStatus> TRANSITION_STATES;
  static {
    TRANSITION_STATES = EnumSet.noneOf(ReservationStatus.class);
    for (Transition<ReservationStatus> transition : VALID_TRANSITIONS) {
      TRANSITION_STATES.add(transition.getFrom());
    }
  }

  public static final ReservationStatus[] TRANSITION_STATES_AS_ARRAY = TRANSITION_STATES.toArray(new ReservationStatus[0]);

  public boolean canDirectlyTransitionTo(ReservationStatus to) {
    return VALID_TRANSITIONS.contains(new Transition<>(this, to)) || to.isErrorState();
  }

  public boolean canTransition(ReservationStatus to) {
    return this == to || this.canDirectlyTransitionTo(to) || TRANSITION_PATHS.containsKey(new Transition<>(this, to));
  }

  /**
   * @return the transition path to go from {@literal this} to {@literal to}
   *         state. An empty path indicates that there is no possible path.
   */
  public ImmutableList<Transition<ReservationStatus>> transitionPath(ReservationStatus to) {
    if (this.canDirectlyTransitionTo(to)) {
      return ImmutableList.of(new Transition<>(this, to));
    } else {
      ImmutableList<Transition<ReservationStatus>> path = TRANSITION_PATHS.get(new Transition<>(this, to));
      return path == null ? ImmutableList.<Transition<ReservationStatus>>of() : path;
    }
  }

  private static ImmutableList<Transition<ReservationStatus>> path(ReservationStatus... path) {
    // Poor man's implementation of zipWith.
    ImmutableList.Builder<Transition<ReservationStatus>> builder = ImmutableList.builder();
    for (int i = 0; i < path.length - 1; ++i) {
      builder.add(new Transition<>(path[i], path[i + 1]));
    }
    return builder.build();
  }
}
