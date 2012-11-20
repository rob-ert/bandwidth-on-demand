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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * 
 * Base entity for archiving entities to JSON.
 * 
 */
@Entity
public class ReservationArchive {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @Column(unique = true, nullable = false)
  private long reservationPrimaryKey;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String reservationAsJson;

  public final Long getId() {
    return id;
  }

  public final void setId(Long id) {
    this.id = id;
  }

  public final Integer getVersion() {
    return version;
  }

  public final void setVersion(Integer version) {
    this.version = version;
  }

  public final long getReservationPrimaryKey() {
    return reservationPrimaryKey;
  }

  public final void setReservationPrimaryKey(long reservationPrimaryKey) {
    this.reservationPrimaryKey = reservationPrimaryKey;
  }

  public final String getReservationAsJson() {
    return reservationAsJson;
  }

  public final void setReservationAsJson(String reservationAsJson) {
    this.reservationAsJson = reservationAsJson;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ReservationArchive [id=");
    builder.append(getId());
    builder.append(", version=");
    builder.append(getVersion());
    builder.append(", reservationPrimaryKey=");
    builder.append(getReservationPrimaryKey());
    builder.append(", reservationAsJson=");
    builder.append(getReservationAsJson());
    builder.append("]");
    return builder.toString();
  }
}
