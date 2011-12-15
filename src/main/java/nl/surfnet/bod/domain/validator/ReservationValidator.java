package nl.surfnet.bod.domain.validator;

import nl.surfnet.bod.domain.Reservation;

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

    if (reservation.getEndTimeStamp() != null && reservation.getStartTimeStamp() != null) {
      if (reservation.getEndTimeStamp().before(reservation.getStartTimeStamp())) {
        errors.rejectValue("endTime", "validation.end.after.start");
      }
      else {
        errors.rejectValue("startTime", "validation.start.before.end");
      }
    }
  }

}
