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
import java.util.UUID;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;

import com.google.common.collect.ImmutableList;

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
  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  private DateTime requestDateTime;

  @NotNull
  @ManyToOne(optional = false)
  private VirtualResourceGroup virtualResourceGroup;

  @NotNull
  @ManyToOne(optional = false)
  private PhysicalResourceGroup physicalResourceGroup;

  @Enumerated(EnumType.STRING)
  private RequestStatus status = RequestStatus.PENDING;

  private String userLabel;

  private String message;

  private Long minBandwidth;

  @Override
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

  public DateTime getRequestDateTime() {
    return requestDateTime;
  }

  public void setRequestDateTime(DateTime requestDateTime) {
    this.requestDateTime = requestDateTime;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Long getMinBandwidth() {
    return minBandwidth;
  }

  public void setMinBandwidth(Long minBandwidth) {
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
  public Collection<String> getAdminGroups() {
    return ImmutableList.of(virtualResourceGroup.getAdminGroup(), physicalResourceGroup.getAdminGroup());
  }

  @Override
  public String getLabel() {
    return virtualResourceGroup.getLabel();
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    VirtualPortRequestLink other = (VirtualPortRequestLink) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    }
    else if (!id.equals(other.id)) {
      return false;
    }
    if (version == null) {
      if (other.version != null) {
        return false;
      }
    }
    else if (!version.equals(other.version)) {
      return false;
    }
    return true;
  }

  public enum RequestStatus {
    PENDING, APPROVED, DECLINED, DELETE_REQUEST_PENDING, DELETE_REQUEST_APPROVED
  }
}
