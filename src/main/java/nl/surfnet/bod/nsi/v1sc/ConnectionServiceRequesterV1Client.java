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
package nl.surfnet.bod.nsi.v1sc;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import com.sun.xml.ws.client.ClientTransportException;

import nl.surfnet.bod.domain.NsiRequestDetails;

import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort;
import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionServiceRequester;
import org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReserveConfirmedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ConnectionServiceRequesterV1Client {

  private static final String WSDL_LOCATION = "/wsdl/1.0_sc/ogf_nsi_connection_requester_v1_0.wsdl";

  private final Logger log = LoggerFactory.getLogger(ConnectionServiceRequesterV1Client.class);

  @Async
  public void asyncSendReserveConfirmed(ReserveConfirmedType reserveConfirmedType, NsiRequestDetails requestDetails) {
    ConnectionRequesterPort port = createPort(requestDetails.getReplyTo());
    try {
      port.reserveConfirmed(new Holder<>(requestDetails.getCorrelationId()), reserveConfirmedType);
    } catch (ClientTransportException | ServiceException e) {
      log.info("Sending Reserve Confirmed failed", e);
    }
  }

  @Async
  public void asyncSendReserveFailed(GenericFailedType reserveFailed, NsiRequestDetails requestDetails) {
    ConnectionRequesterPort port = createPort(requestDetails.getReplyTo());
    try {
      port.reserveFailed(new Holder<>(requestDetails.getCorrelationId()), reserveFailed);
    } catch (ClientTransportException | ServiceException e) {
      log.info("Sending Reserve Failed failed", e);
    }
  }

  @Async
  public void asyncSendGenericFailed(GenericFailedType genericFailed, NsiRequestDetails requestDetails) {
    ConnectionRequesterPort port = createPort(requestDetails.getReplyTo());
    try {
      port.provisionFailed(new Holder<>(requestDetails.getCorrelationId()), genericFailed);
    } catch (ClientTransportException | ServiceException e) {
      log.info("Sending Generic Failed failed", e);
    }
  }

  @Async
  public void asyncSendProvisionConfirmed(GenericConfirmedType genericConfirm, NsiRequestDetails requestDetails) {
    ConnectionRequesterPort port = createPort(requestDetails.getReplyTo());
    try {
      port.provisionConfirmed(new Holder<>(requestDetails.getCorrelationId()), genericConfirm);
    } catch (ClientTransportException | ServiceException e) {
      log.info("Sending Provision Confirmed failed", e);
    }
  }

  @Async
  public void sendAsyncTerminateConfirmed(GenericConfirmedType genericConfirmed, NsiRequestDetails requestDetails) {
    ConnectionRequesterPort port = createPort(requestDetails.getReplyTo());
    try {
      port.terminateConfirmed(new Holder<>(requestDetails.getCorrelationId()), genericConfirmed);
    } catch (ClientTransportException | ServiceException e) {
      log.info("Sending Terminate Confirmed failed", e);
    }
  }

  @Async
  public void asyncSendTerminateFailed(GenericFailedType genericFailed, NsiRequestDetails requestDetails) {
    ConnectionRequesterPort port = createPort(requestDetails.getReplyTo());
    try {
      port.terminateFailed(new Holder<>(requestDetails.getCorrelationId()), genericFailed);
    } catch (ClientTransportException | ServiceException e) {
      log.info("Sending Terminate Failed failed", e);
    }
  }

  @Async
  public void asyncSendQueryConfirmed(QueryConfirmedType queryResult, NsiRequestDetails requestDetails) {
    ConnectionRequesterPort port = createPort(requestDetails.getReplyTo());
    try {
      port.queryConfirmed(new Holder<>(requestDetails.getCorrelationId()), queryResult);
    } catch (ClientTransportException | ServiceException e) {
      log.info("Sending Query Confirmed failed", e);
    }
  }

//  private static final QName SERVICE_NAME =
//    new QName("http://schemas.ogf.org/nsi/2011/10/connection/requester", "ConnectionServiceRequester");

  private ConnectionRequesterPort createPort(URI endpoint) {
    ConnectionRequesterPort port = new ConnectionServiceRequester(wsdlUrl()).getConnectionServiceRequesterPort();
    ((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint.toASCIIString());

    return port;
  }

  private URL wsdlUrl() {
    try {
      return new ClassPathResource(WSDL_LOCATION).getURL();
    }
    catch (IOException e) {
      throw new RuntimeException("Could not find the requester wsdl", e);
    }
  }

}
