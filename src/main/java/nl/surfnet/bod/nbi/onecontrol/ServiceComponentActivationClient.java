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
package nl.surfnet.bod.nbi.onecontrol;

import static nl.surfnet.bod.nbi.onecontrol.HeaderBuilder.buildReserveHeader;
import static nl.surfnet.bod.nbi.onecontrol.MtosiUtils.createRfs;
import static nl.surfnet.bod.nbi.onecontrol.ReserveRequestBuilder.createReservationRequest;

import javax.xml.ws.BindingProvider;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tmforum.mtop.fmw.xsd.msg.v1.BaseExceptionMessageType;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ActivateException;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ReserveException;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ServiceComponentActivationInterface;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ServiceComponentActivationInterfaceHttp;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.TerminateException;
import org.tmforum.mtop.sa.xsd.scai.v1.ActivateRequest;
import org.tmforum.mtop.sa.xsd.scai.v1.ObjectFactory;
import org.tmforum.mtop.sa.xsd.scai.v1.ReserveRequest;
import org.tmforum.mtop.sa.xsd.scai.v1.ReserveResponse;
import org.tmforum.mtop.sa.xsd.scai.v1.TerminateRequest;

@Service
public class ServiceComponentActivationClient {

  private static final String WSDL_LOCATION = "/mtosi/2.1/DDPs/ServiceActivation/IIS/wsdl/ServiceComponentActivationInterface/ServiceComponentActivationInterfaceHttp.wsdl";

  private final Logger logger = LoggerFactory.getLogger(ServiceComponentActivationClient.class);

  private final String endPoint;

  @Autowired
  public ServiceComponentActivationClient(@Value("${nbi.onecontrol.service.reserve.endpoint}") String endPoint) {
    this.endPoint = endPoint;
  }

  public Reservation reserve(Reservation reservation, boolean autoProvision) {
    ServiceComponentActivationInterface port = createPort();

    ReserveRequest reserveRequest = createReservationRequest(reservation, autoProvision);
    try {
      ReserveResponse reserveResponse = port.reserve(buildReserveHeader(endPoint), reserveRequest);

      if (reserveResponse.getRfsNameOrRfsCreation().isEmpty()) {
        reservation.setStatus(ReservationStatus.FAILED);
      } else {
        // TODO is this really true, should we not wait on a serviceObjectCreation notification
        reservation.setStatus(ReservationStatus.RESERVED);
      }
    } catch (ReserveException e) {
      logger.info("Reserve request failed", e);
      // FIXME db should allow bigger failed reasons..
      reservation.setFailedReason(StringUtils.abbreviate(e.getCause().getMessage(), 255));
      reservation.setStatus(ReservationStatus.NOT_ACCEPTED);
    }

    return reservation;
  }

  public boolean activate(Reservation reservation) {
    ServiceComponentActivationInterface port = createPort();

    ActivateRequest activateRequest = new ObjectFactory().createActivateRequest();
    activateRequest.setRfsName(createRfs(reservation.getReservationId()));

    try {
      port.activate(buildReserveHeader(endPoint), activateRequest);
      // TODO something with the response..
      //response.getRfsNameOrRfsCreationOrRfsStateChange()
      return true;
    } catch (ActivateException e) {
      BaseExceptionMessageType baseExceptionMessage = MtosiUtils.getBaseExceptionMessage(e);
      logger.warn("Could not activate reservation {} because {}", reservation, baseExceptionMessage.getReason());
      throw new AssertionError(baseExceptionMessage.getReason(), e);
    }
  }

  public void terminate(Reservation reservation) {
    ServiceComponentActivationInterface port = createPort();

    TerminateRequest terminateRequest = new ObjectFactory().createTerminateRequest();
    terminateRequest.setRfsName(createRfs(reservation.getReservationId()));
    try {
      port.terminate(buildReserveHeader(endPoint), terminateRequest);
    } catch (TerminateException e) {
      logger.info("Terminate failed", e);
      e.printStackTrace();
    }
  }

  private ServiceComponentActivationInterface createPort() {
    ServiceComponentActivationInterface port = new ServiceComponentActivationInterfaceHttp(this.getClass().getResource(WSDL_LOCATION)).getServiceComponentActivationInterfaceSoapHttp();
    ((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);

    return port;
  }

}