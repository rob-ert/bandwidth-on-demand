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
package nl.surfnet.bod.domain.validator;

import java.util.Date;

import nl.surfnet.bod.domain.Reservation;

import org.joda.time.Duration;
import org.joda.time.Seconds;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ReservationValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return Reservation.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object objectToValidate, Errors errors) {
    Reservation reservation = (Reservation) objectToValidate;

    validatePorts(errors, reservation);
    validateStartAndEndDate(errors, reservation);

  }

  private void validatePorts(Errors errors, Reservation reservation) {
    if (reservation.getSourcePort() == null || reservation.getDestinationPort() == null) {
      return;
    }

    if (reservation.getSourcePort().equals(reservation.getDestinationPort())) {
      errors.reject("validation.reservation.sameports", "Source and Destination port should be different");
      errors.rejectValue("sourcePort", "", "");
      errors.rejectValue("destinationPort", "", "");
    }
  }

  private void validateStartAndEndDate(Errors errors, Reservation reservation) {
    if (reservation.getStartDate() == null || reservation.getStartTime() == null || reservation.getEndDate() == null
        || reservation.getEndTime() == null) {
      return;
    }

    if (reservation.getEndDate().before(reservation.getStartDate())) {
      errors.rejectValue("endDate", "validation.end.before.start");
    }

    if (reservation.getStartDate().after(reservation.getEndDate())) {
      errors.rejectValue("startDate", "validation.start.after.end");
    }


    if (datesAreOnSameDay(reservation.getStartDate(), reservation.getEndDate())) {
      if (reservation.getEndTime().before(reservation.getStartTime())) {
        errors.rejectValue("endTime", "validation.end.before.start");
      }

      if (reservation.getStartTime().after(reservation.getEndTime())) {
        errors.rejectValue("startTime", "validation.start.after.end");
      }

      Duration timeDuration = new Duration(reservation.getStartTime().getTime(), reservation.getEndTime().getTime());
      if (timeDuration.toStandardSeconds().isLessThan(Seconds.TWO)) {
        errors.reject("validation.reservation.tooshort");
      }
    }
  }

  private boolean datesAreOnSameDay(Date startDate, Date endDate) {
    return new Duration(startDate.getTime(), endDate.getTime()).getStandardDays() == 0;
  }

}
