package nl.surfnet.bod.support;

import nl.surfnet.bod.web.services.NsiConnectionService;

import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReserveType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;

public class NsiReservationFactory {

  private String correlationId = NsiConnectionService.getCorrelationId();
  private String nsaProviderUrn = "urn:ogf:network:nsa:netherlight";

  /**
   * @param nsaProviderUrn
   * @param correlationId
   * @return
   */
  public ReserveRequestType createReservation() {
    final ReserveRequestType reservationRequest = new ReserveRequestType();
    reservationRequest.setCorrelationId(this.correlationId);
    reservationRequest.setReplyTo("http://localhost:8082/bod/nsi/requester");
    final ReserveType reservationType = new ReserveType();
    reservationType.setProviderNSA(this.nsaProviderUrn);
    final ReservationInfoType reservationInfoType = new ReservationInfoType();
    final ServiceParametersType serviceParameters = new ServiceParametersType();
    serviceParameters.setBandwidth(null);
    serviceParameters.setSchedule(null);
    reservationInfoType.setServiceParameters(serviceParameters);
    reservationType.setReservation(reservationInfoType);
    reservationRequest.setReserve(reservationType);
    return reservationRequest;
  }

  public NsiReservationFactory setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
    return this;
  }

  public NsiReservationFactory setNsaProviderUrn(String nsaProviderUrn) {
    this.nsaProviderUrn = nsaProviderUrn;
    return this;
  }

}
