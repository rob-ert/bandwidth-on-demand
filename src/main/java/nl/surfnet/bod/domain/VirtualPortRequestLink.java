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

import java.util.UUID;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.LocalDateTime;

import com.google.common.base.Objects;

@Entity
public class VirtualPortRequestLink {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @NotNull
  @Column(nullable = false)
  private String uuid = UUID.randomUUID().toString();;

  @NotEmpty
  @Column(nullable = false)
  private String requestorName;

  @NotEmpty
  @Column(nullable = false)
  private String requestorEmail;

  @NotEmpty
  @Column(nullable = false)
  private String requestorUrn;

  @NotNull
  @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDateTime")
  private LocalDateTime requestDateTime;

  @NotNull
  @ManyToOne
  private VirtualResourceGroup virtualResourceGroup;

  @NotNull
  @ManyToOne
  private PhysicalResourceGroup physicalResourceGroup;

  @Enumerated(EnumType.STRING)
  private RequestStatus status = RequestStatus.PENDING;

  private String message;

  private Integer minBandwidth;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public VirtualResourceGroup getVirtualResourceGroup() {
    return virtualResourceGroup;
  }

  public void setVirtualResourceGroup(VirtualResourceGroup virtualResourceGroup) {
    this.virtualResourceGroup = virtualResourceGroup;
  }

  public PhysicalResourceGroup getPhysicalResourceGroup() {
    return physicalResourceGroup;
  }

  public void setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
    this.physicalResourceGroup = physicalResourceGroup;
  }

  public LocalDateTime getRequestDateTime() {
    return requestDateTime;
  }

  public void setRequestDateTime(LocalDateTime requestDateTime) {
    this.requestDateTime = requestDateTime;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Integer getMinBandwidth() {
    return minBandwidth;
  }

  public void setMinBandwidth(Integer minBandwidth) {
    this.minBandwidth = minBandwidth;
  }

  public String getRequestorUrn() {
    return requestorUrn;
  }

  public void setRequestorUrn(String requestorUrn) {
    this.requestorUrn = requestorUrn;
  }

  public RequestStatus getStatus() {
    return status;
  }

  public void setStatus(RequestStatus status) {
    this.status = status;
  }

  public enum RequestStatus {
    PENDING, APPROVED, DECLINED
  }

  public boolean isPending() {
    return status == RequestStatus.PENDING;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(VirtualPortRequestLink.class)
        .add("id", id)
        .add("uuid", uuid)
        .add("virtualResourceGroup", virtualResourceGroup)
        .add("physicalResourceGroup", physicalResourceGroup)
        .toString();
  }

  public String getRequestorName() {
    return requestorName;
  }

  public void setRequestorName(String requestorName) {
    this.requestorName = requestorName;
  }

  public String getRequestorEmail() {
    return requestorEmail;
  }

  public void setRequestorEmail(String requestorEmail) {
    this.requestorEmail = requestorEmail;
  }



}
