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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.validator.constraints.Range;

/**
 * Value object to represent the end-point of a Reservation (either a UNI or ENNI port).
 *
 * Only modeled as a JPA entity due to JPA leaky abstractions.
 */
@Entity
@Indexed
@Analyzer(definition = "customanalyzer")
public class ReservationEndPoint {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @DocumentId
  private Long id;

  @ManyToOne(optional = true)
  @IndexedEmbedded
  private VirtualPort virtualPort;

  @ManyToOne(optional = true)
  @IndexedEmbedded
  private EnniPort enniPort;

  @Field
  @Range(min = 1, max = 4095)
  private Integer enniVlanId;

  protected ReservationEndPoint() {
  }

  public ReservationEndPoint(VirtualPort virtualPort) {
    this.virtualPort = Preconditions.checkNotNull(virtualPort, "virtualPort is required");
    this.enniPort = null;
    this.enniVlanId = null;
  }

  public ReservationEndPoint(EnniPort enniPort, Optional<Integer> vlanId) {
    this.virtualPort = null;
    this.enniPort = Preconditions.checkNotNull(enniPort, "enniPort is required");
    Preconditions.checkArgument(enniPort.isVlanRequired() == vlanId.isPresent(), "E-NNI port {} and VLAN ID {} configuration must match", enniPort, vlanId);
    this.enniVlanId = vlanId.orNull();
  }

  public Optional<VirtualPort> getVirtualPort() {
    return Optional.fromNullable(virtualPort);
  }

  public Optional<EnniPort> getEnniPort() {
    return Optional.fromNullable(enniPort);
  }

  public Optional<UniPort> getUniPort() {
    return virtualPort != null ? Optional.of(virtualPort.getPhysicalPort()) : Optional.<UniPort>absent();
  }

  public PhysicalPort getPhysicalPort() {
    return virtualPort != null ? virtualPort.getPhysicalPort() : enniPort;
  }

  public Optional<Integer> getEnniVlanId() {
    return Optional.fromNullable(enniVlanId);
  }

  public String getNsiStpIdV1() {
    return virtualPort != null ? virtualPort.getNsiStpIdV1() : null;
  }

  public String getNsiStpIdV2() {
    return virtualPort != null ? virtualPort.getNsiStpIdV2() : enniPort.getNsiStpIdV2();
  }

  public String getLabel() {
    return virtualPort != null ? virtualPort.getLabel() : enniPort.getNocLabel();
  }

  @Override
  public String toString() {
    if (virtualPort != null) {
      return "ReservationEndPoint [virtualPort=" + virtualPort + "]";
    } else {
      return "ReservationEndPoint [enniPort=" + enniPort + ", vlanId=" + enniVlanId + "]";
    }
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(virtualPort, enniPort, enniVlanId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof ReservationEndPoint))
      return false;
    ReservationEndPoint that = (ReservationEndPoint) obj;
    return Objects.equal(this.getVirtualPort(), that.getVirtualPort())
        && Objects.equal(this.getEnniPort(), that.getEnniPort())
        && Objects.equal(this.getEnniVlanId(), that.getEnniVlanId());
  }

  public ReservationEndPoint copy() {
    if (virtualPort != null) {
      return new ReservationEndPoint(virtualPort);
    } else {
      return new ReservationEndPoint(enniPort, Optional.fromNullable(enniVlanId));
    }
  }
}
