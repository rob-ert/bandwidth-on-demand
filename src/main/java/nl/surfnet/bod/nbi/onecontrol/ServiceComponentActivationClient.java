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

import static nl.surfnet.bod.nbi.onecontrol.HeaderBuilder.buildActivateHeader;
import static nl.surfnet.bod.nbi.onecontrol.HeaderBuilder.buildReserveHeader;
import static nl.surfnet.bod.nbi.onecontrol.HeaderBuilder.buildTerminateHeader;
import static nl.surfnet.bod.nbi.onecontrol.MtosiUtils.createRfs;
import static nl.surfnet.bod.nbi.onecontrol.ReserveRequestBuilder.createReservationRequest;

import javax.annotation.Resource;
import javax.xml.ws.BindingProvider;

import com.google.common.base.Optional;
import com.sun.xml.ws.developer.JAXWSProperties;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.UpdatedReservationStatus;
import nl.surfnet.bod.nbi.onecontrol.OneControlInstance.OneControlConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
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

@Profile("onecontrol")
@Service
public class ServiceComponentActivationClient {

  private static final String WSDL_LOCATION = "/mtosi/2.1/DDPs/ServiceActivation/IIS/wsdl/ServiceComponentActivationInterface/ServiceComponentActivationInterfaceHttp.wsdl";

  private final Logger logger = LoggerFactory.getLogger(ServiceComponentActivationClient.class);

  @Resource private OneControlInstance oneControlInstance;

  @Value("${onecontrol.service.component.activation.connect.timeout}")
  private int connectTimeout;

  @Value("${onecontrol.service.component.activation.request.timeout}")
  private int requestTimeout;

  public UpdatedReservationStatus reserve(Reservation reservation) {
    OneControlConfiguration configuration = oneControlInstance.getCurrentConfiguration();
    ServiceComponentActivationInterface port = createPort(configuration);

    ReserveRequest reserveRequest = createReservationRequest(reservation);
    try {
      ReserveResponse reserveResponse = port.reserve(buildReserveHeader(configuration), reserveRequest);

      if (reserveResponse.getRfsNameOrRfsCreation().isEmpty()) {
        logger.warn("No RFS name in reserve response {} for reservation request {}", reserveResponse, reserveRequest);
        return UpdatedReservationStatus.failed("OneControl/MTOSI: No RFS name in reserve response");
      }
    } catch (ReserveException e) {
      logger.info("Reserve request " + reservation.getName() +  " failed: " + e + " with fault info " + e.getFaultInfo(), e);
      return UpdatedReservationStatus.notAccepted("reserve operation failed with error '" + e + "'");
    }

    return UpdatedReservationStatus.forNewStatus(ReservationStatus.REQUESTED);
  }

  public boolean activate(Reservation reservation) {
    OneControlConfiguration configuration = oneControlInstance.getCurrentConfiguration();
    ServiceComponentActivationInterface port = createPort(configuration);

    ActivateRequest activateRequest = new ObjectFactory().createActivateRequest();
    activateRequest.setRfsName(createRfs(reservation.getReservationId()));

    try {
      port.activate(buildActivateHeader(configuration), activateRequest);
      // TODO something with the response..
      //response.getRfsNameOrRfsCreationOrRfsStateChange()
      return true;
    } catch (ActivateException e) {
      BaseExceptionMessageType baseExceptionMessage = MtosiUtils.getBaseExceptionMessage(e);
      logger.warn("Could not activate reservation {} because {}", reservation, baseExceptionMessage.getReason(), e);
      throw new AssertionError(baseExceptionMessage.getReason(), e);
    }
  }

  public Optional<String> terminate(Reservation reservation) {
    OneControlConfiguration configuration = oneControlInstance.getCurrentConfiguration();
    ServiceComponentActivationInterface port = createPort(configuration);

    TerminateRequest terminateRequest = new ObjectFactory().createTerminateRequest()
      .withRfsName(createRfs(reservation.getReservationId()));
    try {
      port.terminate(buildTerminateHeader(configuration), terminateRequest);
      return Optional.absent();
    } catch (TerminateException e) {
      logger.info("Terminate failed: " + e, e);
      return Optional.of(e.toString());
    }
  }

  private ServiceComponentActivationInterface createPort(OneControlConfiguration configuration) {
    ServiceComponentActivationInterface port = new ServiceComponentActivationInterfaceHttp(this.getClass().getResource(WSDL_LOCATION)).getServiceComponentActivationInterfaceSoapHttp();
    BindingProvider bindingProvider = (BindingProvider) port;
    bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, configuration.getServiceReserveEndpoint());
    bindingProvider.getRequestContext().put(JAXWSProperties.CONNECT_TIMEOUT, connectTimeout);
    bindingProvider.getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, requestTimeout);

    return port;
  }

}
