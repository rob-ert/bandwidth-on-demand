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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

import com.google.common.base.Preconditions;

import nl.surfnet.bod.domain.NbiPort.InterfaceType;

import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.apache.solr.analysis.WhitespaceTokenizerFactory;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.hibernate.validator.constraints.NotEmpty;

@AnalyzerDef(name = "customanalyzer",
  tokenizer = @TokenizerDef(factory = WhitespaceTokenizerFactory.class),
  filters = { @TokenFilterDef(factory = LowerCaseFilterFactory.class) })
@Analyzer(definition = "customanalyzer")
@Entity
public abstract class PhysicalPort implements PersistableDomain, Loggable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @DocumentId
  private Long id;

  @Version
  private Integer version;

  @IndexedEmbedded
  @Embedded
  private NbiPort nbiPort;

  @Field
  @NotEmpty
  private String nocLabel;

  @Field
  @NotEmpty
  @Column(unique = true)
  private String bodPortId;

  @Basic(optional = false)
  @Enumerated(EnumType.STRING)
  private NmsAlignmentStatus nmsAlignmentStatus = NmsAlignmentStatus.ALIGNED;;

  public PhysicalPort() {
  }

  public PhysicalPort(NbiPort nbiPort) {
    this.nbiPort = nbiPort;
    this.nocLabel = nbiPort.getSuggestedNocLabel();
    this.bodPortId = nbiPort.getSuggestedBodPortId();
  }

  public static PhysicalPort create(NbiPort nbiPort) {
    if (nbiPort.getInterfaceType() == InterfaceType.E_NNI) {
      return new EnniPort(nbiPort);
    } else if (nbiPort.getInterfaceType() == InterfaceType.UNI) {
      return new UniPort(nbiPort);
    } else {
      throw new IllegalArgumentException(String.format("Can not create physical port, interface type '%s'", nbiPort.getInterfaceType()));
    }
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

  public String getBodPortId() {
    return bodPortId;
  }

  public void setBodPortId(String portId) {
    this.bodPortId = portId;
  }

  public void setNmsAlignmentStatus(NmsAlignmentStatus nmsAlignmentStatus) {
    Preconditions.checkNotNull(nmsAlignmentStatus);
    this.nmsAlignmentStatus = nmsAlignmentStatus;
  }

  public NmsAlignmentStatus getNmsAlignmentStatus() {
    return nmsAlignmentStatus;
  }

  public boolean isAlignedWithNMS() {
    return nmsAlignmentStatus == NmsAlignmentStatus.ALIGNED;
  }

  public NbiPort getNbiPort() {
    return nbiPort;
  }

  public String getNmsPortId() {
    return this.nbiPort.getNmsPortId();
  }

  public boolean isVlanRequired() {
    return nbiPort.isVlanRequired();
  }

  @Override
  public String getLabel() {
    return getNocLabel();
  }

}
