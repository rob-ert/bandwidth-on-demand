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
package nl.surfnet.bod.web.view;

import nl.surfnet.bod.domain.Reservation;

import com.google.common.base.Objects;

/**
 * View object which holds filter related data regaring {@link Reservation}s.
 * 
 * @author Franky
 * 
 */
public class ReservationFilterView {

  private final String id;
  private final String label;

  public ReservationFilterView(int year) {
    id = String.valueOf(year);
    label = id;
  }

  public ReservationFilterView(String id, String label) {
    this.id = id;
    this.label = label;
  }

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, label);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof ReservationFilterView) {
      ReservationFilterView resFilterView = (ReservationFilterView) obj;

      return Objects.equal(this.id, resFilterView.id) && Objects.equal(this.label, resFilterView.label);

    }
    else {
      return false;
    }
  }

  @Override
  public String toString() {
    return id + " " + label;
  }
}