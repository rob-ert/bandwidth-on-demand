package nl.surfnet.bod.service;

import static nl.surfnet.bod.nsi.ws.v1sc.ConnectionServiceProviderFunctions.NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.ws.Holder;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.nsi.ws.ConnectionServiceProviderErrorCodes;
import nl.surfnet.bod.nsi.ws.v1sc.ConnectionServiceProviderFunctions;
import nl.surfnet.bod.repo.ConnectionRepo;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.joda.time.LocalDateTime;
import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort;
import org.ogf.schemas.nsi._2011._10.connection.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

@Service
public class ConnectionServiceProviderService {

  private final Logger log = LoggerFactory.getLogger(ConnectionServiceProviderService.class);

  @Resource
  private ConnectionRepo connectionRepo;

  @Resource
  private ReservationService reservationService;

  @Resource
  private VirtualPortService virtualPortService;

  public Reservation createReservation(final Connection connection, NsiRequestDetails requestDetails,
      boolean autoProvision) {

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

    return reservationService.create(reservation, autoProvision, Optional.of(requestDetails));
  }

  public void provision(Connection connection, NsiRequestDetails requestDetails) {
    // TODO [AvD] check if connection is in correct state to receive a provision
    // request..
    // for now we always go to auto provision but this is only correct if the
    // state is reserved.
    // in case it is scheduled we should start the reservation (go to
    // provisioning) But this is not supported
    // by OpenDRAC right now??
    // If we are already in the provisioned state send back a confirm and we are
    // done..
    // Any other state we have to send back a provision failed...
    connection.setCurrentState(ConnectionStateType.AUTO_PROVISION);
    connection.setProvisionRequestDetails(requestDetails);
    connectionRepo.save(connection);

    reservationService.provision(connection.getReservation(), Optional.of(requestDetails));
  }

  @Async
  public void terminate(final Connection connection, final String requesterNsa, final NsiRequestDetails requestDetails) {
    connection.setCurrentState(ConnectionStateType.TERMINATING);
    connectionRepo.save(connection);

    reservationService.cancel(
        connection.getReservation(),
        new RichUserDetails(requesterNsa, "", "", Collections.<UserGroup> emptyList(), ImmutableList.of(BodRole
            .createNocEngineer())));
  }

  @Async
  public void query(NsiRequestDetails requestDetails, List<String> connectionIds, List<String> globalReservationIds,
      String providerNsa, final  String requesterNsa) {

    QueryConfirmedType confirmedType = new QueryConfirmedType();

    // no criteria, return all connections related to this requester nsa
    if (connectionIds.isEmpty() && globalReservationIds.isEmpty()) {
      for (Connection connection : connectionRepo.findByRequesterNsa(requesterNsa)) {
        addQueryResult(confirmedType, connection);
      }
    }
    else if (globalReservationIds.isEmpty()) {
      for (String connectionId : connectionIds) {
        addQueryResult(confirmedType, connectionRepo.findByConnectionId(connectionId));
      }
    }
    else {
      for (String globalReservationId : globalReservationIds) {
        addQueryResult(confirmedType, connectionRepo.findByGlobalReservationId(globalReservationId));
      }
    }

    ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
    try {
      port.queryConfirmed(new Holder<>(requestDetails.getCorrelationId()), confirmedType);
    }
    catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
      log.error("Error: ", e);
      final QueryFailedType failedType = new QueryFailedType();

      failedType.setProviderNSA(providerNsa);
      failedType.setRequesterNSA(requesterNsa);

      final ServiceExceptionType error = new ServiceExceptionType();
      error.setErrorId(ConnectionServiceProviderErrorCodes.CONNECTION.CONNECTION_ERROR.getId());
      failedType.setServiceException(error);
      sendQueryFailed(requestDetails.getCorrelationId(), failedType, port);
    }
  }

  private void addQueryResult(final QueryConfirmedType confirmedType, final Connection connection) {
    if (connection == null) {
      return;
    }

    for (final QueryDetailsResultType query : confirmedType.getReservationDetails()) {
      if (query.getGlobalReservationId().equals(connection.getGlobalReservationId())) {
        return;
      }
    }

    final QueryDetailsResultType queryResult = ConnectionServiceProviderFunctions.CONNECTION_TO_QUERY_RESULT.apply(connection);
    confirmedType.getReservationDetails().add(queryResult);
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
