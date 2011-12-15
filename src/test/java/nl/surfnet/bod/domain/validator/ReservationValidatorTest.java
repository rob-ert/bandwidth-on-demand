package nl.surfnet.bod.domain.validator;

import static org.junit.Assert.*;
import nl.surfnet.bod.domain.Reservation;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

public class ReservationValidatorTest {

  private ReservationValidator subject;
  private DateTime startDate;
  private DateTime startTime;
  private DateTime endDate;
  private DateTime endTime;
  private Errors errors;
  private Reservation reservation;

  @Before
  public void setUp() {
    subject = new ReservationValidator();
    reservation = new Reservation();
    errors = new BeanPropertyBindingResult(reservation, "reservation");

    startDate = DateTime.now();
    endDate = startDate;
  }

  @Test
  public void testSupports() {
    assertTrue(subject.supports(Reservation.class));
  }

  @Test
  public void testSupportsNot() {
    assertFalse(subject.supports(Object.class));
  }

  @Test
  public void testEndDateAfterStartDate() {
    endDate = startDate.plusDays(1);

    initReservation(reservation, startDate, endDate);
    subject.validate(reservation, errors);
    assertFalse(errors.hasErrors());
  }

  @Test
  public void testEndDateBeforeStartDate() {
    endDate = startDate.minusDays(1);

    initReservation(reservation, startDate, endDate);
    subject.validate(reservation, errors);
    assertTrue(errors.hasFieldErrors("endDate"));
    assertFalse(errors.hasGlobalErrors());
  }

  @Test(expected = NullPointerException.class)
  public void testEndDateOnStartDate() {

    initReservation(reservation, startDate, endDate);
    subject.validate(reservation, errors);

    // When days are equal, times are evaluated. Are required in domain.
  }

  @Test
  public void testEndDateOnSameDayStartTimeBeforeEndTime() {
    startTime = DateTime.now().withTime(11, 0, 0, 0);
    endTime = startTime.plusHours(1);

    initReservation(reservation, startDate, endDate, startTime, endTime);
    subject.validate(reservation, errors);
    assertFalse(errors.hasErrors());
  }

  @Test
  public void testEndDateOnSameDayStartTimeAfterEndTime() {
    endTime = DateTime.now().withTime(11, 0, 0, 0);
    startTime = endTime.plusHours(1);

    initReservation(reservation, startDate, endDate, startTime, endTime);
    subject.validate(reservation, errors);
    assertTrue(errors.hasFieldErrors("startTime"));
    assertTrue(errors.hasGlobalErrors());
  }

  @Test
  public void testEndDateOnSameDaySameStartTimeAndEndTime() {
    endDate = startDate;
    startTime = DateTime.now().withTime(11, 0, 0, 0);
    endTime = startTime;

    initReservation(reservation, startDate, endDate, startTime, endTime);
    subject.validate(reservation, errors);
    assertFalse(errors.hasFieldErrors());
    assertTrue(errors.hasGlobalErrors());
  }

  private void initReservation(Reservation reservation, DateTime startDate, DateTime endDate) {
    reservation.setStartDate(startDate.toDate());
    reservation.setEndDate(endDate.toDate());
  }

  private void initReservation(Reservation reservation, DateTime startDate, DateTime endDate, DateTime startTime,
      DateTime endTime) {
    initReservation(reservation, startDate, endDate);
    reservation.setStartTime(startTime.toDate());
    reservation.setEndTime(endTime.toDate());
  }

}
