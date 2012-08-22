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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.LocalDateTime;

@Entity
public class VirtualPortRequestLink implements Loggable {

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
  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
  private LocalDateTime requestDateTime;

  @NotNull
  @ManyToOne
  private VirtualResourceGroup virtualResourceGroup;

  @NotNull
  @ManyToOne
  private PhysicalResourceGroup physicalResourceGroup;

  @Enumerated(EnumType.STRING)
  private RequestStatus status = RequestStatus.PENDING;

  private String userLabel;

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

  public boolean isPending() {
    return status == RequestStatus.PENDING;
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

  public String getUserLabel() {
    return userLabel;
  }

  public void setUserLabel(String userLabel) {
    this.userLabel = userLabel;
  }

  @Override
  public String getAdminGroup() {
    return virtualResourceGroup.getAdminGroup();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("VirtualPortRequestLink [");
    if (id != null) {
      builder.append("id=");
      builder.append(id);
      builder.append(", ");
    }
    if (version != null) {
      builder.append("version=");
      builder.append(version);
      builder.append(", ");
    }
    if (uuid != null) {
      builder.append("uuid=");
      builder.append(uuid);
      builder.append(", ");
    }
    if (requestorName != null) {
      builder.append("requestorName=");
      builder.append(requestorName);
      builder.append(", ");
    }
    if (requestorEmail != null) {
      builder.append("requestorEmail=");
      builder.append(requestorEmail);
      builder.append(", ");
    }
    if (requestorUrn != null) {
      builder.append("requestorUrn=");
      builder.append(requestorUrn);
      builder.append(", ");
    }
    if (requestDateTime != null) {
      builder.append("requestDateTime=");
      builder.append(requestDateTime);
      builder.append(", ");
    }
    if (virtualResourceGroup != null) {
      builder.append("virtualResourceGroup=");
      builder.append(virtualResourceGroup);
      builder.append(", ");
    }
    if (physicalResourceGroup != null) {
      builder.append("physicalResourceGroup=");
      builder.append(physicalResourceGroup.getName());
      builder.append(", ");
    }
    if (status != null) {
      builder.append("status=");
      builder.append(status);
      builder.append(", ");
    }
    if (userLabel != null) {
      builder.append("userLabel=");
      builder.append(userLabel);
      builder.append(", ");
    }
    if (message != null) {
      builder.append("message=");
      builder.append(message);
      builder.append(", ");
    }
    if (minBandwidth != null) {
      builder.append("minBandwidth=");
      builder.append(minBandwidth);
    }
    builder.append("]");
    return builder.toString();
  }

  public enum RequestStatus {
    PENDING, APPROVED, DECLINED
  }
}
