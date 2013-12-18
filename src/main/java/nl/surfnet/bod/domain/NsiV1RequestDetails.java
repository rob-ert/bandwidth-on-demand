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

import java.net.URI;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.ogf.schemas.nsi._2013._12.framework.headers.CommonHeaderType;

@Entity
@Indexed
@Analyzer(definition = "customanalyzer")
@Table(name = "nsi_v1_request_details")
public class NsiV1RequestDetails {

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

  @SuppressWarnings("unused")
  private NsiV1RequestDetails() {
  }

  public NsiV1RequestDetails(URI replyTo, String correlationId) {
    this.replyTo = replyTo;
    this.correlationId = correlationId;
  }

  public CommonHeaderType getCommonHeaderType(String protocolVersion) {
    return new CommonHeaderType()
      .withCorrelationId(getCorrelationId())
      .withProtocolVersion(protocolVersion);
  }

  public URI getReplyTo() {
    return replyTo;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  @Override
  public String toString() {
    return "NsiRequestDetails [replyTo=" + replyTo + ", correlationId=" + correlationId + "]";
  }
}
