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
package nl.surfnet.bod.nbi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.nbi.mtosi.MtosiInventoryRetrievalClient;
import nl.surfnet.bod.nbi.mtosi.ReserveRequestBuilder;
import nl.surfnet.bod.util.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationPatternType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationStyleType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.hdr.v1.MessageTypeType;
import org.tmforum.mtop.sa.wsdl.sai.v1_0.ServiceActivationInterface;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ReserveException;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ServiceComponentActivationInterfaceHttp;
import org.tmforum.mtop.sa.xsd.scai.v1.ReserveRequest;
import org.tmforum.mtop.sa.xsd.scai.v1.ReserveResponse;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;

public class NbiMtosiClient implements NbiClient {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource
  private Environment environment;

  @Resource
  private MtosiInventoryRetrievalClient inventoryRetrievalClient;

  private boolean shouldInit;

  private ServiceComponentActivationInterfaceHttp serviceComponentActivationInterfaceHttp;

  public NbiMtosiClient() {
    this(true);
  }

  @VisibleForTesting
  NbiMtosiClient(boolean shouldInit) {
    this.shouldInit = shouldInit;
    init();
  }

  @Override
  public boolean activateReservation(String reservationId) {
    throw new UnsupportedOperationException("Not implemented yet..");
  }

  @Override
  public ReservationStatus cancelReservation(String scheduleId) {
    throw new UnsupportedOperationException("Not implemented yet..");
  }

  @Override
  public long getPhysicalPortsCount() {
    return inventoryRetrievalClient.getUnallocatedMtosiPortCount();
  }

  @Override
  public Reservation createReservation(Reservation reservation, boolean autoProvision) {
    Holder<Header> header = createServiceActivationRequestHeaders();
    ReserveRequest reserveRequest = new ReserveRequestBuilder().createReservationRequest(reservation, autoProvision);

    try {
      ReserveResponse reserveResponse = serviceComponentActivationInterfaceHttp.getServiceComponentActivationInterfaceSoapHttp().reserve(header,
          reserveRequest);
      // TODO do something with the reserveResponse..
    }
    catch (ReserveException e) {
      e.printStackTrace();
      Throwables.propagate(e);
    }

    return reservation;
  }

  @Override
  public List<PhysicalPort> findAllPhysicalPorts() {
    return inventoryRetrievalClient.getUnallocatedPorts();
  }

  @Override
  public Optional<ReservationStatus> getReservationStatus(String scheduleId) {
    throw new UnsupportedOperationException("Not implemented yet..");
  }

  @Override
  public PhysicalPort findPhysicalPortByNmsPortId(String nmsPortId) {
    throw new UnsupportedOperationException("Not implemented yet..");
  }

  private void init() {
    if (shouldInit) {
      try {
        serviceComponentActivationInterfaceHttp = new ServiceComponentActivationInterfaceHttp(new URL(environment
            .getMtosiReserveEndPoint()), new QName("http://www.tmforum.org/mtop/sa/wsdl/scai/v1-0",
            "ServiceActivationInterfaceHttp"));

        final Map<String, Object> requestContext = ((BindingProvider) serviceComponentActivationInterfaceHttp
            .getPort(ServiceActivationInterface.class)).getRequestContext();

        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, environment.getMtosiReserveEndPoint());

        shouldInit = false;
      }
      catch (MalformedURLException e) {
        logger.error("Error: ", e);
        Throwables.propagate(e);
      }
    }
  }

  /**
   * <v1:header> <v1:destinationURI>http://localhost:9006/mtosi/sa/
   * ServiceComponentActivation</v1:destinationURI>
   * <v1:communicationStyle>RPC</v1:communicationStyle>
   * <v1:timestamp>2012-11-26T00:00:00.000-05:00</v1:timestamp>
   * <v1:activityName>reserve</v1:activityName>
   * <v1:msgName>reserveRequest</v1:msgName>
   * <v1:senderURI>http://localhost:9009</v1:senderURI>
   * <v1:msgType>REQUEST</v1:msgType>
   * <v1:communicationPattern>SimpleResponse</v1:communicationPattern>
   * </v1:header>
   *
   * @return
   */
  private Holder<Header> createServiceActivationRequestHeaders() {
    final Header header = new Header();
    header.setDestinationURI(environment.getMtosiReserveEndPoint());
    header.setCommunicationStyle(CommunicationStyleType.RPC);
    try {
      header.setTimestamp(DatatypeFactory.newInstance().newXMLGregorianCalendar());
    }
    catch (DatatypeConfigurationException e) {
      Throwables.propagate(e);
    }
    header.setActivityName("reserve");
    header.setMsgName("reserveRequest");
    header.setSenderURI("http://localhost:9009");
    header.setMsgType(MessageTypeType.REQUEST);
    header.setCommunicationPattern(CommunicationPatternType.SIMPLE_RESPONSE);

    return new Holder<Header>(header);
  }

//  private Holder<Header> getInventoryRequestHeaders() {
//    final Header header = new Header();
//    header.setDestinationURI(resourceInventoryRetrievalUrl);
//    header.setCommunicationPattern(CommunicationPatternType.SIMPLE_RESPONSE);
//    header.setCommunicationStyle(CommunicationStyleType.RPC);
//    header.setActivityName("getServiceInventory");
//    header.setMsgName("getServiceInventoryRequest");
//    header.setSenderURI(senderUri);
//    header.setMsgType(MessageTypeType.REQUEST);
//    return new Holder<Header>(header);
//  }

}
