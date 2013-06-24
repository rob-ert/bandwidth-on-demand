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

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.apache.solr.analysis.WhitespaceTokenizerFactory;
import org.hibernate.search.annotations.*;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

@Indexed
@AnalyzerDef(name = "customanalyzer",
  tokenizer = @TokenizerDef(factory = WhitespaceTokenizerFactory.class),
  filters = { @TokenFilterDef(factory = LowerCaseFilterFactory.class) })
@Analyzer(definition = "customanalyzer")
@Entity
public class PhysicalPort implements Loggable, PersistableDomain {

  // The only supported porttype at this moment
  private static final String PORT_TYPE_UNI = "UNI-N";
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @DocumentId
  private Long id;

  @Version
  private Integer version;

  @Field
  @NotEmpty
  private String nocLabel;

  @Field
  private String managerLabel;

  @Field
  @NotEmpty
  private String bodPortId;

  @Field
  @NotEmpty
  @Column(unique = true)
  private String nmsPortId;

  @IndexedEmbedded
  @NotNull
  @ManyToOne(optional = false)
  private PhysicalResourceGroup physicalResourceGroup;

  @Basic
  private final boolean vlanRequired;

  @Basic
  @Column(name = "aligned_nms")
  private boolean alignedWithNMS;

  @Field
  @Basic
  private String nmsNeId;

  @Field
  @Basic
  private String nmsPortSpeed;

  @Field
  @Basic
  private String nmsSapName;

  @Field
  @Basic
  private String signalingType;

  @Field
  @Basic
  private String supportedServiceType;

  public PhysicalPort() {
    this(false);
  }

  public PhysicalPort(boolean vlanRequired) {
    this.vlanRequired = vlanRequired;
    this.alignedWithNMS = true;
  }

  @Override
  public Long getId() {
    return this.id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public Integer getVersion() {
    return this.version;
  }

  public void setVersion(final Integer version) {
    this.version = version;
  }

  public String getNocLabel() {
    return this.nocLabel;
  }

  public void setNocLabel(final String name) {
    this.nocLabel = name;
  }

  public PhysicalResourceGroup getPhysicalResourceGroup() {
    return this.physicalResourceGroup;
  }

  public void setPhysicalResourceGroup(final PhysicalResourceGroup physicalResourceGroup) {
    this.physicalResourceGroup = physicalResourceGroup;
  }

  public String getNmsPortId() {
    return nmsPortId;
  }

  public void setNmsPortId(String nmsPortId) {
    this.nmsPortId = nmsPortId;
  }

  public String getManagerLabel() {
    return Strings.emptyToNull(managerLabel) == null ? nocLabel : managerLabel;
  }

  public boolean hasManagerLabel() {
    return Strings.emptyToNull(managerLabel) != null;
  }

  public void setManagerLabel(String managerLabel) {
    this.managerLabel = managerLabel;
  }

  public boolean isAllocated() {
    return getPhysicalResourceGroup() != null;
  }

  public String getBodPortId() {
    return bodPortId;
  }

  public void setBodPortId(String portId) {
    this.bodPortId = portId;
  }

  public boolean isVlanRequired() {
    return vlanRequired;
  }

  public void setAlignedWithNMS(boolean aligned) {
    this.alignedWithNMS = aligned;
  }

  public boolean isAlignedWithNMS() {
    return alignedWithNMS;
  }

  public String getNmsNeId() {
    return nmsNeId;
  }

  public void setNmsNeId(String nmsNeId) {
    this.nmsNeId = nmsNeId;
  }

  public String getNmsPortSpeed() {
    return nmsPortSpeed;
  }

  public void setNmsPortSpeed(String nmsPortSpeed) {
    this.nmsPortSpeed = nmsPortSpeed;
  }

  public String getNmsSapName() {
    return nmsSapName;
  }

  public void setNmsSapName(String nmsSapName) {
    this.nmsSapName = nmsSapName;
  }

  public final String getSignalingType() {
    return signalingType;
  }

  public final void setSignalingType(String signalingType) {
    this.signalingType = signalingType;
  }

  public final String getSupportedServiceType() {
    return supportedServiceType;
  }

  public final void setSupportedServiceType(String supportedServiceType) {
    this.supportedServiceType = supportedServiceType;
  }

  public final String getPortType() {
    return PORT_TYPE_UNI;
  }

  @Override
  public Collection<String> getAdminGroups() {
    return ImmutableList.of(physicalResourceGroup.getAdminGroup());
  }

  @Override
  public String getLabel() {
    return getNocLabel();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("PhysicalPort [");
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
    if (nocLabel != null) {
      builder.append("nocLabel=");
      builder.append(nocLabel);
      builder.append(", ");
    }
    if (managerLabel != null) {
      builder.append("managerLabel=");
      builder.append(managerLabel);
      builder.append(", ");
    }
    if (bodPortId != null) {
      builder.append("bodPortId=");
      builder.append(bodPortId);
      builder.append(", ");
    }
    if (nmsPortId != null) {
      builder.append("nmsPortId=");
      builder.append(nmsPortId);
      builder.append(", ");
    }
    if (physicalResourceGroup != null) {
      builder.append("physicalResourceGroup=");
      builder.append(physicalResourceGroup.getId());
      builder.append(", ");
    }
    if (nmsNeId != null) {
      builder.append("nmsNeId=");
      builder.append(nmsNeId);
      builder.append(", ");
    }
    if (nmsPortSpeed != null) {
      builder.append("nmsPortSpeed=");
      builder.append(nmsPortSpeed);
      builder.append(", ");
    }
    if (nmsSapName != null) {
      builder.append("nmsSapName=");
      builder.append(nmsSapName);
      builder.append(", ");
    }

    if (signalingType != null) {
      builder.append("signalingType=");
      builder.append(signalingType);
      builder.append(", ");
    }
    if (supportedServiceType != null) {
      builder.append("supportedServiceType=");
      builder.append(supportedServiceType);
      builder.append(", ");
    }
    builder.append("vlanRequired=");
    builder.append(vlanRequired);
    builder.append(", alignedWithNMS=");
    builder.append(alignedWithNMS);
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
    PhysicalPort other = (PhysicalPort) obj;
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

}
