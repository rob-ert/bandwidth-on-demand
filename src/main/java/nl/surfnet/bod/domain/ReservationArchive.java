/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.domain;

import java.util.Collection;
import java.util.Collections;

import javax.persistence.*;

@Entity
public class ReservationArchive implements Loggable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @Column(unique = true, nullable = false)
  private long reservationPrimaryKey;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String reservationAsJson;

  @Override
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

  @Override
  public Collection<String> getAdminGroups() {
    return Collections.emptyList();
  }

  @Override
  public String getLabel() {
    return String.valueOf(reservationPrimaryKey);
  }
}
