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

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import com.google.common.base.Optional;

import nl.surfnet.bod.util.TimeStampBridge;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.joda.time.DateTime;

@MappedSuperclass
public abstract class AbstractConnection implements Connection {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @DocumentId
  protected Long id;

  @Version
  protected Integer jpaVersion;

  @Column(unique = true, nullable = false)
  @Field
  protected String connectionId;

  @Column(nullable = false)
  @Field
  protected String requesterNsa;

  @Column(nullable = false)
  @Field
  protected String providerNsa;

  @Column(nullable = false)
  @Field
  protected String description;

  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  @Field
  @FieldBridge(impl = TimeStampBridge.class)
  protected DateTime startTime;

  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  @Field
  @FieldBridge(impl = TimeStampBridge.class)
  protected DateTime endTime;

  @OneToOne
  @JsonIgnore //prevent loop back to reservation
  @ContainedIn
  protected Reservation reservation;

  @Column(nullable = false)
  @Field
  protected int desiredBandwidth;

  @Column(nullable = false)
  @Field
  private String protectionType = "PROTECTED";

  public AbstractConnection() {
  }

  @Override
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRequesterNsa() {
    return requesterNsa;
  }

  public void setRequesterNsa(String requesterNSA) {
    this.requesterNsa = requesterNSA;
  }

  public String getProviderNsa() {
    return providerNsa;
  }

  public void setProviderNsa(String providerNSA) {
    this.providerNsa = providerNSA;
  }

  public String getConnectionId() {
    return connectionId;
  }

  public void setConnectionId(String connectionId) {
    this.connectionId = connectionId;
  }

  public Reservation getReservation() {
    return reservation;
  }

  public void setReservation(Reservation reservation) {
    this.reservation = reservation;
  }

  @Override
  public Collection<String> getAdminGroups() {
    return reservation != null ? reservation.getAdminGroups() : Collections.<String> emptyList();
  }

  @Override
  public String getLabel() {
    return reservation != null ? reservation.getLabel() : connectionId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Optional<DateTime> getStartTime() {
    return Optional.fromNullable(startTime);
  }

  public void setStartTime(DateTime startTime) {
    this.startTime = startTime;
  }

  public Optional<DateTime> getEndTime() {
    return Optional.fromNullable(endTime);
  }

  public void setEndTime(DateTime endTime) {
    this.endTime = endTime;
  }

  public int getDesiredBandwidth() {
    return desiredBandwidth;
  }

  public void setDesiredBandwidth(int desiredBandwidth) {
    this.desiredBandwidth = desiredBandwidth;
  }

  public String getProtectionType() {
    return protectionType;
  }

  public void setProtectionType(String protectionType) {
    this.protectionType = protectionType;
  }
}
