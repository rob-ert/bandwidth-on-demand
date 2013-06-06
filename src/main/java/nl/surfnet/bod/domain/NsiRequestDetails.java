/**
 * Copyright (c) 2012, SURFnet BV
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

import java.net.URI;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.common.annotations.VisibleForTesting;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;

@Entity
@Indexed
@Analyzer(definition = "customanalyzer")
public class NsiRequestDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @DocumentId
  private Long id;

  @Field
  @Column(nullable = false)
  @Type(type = "nl.surfnet.bod.util.PersistentUri")
  private URI replyTo;

  @Field
  @Column(nullable = false)
  private String correlationId;

  @Field private String requesterNsa;
  @Field private String providerNsa;

  @SuppressWarnings("unused")
  private NsiRequestDetails() {
  }

  public NsiRequestDetails(URI replyTo, String correlationId) {
    this.replyTo = replyTo;
    this.correlationId = correlationId;
  }

  public NsiRequestDetails(URI replyTo, String correlationId, String requesterNsa, String providerNsa) {
    this.replyTo = replyTo;
    this.correlationId = correlationId;
    this.requesterNsa = requesterNsa;
    this.providerNsa = providerNsa;
  }

  public CommonHeaderType getCommonHeaderType() {
    return new CommonHeaderType()
      .withCorrelationId(getCorrelationId())
      .withProtocolVersion("urn:2.0:FIXME")
      .withProviderNSA(getProviderNsa())
      .withRequesterNSA(getRequesterNsa());
  }

  public URI getReplyTo() {
    return replyTo;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  @VisibleForTesting
  Long getId() {
    return id;
  }

  @VisibleForTesting
  void setId(Long id) {
    this.id = id;
  }

  public String getRequesterNsa() {
    return requesterNsa;
  }

  public String getProviderNsa() {
    return providerNsa;
  }

  @Override
  public String toString() {
    return "NsiRequestDetails [replyTo=" + replyTo + ", correlationId=" + correlationId + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    NsiRequestDetails other = (NsiRequestDetails) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    }
    else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }

}
