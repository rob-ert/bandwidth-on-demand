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
package nl.surfnet.bod.nbi.mtosi;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import nl.surfnet.bod.domain.Reservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ReserveException;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ServiceComponentActivationInterface;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ServiceComponentActivationInterfaceHttp;
import org.tmforum.mtop.sa.xsd.scai.v1.ReserveRequest;
import org.tmforum.mtop.sa.xsd.scai.v1.ReserveResponse;

import com.google.common.base.Throwables;

@Service
public class ServiceComponentActivationClient {

  private static final String WSDL_LOCATION =
    "/mtosi/2.1/DDPs/ServiceActivation/IIS/wsdl/ServiceComponentActivationInterface/ServiceComponentActivationInterfaceHttp.wsdl";

  private final String endPoint;
  private final ServiceComponentActivationInterfaceHttp client;

  @Autowired
  public ServiceComponentActivationClient(@Value("${nbi.mtosi.service.reserve.endpoint}") String endPoint) {
    this.endPoint = endPoint;
    URL wsdlUrl = this.getClass().getResource(WSDL_LOCATION);
    this.client = new ServiceComponentActivationInterfaceHttp(
      wsdlUrl,
      new QName("http://www.tmforum.org/mtop/sa/wsdl/scai/v1-0", "ServiceComponentActivationInterfaceHttp"));
  }

  public void reserve(Reservation reservation, boolean autoProvision) {
    Holder<Header> header = HeaderBuilder.buildReserveHeader(endPoint);
    ReserveRequest reserveRequest = new ReserveRequestBuilder().createReservationRequest(reservation, autoProvision);

    try {
      ServiceComponentActivationInterface proxy = client.getServiceComponentActivationInterfaceSoapHttp();
      ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);

      ReserveResponse reserveResponse = proxy.reserve(header,
          reserveRequest);
      System.err.println("---->>");
      System.err.println(reserveResponse);
      System.err.println("<<----");
      System.err.println(reserveResponse.getRfsNameOrRfsCreation());
      // TODO do something with the reserveResponse..
    }
    catch (ReserveException e) {
      e.printStackTrace();
      Throwables.propagate(e);
    }
  }

}
