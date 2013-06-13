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
package nl.surfnet.bod.nbi.mtosi;

import static nl.surfnet.bod.nbi.mtosi.MtosiUtils.createRfs;

import javax.annotation.Resource;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.msg.v1.BaseExceptionMessageType;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.*;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ActivateException;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ReserveException;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.TerminateException;
import org.tmforum.mtop.sa.xsd.scai.v1.*;

import com.google.common.annotations.VisibleForTesting;

@Service
public class ServiceComponentActivationClient {

  private static final String WSDL_LOCATION = "/mtosi/2.1/DDPs/ServiceActivation/IIS/wsdl/ServiceComponentActivationInterface/ServiceComponentActivationInterfaceHttp.wsdl";
  private static final String NAMESPACE_URI = "http://www.tmforum.org/mtop/sa/wsdl/scai/v1-0";
  private static final String LOCAL_PART = "ServiceComponentActivationInterfaceHttp";

  private final Logger logger = LoggerFactory.getLogger(ServiceComponentActivationClient.class);

  private final ServiceComponentActivationInterface proxy;
  private final Holder<Header> header;
  private final String endPoint;

  @Resource
  private DataFieldMaxValueIncrementer valueIncrementer;

  @Autowired
  public ServiceComponentActivationClient(@Value("${nbi.mtosi.service.reserve.endpoint}") String endPoint) {
    this.endPoint = endPoint;
    ServiceComponentActivationInterfaceHttp client = new ServiceComponentActivationInterfaceHttp(
      this.getClass().getResource(WSDL_LOCATION),
      new QName(NAMESPACE_URI, LOCAL_PART));
    this.proxy = client.getServiceComponentActivationInterfaceSoapHttp();
    this.header = HeaderBuilder.buildReserveHeader(endPoint);
  }

  public Reservation reserve(Reservation reservation, boolean autoProvision) {
    ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);

    ReserveRequest reserveRequest = new ReserveRequestBuilder()
      .createReservationRequest(reservation, autoProvision, valueIncrementer.nextLongValue());

    try {
      ReserveResponse reserveResponse = proxy.reserve(header, reserveRequest);

      handleInitialReservationStatus(reservation, reserveResponse);
    }
    catch (ReserveException e) {
      handleInitialReservationException(reservation, e);
    }

    return reservation;
  }

  public boolean activate(Reservation reservation) {
    ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);

    ActivateRequest activateRequest = new ObjectFactory().createActivateRequest();
    activateRequest.setRfsName(createRfs(reservation.getReservationId()));

    try {
      proxy.activate(header, activateRequest);
      // TODO something with the response..
      //response.getRfsNameOrRfsCreationOrRfsStateChange()
      return true;
    }
    catch (ActivateException e) {
      BaseExceptionMessageType baseExceptionMessage = MtosiUtils.getBaseExceptionMessage(e);
      logger.warn("Could not activate reservation {} because {}", reservation.getReservationId(), baseExceptionMessage.getReason());
      throw new AssertionError(baseExceptionMessage.getReason(), e);
    }
  }

  public void terminate(Reservation reservation) {
    ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);

    TerminateRequest terminateRequest = new ObjectFactory().createTerminateRequest();
    terminateRequest.setRfsName(createRfs(reservation.getReservationId()));

    try {
      proxy.terminate(header, terminateRequest);
    }
    catch (TerminateException e) {
      e.printStackTrace();
      throw new AssertionError(e);
    }
  }

  @VisibleForTesting
  void handleInitialReservationException(Reservation reservation, ReserveException cause) {
    reservation.setFailedReason(cause.getMessage());
    reservation.setStatus(ReservationStatus.NOT_ACCEPTED);
    logger.warn("Error creating reservation with id: " + reservation.getReservationId(), cause);
  }

  private void handleInitialReservationStatus(Reservation reservation, ReserveResponse reserveResponse) {
    if (CollectionUtils.isEmpty(reserveResponse.getRfsNameOrRfsCreation())) {
      reservation.setStatus(ReservationStatus.FAILED);
      return;
    }

    //NamingAttributeType namingAttr = (NamingAttributeType) rfsNameOrRfsCreation.get(0);

    reservation.setStatus(ReservationStatus.RESERVED);
  }

  protected void setValueIncrementer(DataFieldMaxValueIncrementer valueIncrementer) {
    this.valueIncrementer = valueIncrementer;
  }

}