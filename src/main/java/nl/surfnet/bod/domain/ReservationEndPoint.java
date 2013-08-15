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
import javax.validation.constraints.NotNull;

import com.google.common.base.Preconditions;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

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

  @NotNull
  @ManyToOne(optional = false)
  @IndexedEmbedded
  private VirtualPort virtualPort;

  protected ReservationEndPoint() {
  }

  public ReservationEndPoint(VirtualPort virtualPort) {
    this.virtualPort = Preconditions.checkNotNull(virtualPort, "virtualPort is required");
  }

  public VirtualResourceGroup getVirtualResourceGroup() {
    return virtualPort.getVirtualResourceGroup();
  }

  public VirtualPort getVirtualPort() {
    return virtualPort;
  }

  public UniPort getPhysicalPort() {
    return virtualPort.getPhysicalPort();
  }

  public PhysicalResourceGroup getPhysicalResourceGroup() {
    return virtualPort.getPhysicalResourceGroup();
  }

  public Long getMaxBandwidth() {
    return virtualPort.getMaxBandwidth();
  }

  public Integer getVlanId() {
    return virtualPort.getVlanId();
  }

  public String getNsiStpIdV1() {
    return virtualPort.getNsiStpIdV1();
  }

  public String getNsiStpIdV2() {
    return virtualPort.getNsiStpIdV2();
  }

  public String getLabel() {
    return virtualPort.getLabel();
  }

  public String getUserLabel() {
    return virtualPort.getUserLabel();
  }

  public String getManagerLabel() {
    return virtualPort.getManagerLabel();
  }

  @Override
  public String toString() {
    return "ReservationEndPoint [virtualPort=" + virtualPort + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((virtualPort == null) ? 0 : virtualPort.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof ReservationEndPoint))
      return false;
    ReservationEndPoint that = (ReservationEndPoint) obj;
    return this.getVirtualPort().equals(that.getVirtualPort());
  }

  public ReservationEndPoint copy() {
    return new ReservationEndPoint(virtualPort);
  }
}
