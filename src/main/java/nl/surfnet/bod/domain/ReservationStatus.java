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
package nl.surfnet.bod.domain;

import java.util.Arrays;
import java.util.List;

/**
 * Enum representing the status of a {@link Reservation}.
 * 
 * @author Franky
 * 
 */
public enum ReservationStatus {

  SCHEDULED, RUNNING, SUCCEEDED, SUBMITTED, PREPARING, CANCELLED, FAILED;

  /**
   * All states which are allowed to transition to an other state. All other
   * states will automatically be regarded as endStates.
   */
  public static final List<ReservationStatus> TRANSITION_STATES = Arrays.asList(SCHEDULED, RUNNING, SUBMITTED,
      PREPARING);

  /**
   * 
   * @param reservationStatus
   *          {@link ReservationStatus} to check
   * @return true if the reservationStatus is an endState, meaning no further
   *         state transitions are allowed. Returns false otherwise.
   */
  public boolean isEndState(ReservationStatus reservationStatus) {
    return !isTransitionState(reservationStatus);
  }

  /**
   * 
   * @param reservationStatus
   *          {@link ReservationStatus} to check
   * @return true if the reservationStatus is an transitionState, meaning
   *         further state transitions are allowed. Returns false otherwise.
   */
  public boolean isTransitionState(ReservationStatus reservationStatus) {
    return TRANSITION_STATES.contains(reservationStatus);
  }

}
