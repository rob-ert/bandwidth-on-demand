package nl.surfnet.bod.service;

import java.util.Collections;

import javax.annotation.Resource;
import javax.xml.ws.Holder;

import org.joda.time.LocalDateTime;
import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceExceptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nsi.ws.ConnectionServiceProviderErrorCodes;
import nl.surfnet.bod.repo.ConnectionRepo;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.security.RichUserDetails;

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
    // TODO [AvD] make asyn, strange richUserDetails here...
    reservationService.cancel(
        connection.getReservation(),
        new RichUserDetails(requesterNsa, "", "", Collections.<UserGroup> emptyList(), ImmutableList.of(BodRole
            .createNocEngineer())));
  }

  @Async
  public void sendQueryConfirmed(final String correlationId, final QueryConfirmedType confirmedType,
      final ConnectionRequesterPort port, final String providerNsa,final  String requesterNsa) {
    try {
      port.queryConfirmed(new Holder<>(correlationId), confirmedType);
    }
    catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
      log.error("Error: ", e);
      final QueryFailedType failedType = new QueryFailedType();
      
      failedType.setProviderNSA(providerNsa);
      failedType.setRequesterNSA(requesterNsa);
      
      final ServiceExceptionType error = new ServiceExceptionType();
      error.setErrorId(ConnectionServiceProviderErrorCodes.CONNECTION.CONNECTION_ERROR.getId());
      failedType.setServiceException(error );
      sendQueryFailed(correlationId, failedType , port);
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
