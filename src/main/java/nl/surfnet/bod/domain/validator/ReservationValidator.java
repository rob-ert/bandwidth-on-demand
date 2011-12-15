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

    if (reservation.getEndDate() != null && reservation.getStartDate() != null) {
      
      if (reservation.getEndDate().before(reservation.getStartDate())) {
        errors.rejectValue("endDate", "validation.end.before.start");
      }

      if (reservation.getStartDate().after(reservation.getEndDate())) {
        errors.rejectValue("startDate", "validation.start.after.end");
      }
    }
  }

}
