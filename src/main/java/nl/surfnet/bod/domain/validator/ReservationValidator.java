package nl.surfnet.bod.domain.validator;

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

    if (reservation.getEndDate() != null && reservation.getStartDate() != null) {

      if (reservation.getEndDate().before(reservation.getStartDate())) {
        errors.rejectValue("endDate", "validation.end.before.start");
      }

      if (reservation.getStartDate().after(reservation.getEndDate())) {
        errors.rejectValue("startDate", "validation.start.after.end");
      }

      Duration dateDuration = new Duration(reservation.getStartDate().getTime(), reservation.getEndDate().getTime());
      // If both dates are on the same day, then check the times
      if (dateDuration.getStandardDays() == 0) {
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
  }
}
