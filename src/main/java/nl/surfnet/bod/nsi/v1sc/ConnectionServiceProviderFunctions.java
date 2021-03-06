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

import static com.google.common.base.Optional.fromNullable;
import static nl.surfnet.bod.util.XmlUtils.xmlCalendarToDateTime;
import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.INITIAL;

import java.util.Date;
import java.util.List;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import nl.surfnet.bod.domain.ConnectionV1;
import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.util.XmlUtils;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.joda.time.DateTime;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
import org.springframework.util.StringUtils;

public final class ConnectionServiceProviderFunctions {

  public static final Function<ConnectionV1, GenericConfirmedType> CONNECTION_TO_GENERIC_CONFIRMED =
    new Function<ConnectionV1, GenericConfirmedType>() {
      @Override
      public GenericConfirmedType apply(ConnectionV1 connection) {
        return new GenericConfirmedType()
          .withProviderNSA(connection.getProviderNsa())
          .withRequesterNSA(connection.getRequesterNsa())
          .withConnectionId(connection.getConnectionId())
          .withGlobalReservationId(connection.getGlobalReservationId());
      }
    };

  public static final Function<ReserveRequestType, ConnectionV1> reserveRequestToConnection(final NsiHelper nsiHelper, final ProtectionType defaultProtectionType) {
    return new Function<ReserveRequestType, ConnectionV1>() {
      @Override
      public ConnectionV1 apply(ReserveRequestType reserveRequestType) {

        ReservationInfoType reservation = reserveRequestType.getReserve().getReservation();

        ConnectionV1 connection = new ConnectionV1();
        connection.setCurrentState(INITIAL);
        connection.setConnectionId(reservation.getConnectionId());
        connection.setDescription(reservation.getDescription());

        ServiceParametersType serviceParameters = reservation.getServiceParameters();

        Optional<DateTime> startTime = fromNullable(serviceParameters.getSchedule().getStartTime()).transform(xmlCalendarToDateTime);
        connection.setStartTime(startTime.orNull());

        Optional<DateTime> endTime = calculateEndTime(
          serviceParameters.getSchedule().getEndTime(),
          serviceParameters.getSchedule().getDuration(),
          startTime);
        connection.setEndTime(endTime.orNull());

        // Ignoring the max. and min. bandwidth attributes...
        connection.setDesiredBandwidth(serviceParameters.getBandwidth().getDesired());
        connection.setProtectionType(getProtectionType(serviceParameters));

        connection.setSourceStpId(reservation.getPath().getSourceSTP().getStpId());
        connection.setDestinationStpId(reservation.getPath().getDestSTP().getStpId());
        connection.setProviderNsa(reserveRequestType.getReserve().getProviderNSA());
        connection.setRequesterNsa(reserveRequestType.getReserve().getRequesterNSA());

        String globalReservationId = reservation.getGlobalReservationId();
        if (!StringUtils.hasText(globalReservationId)) {
          globalReservationId = nsiHelper.generateGlobalReservationId();
        }
        connection.setGlobalReservationId(globalReservationId);

        // store the path and service parameters, needed to send back the
        // response...
        connection.setPath(reservation.getPath());
        connection.setServiceParameters(serviceParameters);

        return connection;
      }

      private ProtectionType getProtectionType(ServiceParametersType serviceParameters) {
        if (guaranteedAttributesAreSpecified(serviceParameters)) {

          List<Object> guaranteeds = serviceParameters.getServiceAttributes().getGuaranteed().getAttributeOrEncryptedAttribute();

          // only supported 1 guaranteed attribute namely 'sNCP' with values of ProtectionType enum
          if (guaranteeds.size() == 1 && guaranteeds.get(0) instanceof AttributeType) {
            AttributeType protectedAttr = (AttributeType) guaranteeds.get(0);

            if (protectedAttr.getName().equals("sNCP")
              && protectedAttr.getAttributeValue().size() == 1
              && protectedAttr.getAttributeValue().get(0) instanceof String) {
              return ProtectionType.valueOf(((String) protectedAttr.getAttributeValue().get(0)).trim().toUpperCase());
            }
          }
        }

        return defaultProtectionType;
      }

      private boolean guaranteedAttributesAreSpecified(ServiceParametersType serviceParameters) {
        return serviceParameters.getServiceAttributes() != null && serviceParameters.getServiceAttributes().getGuaranteed() != null;
      }

      private Optional<DateTime> calculateEndTime(XMLGregorianCalendar endTimeCalendar, Duration duration, Optional<DateTime> startTime) {
        if (endTimeCalendar != null) {
          return Optional.of(XmlUtils.toDateTime(endTimeCalendar));
        }

        if (duration != null && startTime.isPresent()) {
          Date endTime = new Date(startTime.get().getMillis());
          duration.addTo(endTime);
          // Use timezone of start
          return Optional.of(new DateTime(endTime, startTime.get().getZone()));
        }

        return Optional.absent();
      }

    };
  }

  private ConnectionServiceProviderFunctions() {
  }

}
