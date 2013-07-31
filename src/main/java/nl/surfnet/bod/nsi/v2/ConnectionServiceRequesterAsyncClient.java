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
import java.net.URI;
import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service.Mode;

import com.google.common.base.Optional;
import com.sun.xml.ws.developer.JAXWSProperties;

import org.ogf.schemas.nsi._2013._04.connection.requester.ConnectionServiceRequester;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
class ConnectionServiceRequesterAsyncClient {

  private static final String WSDL_LOCATION = "/wsdl/2.0/ogf_nsi_connection_requester_v2_0.wsdl";

  @Value("${connection.service.requester.v2.connect.timeout}")
  private int connectTimeout;

  @Value("${connection.service.requester.v2.request.timeout}")
  private int requestTimeout;

  @Async
  public void asyncSend(Optional<URI> replyTo, String soapAction, SOAPMessage message) {
    if (!replyTo.isPresent()) {
      return;
    }

    Dispatch<SOAPMessage> dispatch = createDispatcher();

    Map<String, Object> requestContext = dispatch.getRequestContext();
    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, replyTo.get().toASCIIString());
    requestContext.put(JAXWSProperties.CONNECT_TIMEOUT, connectTimeout);
    requestContext.put(JAXWSProperties.REQUEST_TIMEOUT, requestTimeout);
    requestContext.put(Dispatch.SOAPACTION_USE_PROPERTY, true);
    requestContext.put(Dispatch.SOAPACTION_URI_PROPERTY, soapAction);

    dispatch.invokeOneWay(message);
  }

  private Dispatch<SOAPMessage> createDispatcher() {
    return new ConnectionServiceRequester(wsdlUrl()).createDispatch(new QName("http://schemas.ogf.org/nsi/2013/04/connection/requester", "ConnectionServiceRequesterPort"), SOAPMessage.class, Mode.MESSAGE);
  }

  private URL wsdlUrl() {
    try {
      return new ClassPathResource(WSDL_LOCATION).getURL();
    } catch (IOException e) {
      throw new RuntimeException("Could not find the requester wsdl", e);
    }
  }

}
