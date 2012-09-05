/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
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
  public static final String NSI_REQUESTER_ENDPOINT = "http://localhost:" + PORT + "/bod/nsi/requester";

  private String correlationId = UUID.randomUUID().toString();
  private String connectionId = null;
  private String providerNsa = "urn:example:nsa:provider", requesterNsa = "urn:example:nsa:provider";

  public QueryRequestType createQueryRequest() {
    final MutableQueryFilterType queryFilter = new MutableQueryFilterType();
    queryFilter.setConnectionId(connectionId);

    final QueryType query = new QueryType();
    query.setOperation(QueryOperationType.SUMMARY);
    query.setProviderNSA(providerNsa);
    query.setQueryFilter(queryFilter);
    query.setSessionSecurityAttr(null);

    final QueryRequestType queryRequest = new QueryRequestType();
    queryRequest.setCorrelationId(correlationId);
    queryRequest.setQuery(query);
    queryRequest.setReplyTo(NSI_REQUESTER_ENDPOINT);

    return queryRequest;
  }

  public TerminateRequestType createTerminateRequest() {
    final TerminateRequestType terminateRequest = new TerminateRequestType();
    terminateRequest.setCorrelationId(correlationId);
    terminateRequest.setReplyTo(NSI_REQUESTER_ENDPOINT);

    terminateRequest.setTerminate(createGenericRequest());
    return terminateRequest;
  }

  public ProvisionRequestType createProvisionRequest() {
    final ProvisionRequestType provisionRequest = new ProvisionRequestType();
    provisionRequest.setCorrelationId(correlationId);
    provisionRequest.setReplyTo(NSI_REQUESTER_ENDPOINT);

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

    public void setGlobalReservationId(String globalReservationId) {
      super.globalReservationId = Lists.newArrayList(globalReservationId);
    }
  }

}
