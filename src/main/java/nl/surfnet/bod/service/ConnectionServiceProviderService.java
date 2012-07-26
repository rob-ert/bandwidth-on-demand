package nl.surfnet.bod.service;

import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.*;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;

import org.joda.time.LocalDateTime;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  public final Function<Connection, Reservation> CONNECTION_TO_RESERVATION = new Function<Connection, Reservation>() {
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
