package nl.surfnet.bod.service;

import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.*;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.joda.time.LocalDateTime;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort;
import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionServiceRequester;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryDetailsResultType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nsi.ws.ConnectionServiceProviderConstants;
import nl.surfnet.bod.repo.ConnectionRepo;
import nl.surfnet.bod.web.security.RichUserDetails;

@Service
public class ConnectionServiceProviderService {

  private final Logger log = LoggerFactory.getLogger(ConnectionServiceProviderService.class);

  @Autowired
  private ConnectionRepo connectionRepo;

  @Autowired
  private ReservationService reservationService;

  @Autowired
  private VirtualPortService virtualPortService;

  public static final Function<NsiRequestDetails, ConnectionRequesterPort> NSI_REQUEST_TO_CONNECTION_REQUESTER = new Function<NsiRequestDetails, ConnectionRequesterPort>() {
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

  private final Function<Connection, Reservation> CONNECTION_TO_RESERVATION = new Function<Connection, Reservation>() {
    @Override
    public Reservation apply(final Connection connection) {

      final VirtualPort sourcePort = virtualPortService.findByNsiStpId(connection.getSourceStpId());
      final VirtualPort destinationPort = virtualPortService.findByNsiStpId(connection.getDestinationStpId());

      final Reservation reservation = new Reservation();
      reservation.setConnection(connection);
      reservation.setName(connection.getDescription());
      reservation.setStartDateTime(new LocalDateTime(connection.getStartTime()));
      reservation.setEndDateTime(new LocalDateTime(connection.getEndTime()));
      reservation.setSourcePort(sourcePort);
      reservation.setDestinationPort(destinationPort);
      reservation.setVirtualResourceGroup(sourcePort.getVirtualResourceGroup());
      reservation.setBandwidth(connection.getDesiredBandwidth());
      reservation.setUserCreated(connection.getRequesterNsa());

      return reservation;
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
      return ConnectionServiceProviderConstants.URN_GLOBAL_RESERVATION_ID + ":" + UUID.randomUUID();
    }

  };

  public Reservation createReservation(final Connection connection, NsiRequestDetails requestDetails,
      boolean autoProvision) {
    return reservationService.create(CONNECTION_TO_RESERVATION.apply(connection), autoProvision,
        Optional.of(requestDetails));
  }

  @Async
  public void terminate(final Connection connection, final String requesterNsa, final NsiRequestDetails requestDetails) {
    connection.setCurrentState(ConnectionStateType.TERMINATING);
    connectionRepo.save(connection);
    // TODO [AvD] make asyn, strange richUserDetails here...
    reservationService.cancel(
        connection.getReservation(),
        new RichUserDetails(requesterNsa, "", "", Collections.<UserGroup> emptyList(), ImmutableList.of(BodRole
            .createNocEngineer())));
  }

  @Async
  public void sendQueryConfirmed(final String correlationId, final QueryConfirmedType confirmedType,
      final ConnectionRequesterPort port) {
    try {
      port.queryConfirmed(new Holder<>(correlationId), confirmedType);
    }
    catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
      log.error("Error: ", e);
    }
  }

  @Async
  public void sendQueryFailed(final String correlationId, QueryFailedType failedType, ConnectionRequesterPort port) {
    try {
      port.queryFailed(new Holder<>(correlationId), failedType);
    }
    catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
      log.error("Error: ", e);
    }
  }

}
