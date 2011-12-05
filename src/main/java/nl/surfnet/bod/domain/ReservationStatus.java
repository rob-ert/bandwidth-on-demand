package nl.surfnet.bod.domain;

import javax.persistence.Entity;

/**
 * Enum representing the status of a {@link Reservation}
 * 
 * @author Franky
 * 
 */
public enum ReservationStatus {

  NEW, FINISHED, CANCELLED_BY_USER;

}
