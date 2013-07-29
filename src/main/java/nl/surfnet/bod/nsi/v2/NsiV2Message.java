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
package nl.surfnet.bod.nsi.v2;

import java.io.IOException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import nl.surfnet.bod.domain.PersistableDomain;

import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;

@Entity
@Table(name = "nsi_v2_message")
public class NsiV2Message implements PersistableDomain {

  public static enum Type {
    REQUEST,
    SYNC_ACK,
    ASYNC_REPLY,
    NOTIFICATION
  };

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Basic(optional = false)
  private String requesterNsa;

  @Basic(optional = false)
  private String correlationId;

  @Basic(optional = false)
  @Enumerated(EnumType.STRING)
  private Type type;

  @Basic(optional = true)
  @Column(nullable = true, columnDefinition = "TEXT")
  private String soapAction;

  @Basic(optional = false)
  @Column(nullable = false, columnDefinition = "TEXT")
  private String message;

  protected NsiV2Message() {
  }

  public NsiV2Message(String requesterNsa, String correlationId, Type type, String soapAction, String message) {
    super();
    this.requesterNsa = requesterNsa;
    this.correlationId = correlationId;
    this.type = type;
    this.soapAction = soapAction;
    this.message = message;
  }

  public static NsiV2Message fromSoapMessage(Type messageType, SOAPMessage message) throws SOAPException, JAXBException, IOException {
    CommonHeaderType header = Converters.parseNsiHeader(message);
    String[] soapActionValues = message.getMimeHeaders().getHeader("SOAPAction");
    String soapAction = (soapActionValues != null && soapActionValues.length > 0) ? soapActionValues[0] : null;
    String serializedMessage = Converters.serializeMessage(message);
    return new NsiV2Message(header.getRequesterNSA(), header.getCorrelationId(), messageType, soapAction, serializedMessage);
  }

  @Override
  public Long getId() {
    return id;
  }

  public String getRequesterNsa() {
    return requesterNsa;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public Type getType() {
    return type;
  }

  public String getSoapAction() {
    return soapAction;
  }

  public String getMessage() {
    return message;
  }
}
