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
package nl.surfnet.bod.support;

import java.util.UUID;

import org.ogf.schemas.nsi._2011._10.connection._interface.ProvisionRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.QueryRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.TerminateRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryFilterType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryOperationType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryType;

import com.google.common.collect.Lists;

public class ConnectionServiceProviderFactory {

  public static final int PORT = 9082;
  public String replyTo = "http://localhost:" + PORT + "/bod/nsi/requester";

  private String correlationId = UUID.randomUUID().toString();
  private String connectionId = null;
  private String providerNsa = "urn:example:nsa:provider";
  private String requesterNsa = "urn:example:nsa:requester";

  public QueryRequestType createQueryRequest() {
    final MutableQueryFilterType queryFilter = new MutableQueryFilterType();
    queryFilter.setConnectionId(connectionId);

    final QueryType query = new QueryType();
    query.setOperation(QueryOperationType.SUMMARY);
    query.setProviderNSA(providerNsa);
    query.setRequesterNSA(requesterNsa);
    query.setQueryFilter(queryFilter);
    query.setSessionSecurityAttr(null);

    final QueryRequestType queryRequest = new QueryRequestType();
    queryRequest.setCorrelationId(correlationId);
    queryRequest.setQuery(query);
    queryRequest.setReplyTo(replyTo);

    return queryRequest;
  }

  public ConnectionServiceProviderFactory setReplyTo(String replyTo) {
    this.replyTo = replyTo;
    return this;
  }

  public TerminateRequestType createTerminateRequest() {
    final TerminateRequestType terminateRequest = new TerminateRequestType();
    terminateRequest.setCorrelationId(correlationId);
    terminateRequest.setReplyTo(replyTo);

    terminateRequest.setTerminate(createGenericRequest());
    return terminateRequest;
  }

  public ProvisionRequestType createProvisionRequest() {
    final ProvisionRequestType provisionRequest = new ProvisionRequestType();
    provisionRequest.setCorrelationId(correlationId);
    provisionRequest.setReplyTo(replyTo);

    provisionRequest.setProvision(createGenericRequest());
    return provisionRequest;

  }

  private GenericRequestType createGenericRequest() {
    GenericRequestType genericRequest = new GenericRequestType();
    genericRequest.setConnectionId(connectionId);
    genericRequest.setProviderNSA(providerNsa);
    genericRequest.setRequesterNSA(requesterNsa);
    genericRequest.setSessionSecurityAttr(null);

    return genericRequest;
  }

  public final ConnectionServiceProviderFactory setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
    return this;
  }

  public final ConnectionServiceProviderFactory setNsaProviderUrn(String nsaProviderUrn) {
    this.providerNsa = nsaProviderUrn;
    return this;
  }

  public final ConnectionServiceProviderFactory setConnectionId(String connectionId) {
    this.connectionId = connectionId;
    return this;
  }

  public ConnectionServiceProviderFactory setProviderNsa(String providerNSA) {
    this.providerNsa = providerNSA;
    return this;
  }

  public ConnectionServiceProviderFactory setRequesterNsa(String requesterNSA) {
    this.requesterNsa = requesterNSA;
    return this;
  }

  private class MutableQueryFilterType extends QueryFilterType {
    private static final long serialVersionUID = 1L;

    public void setConnectionId(String connectionId) {
      super.connectionId = Lists.newArrayList(connectionId);
    }
  }

}
