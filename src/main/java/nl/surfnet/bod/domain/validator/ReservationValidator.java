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

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.web.security.Security;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;
import org.joda.time.Period;
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
    VirtualPort sourcePort = reservation.getSourcePort();
    VirtualPort destinationPort = reservation.getDestinationPort();

    if (sourcePort == null || destinationPort == null) {
      return;
    }

    if (sourcePort.equals(destinationPort)) {
      errors.reject("validation.reservation.sameports", "Source and Destination port should be different");
      errors.rejectValue("sourcePort", "", "");
      errors.rejectValue("destinationPort", "", "");
    }

    String groupNameOfSource = sourcePort.getVirtualResourceGroup().getSurfConnextGroupName();
    String groupNameOfDestination = destinationPort.getVirtualResourceGroup().getSurfConnextGroupName();

    if (!groupNameOfSource.equals(groupNameOfDestination)) {
      errors.reject("validation.reservation.security", "Ports are not in the same virtualResourceGroup");
    }

    if (!Security.getUserDetails().getUserGroupIds().contains(groupNameOfSource)) {
      errors.reject("validation.reservation.security", "You can not select this port");
    }
  }

  private void validateStartAndEndDate(Errors errors, Reservation reservation) {
    if (reservation.getStartDate() == null || reservation.getStartTime() == null || reservation.getEndDate() == null
        || reservation.getEndTime() == null) {
      return;
    }

    LocalDate startDate = reservation.getStartDate();
    LocalTime startTime = reservation.getStartTime();
    LocalDate endDate = reservation.getEndDate();
    LocalTime endTime = reservation.getEndTime();

    LocalDate today = LocalDate.now();
    LocalTime now = LocalTime.now();

    if (startDate.isBefore(today)) {
      errors.rejectValue("startDate", "validation.date.past");
    }
    if (endDate.isBefore(today)) {
      errors.rejectValue("endDate", "validation.date.past");
    }
    if (startDate.isEqual(today) && startTime.isBefore(now)) {
      errors.rejectValue("startTime", "validation.date.past");
    }
    if (endDate.isEqual(today) && endTime.isBefore(now)) {
      errors.rejectValue("endTime", "validation.date.past");
    }

    if (endDate.isBefore(startDate)) {
      errors.rejectValue("endDate", "validation.end.before.start");
    }

    if (datesAreOnSameDay(startDate, endDate)) {
      if (endTime.isBefore(startTime)) {
        errors.rejectValue("endTime", "validation.end.before.start");
      }

      Period period = new Period(reservation.getStartTime(), reservation.getEndTime());
      if (period.toStandardMinutes().isLessThan(Minutes.minutes(5))) {
        errors.reject("validation.reservation.tooshort");
      }
    }
  }

  private boolean datesAreOnSameDay(LocalDate startDate, LocalDate endDate) {
    return startDate.isEqual(endDate);
  }

}
