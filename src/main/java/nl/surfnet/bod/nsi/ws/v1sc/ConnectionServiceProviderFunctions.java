package nl.surfnet.bod.nsi.ws.v1sc;

import static nl.surfnet.bod.nsi.ws.ConnectionServiceProvider.*;
import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.*;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort;
import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionServiceRequester;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryDetailsResultType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import com.google.common.base.Function;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.NsiRequestDetails;

public class ConnectionServiceProviderFunctions {

  public static final Function<NsiRequestDetails, ConnectionRequesterPort> NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT = new Function<NsiRequestDetails, ConnectionRequesterPort>() {
    @Override
    public ConnectionRequesterPort apply(final NsiRequestDetails requestDetails) {
      URL url;
      try {
        url = new ClassPathResource("/wsdl/nsi/ogf_nsi_connection_requester_v1_0.wsdl").getURL();
      }
      catch (IOException e) {
        throw new RuntimeException("Could not find the requester wsdl", e);
      }
      final ConnectionRequesterPort port = new ConnectionServiceRequester(url, new QName(
          "http://schemas.ogf.org/nsi/2011/10/connection/requester", "ConnectionServiceRequester"))
          .getConnectionServiceRequesterPort();

      final Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
      requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, requestDetails.getReplyTo());
      return port;
    }
  };

  public static final Function<Connection, GenericFailedType> CONNECTION_TO_GENERIC_FAILED = new Function<Connection, GenericFailedType>() {
    @Override
    public GenericFailedType apply(final Connection connection) {
      final GenericFailedType generic = new GenericFailedType();
      generic.setProviderNSA(connection.getProviderNsa());
      generic.setRequesterNSA(connection.getRequesterNsa());
      generic.setConnectionId(connection.getConnectionId());
      generic.setGlobalReservationId(connection.getGlobalReservationId());
      generic.setConnectionState(connection.getCurrentState());
      return generic;
    }
  };

  public static final Function<Connection, GenericConfirmedType> CONNECTION_TO_GENERIC_CONFIRMED = new Function<Connection, GenericConfirmedType>() {
    @Override
    public GenericConfirmedType apply(final Connection connection) {
      final GenericConfirmedType generic = new GenericConfirmedType();
      generic.setProviderNSA(connection.getProviderNsa());
      generic.setRequesterNSA(connection.getRequesterNsa());
      generic.setConnectionId(connection.getConnectionId());
      generic.setGlobalReservationId(connection.getGlobalReservationId());
      return generic;
    }
  };

  public static final Function<Connection, QueryDetailsResultType> CONNECTION_TO_QUERY_RESULT = new Function<Connection, QueryDetailsResultType>() {
    @Override
    public QueryDetailsResultType apply(final Connection connection) {
      final QueryDetailsResultType queryDetailsResultType = new QueryDetailsResultType();
      queryDetailsResultType.setConnectionId(connection.getConnectionId());

      // RH: We don't have a description......
      // queryDetailsResultType.setDescription("description");
      queryDetailsResultType.setGlobalReservationId(connection.getGlobalReservationId());
      queryDetailsResultType.setServiceParameters(connection.getServiceParameters());
      return queryDetailsResultType;
    }
  };

  public static final Function<ReserveRequestType, Connection> RESERVE_REQUEST_TO_CONNECTION = new Function<ReserveRequestType, Connection>() {
    @Override
    public Connection apply(final ReserveRequestType reserveRequestType) {

      final ReservationInfoType reservation = reserveRequestType.getReserve().getReservation();

      final Connection connection = new Connection();
      connection.setCurrentState(INITIAL);
      connection.setConnectionId(reservation.getConnectionId());
      connection.setDescription(reservation.getDescription());
      connection.setStartTime(getDateFrom(reservation.getServiceParameters().getSchedule().getStartTime()));
      connection.setEndTime(getDateFrom(reservation.getServiceParameters().getSchedule().getEndTime()));

      // TODO [AvD] end time is optional could also set duration...
      // reserveRequestType.getReserve().getReservation().getServiceParameters().getSchedule().getDuration();

      // Ignoring the max. and min. bandwidth attributes...
      connection.setDesiredBandwidth(reservation.getServiceParameters().getBandwidth().getDesired());
      connection.setSourceStpId(reservation.getPath().getSourceSTP().getStpId());
      connection.setDestinationStpId(reservation.getPath().getDestSTP().getStpId());
      connection.setProviderNsa(reserveRequestType.getReserve().getProviderNSA());
      connection.setRequesterNsa(reserveRequestType.getReserve().getRequesterNSA());

      String globalReservationId = reservation.getGlobalReservationId();
      if (!StringUtils.hasText(globalReservationId)) {
        globalReservationId = generateGlobalId();
      }
      connection.setGlobalReservationId(globalReservationId);

      // store the path and service parameters, needed to send back the
      // response...
      connection.setPath(reservation.getPath());
      connection.setServiceParameters(reservation.getServiceParameters());

      return connection;
    }

    private Date getDateFrom(XMLGregorianCalendar calendar) {
      return calendar.toGregorianCalendar().getTime();
    }

    private String generateGlobalId() {
      return URN_GLOBAL_RESERVATION_ID + ":" + UUID.randomUUID();
    }

  };

}
