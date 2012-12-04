/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.apache.solr.analysis.WhitespaceTokenizerFactory;
import org.hibernate.search.annotations.*;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Strings;

@Indexed
@AnalyzerDef(name = "customanalyzer", tokenizer = @TokenizerDef(factory = WhitespaceTokenizerFactory.class), filters = { @TokenFilterDef(factory = LowerCaseFilterFactory.class) })
@Entity
public class PhysicalPort implements Loggable, PersistableDomain {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @DocumentId
  private Long id;

  @Version
  private Integer version;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @NotEmpty
  private String nocLabel;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  private String managerLabel;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @NotEmpty
  private String bodPortId;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @NotEmpty
  @Column(unique = true)
  private String nmsPortId;

  @IndexedEmbedded
  @NotNull
  @ManyToOne(optional = false)
  private PhysicalResourceGroup physicalResourceGroup;

  @Basic
  private boolean vlanRequired;

  @Basic
  @Column(name = "aligned_nms")
  private boolean alignedWithNMS;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @Basic
  private String nmsNeId;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @Basic
  private String nmsPortSpeed;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @Basic
  private String nmsSapName;

  @Field
  @Basic
  private String signalingType;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
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

  @Override
  public String getAdminGroup() {
    return physicalResourceGroup.getAdminGroup();
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
    result = prime * result + (alignedWithNMS ? 1231 : 1237);
    result = prime * result + ((bodPortId == null) ? 0 : bodPortId.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((managerLabel == null) ? 0 : managerLabel.hashCode());
    result = prime * result + ((nmsNeId == null) ? 0 : nmsNeId.hashCode());
    result = prime * result + ((nmsPortId == null) ? 0 : nmsPortId.hashCode());
    result = prime * result + ((nmsPortSpeed == null) ? 0 : nmsPortSpeed.hashCode());
    result = prime * result + ((nmsSapName == null) ? 0 : nmsSapName.hashCode());
    result = prime * result + ((nocLabel == null) ? 0 : nocLabel.hashCode());
    result = prime * result + ((physicalResourceGroup == null) ? 0 : physicalResourceGroup.hashCode());
    result = prime * result + ((signalingType == null) ? 0 : signalingType.hashCode());
    result = prime * result + ((supportedServiceType == null) ? 0 : supportedServiceType.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    result = prime * result + (vlanRequired ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PhysicalPort other = (PhysicalPort) obj;
    if (alignedWithNMS != other.alignedWithNMS)
      return false;
    if (bodPortId == null) {
      if (other.bodPortId != null)
        return false;
    }
    else if (!bodPortId.equals(other.bodPortId))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    if (managerLabel == null) {
      if (other.managerLabel != null)
        return false;
    }
    else if (!managerLabel.equals(other.managerLabel))
      return false;
    if (nmsNeId == null) {
      if (other.nmsNeId != null)
        return false;
    }
    else if (!nmsNeId.equals(other.nmsNeId))
      return false;
    if (nmsPortId == null) {
      if (other.nmsPortId != null)
        return false;
    }
    else if (!nmsPortId.equals(other.nmsPortId))
      return false;
    if (nmsPortSpeed == null) {
      if (other.nmsPortSpeed != null)
        return false;
    }
    else if (!nmsPortSpeed.equals(other.nmsPortSpeed))
      return false;
    if (nmsSapName == null) {
      if (other.nmsSapName != null)
        return false;
    }
    else if (!nmsSapName.equals(other.nmsSapName))
      return false;
    if (nocLabel == null) {
      if (other.nocLabel != null)
        return false;
    }
    else if (!nocLabel.equals(other.nocLabel))
      return false;
    if (physicalResourceGroup == null) {
      if (other.physicalResourceGroup != null)
        return false;
    }
    else if (!physicalResourceGroup.equals(other.physicalResourceGroup))
      return false;
    if (signalingType == null) {
      if (other.signalingType != null)
        return false;
    }
    else if (!signalingType.equals(other.signalingType))
      return false;
    if (supportedServiceType == null) {
      if (other.supportedServiceType != null)
        return false;
    }
    else if (!supportedServiceType.equals(other.supportedServiceType))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    }
    else if (!version.equals(other.version))
      return false;
    if (vlanRequired != other.vlanRequired)
      return false;
    return true;
  }

}
