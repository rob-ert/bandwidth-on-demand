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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;


@Entity
public class ReservationFlattened {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;
  
  private String reservationAsString;

  public ReservationFlattened(final Reservation reservation) {
    super();
    this.reservationAsString = reservation.toString();
  }

  public String getReservationAsString() {
    return reservationAsString;
  }

  public void setReservationAsString(final String reservationAsString) {
    this.reservationAsString = reservationAsString;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ReservationFlattened [id=");
    builder.append(id);
    builder.append(", version=");
    builder.append(version);
    builder.append(", reservationAsString=");
    builder.append(reservationAsString);
    builder.append("]");
    return builder.toString();
  }

  
}
