package nl.surfnet.bod.support;

import javax.xml.datatype.XMLGregorianCalendar;

import nl.surfnet.bod.web.services.NsiConnectionService;

import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.BandwidthType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReserveType;
import org.ogf.schemas.nsi._2011._10.connection.types.ScheduleType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;

public class NsiReservationFactory {

  public static final String NSI_REQUESTER_ENDPOINT = "http://localhost:9082/bod/nsi/requester";
  private String correlationId = NsiConnectionService.getCorrelationId();
  private String nsaProviderUrn = "urn:ogf:network:nsa:netherlight";
  private int desiredBandwidth = 1000;
  private int maxBandwidth = 1000;
  private int minBandwidth = 950;
  private XMLGregorianCalendar scheduleEndTime;
  private XMLGregorianCalendar scheduleStartTime;

  /**
   * @param nsaProviderUrn
   * @param correlationId
   * @return
   */
  public ReserveRequestType createReservation() {
    final ReserveRequestType reservationRequest = new ReserveRequestType();
    reservationRequest.setCorrelationId(this.correlationId);
    reservationRequest.setReplyTo(NSI_REQUESTER_ENDPOINT);

    final ReserveType reservationType = new ReserveType();
    reservationType.setProviderNSA(this.nsaProviderUrn);

    final ReservationInfoType reservationInfoType = new ReservationInfoType();
    final ServiceParametersType serviceParameters = new ServiceParametersType();

    final BandwidthType bandwidth = new BandwidthType();
    bandwidth.setDesired(desiredBandwidth);
    bandwidth.setMaximum(maxBandwidth);
    bandwidth.setMinimum(minBandwidth);
    serviceParameters.setBandwidth(bandwidth);

    final ScheduleType schedule = new ScheduleType();
    schedule.setDuration(null);
    schedule.setEndTime(scheduleEndTime);
    schedule.setStartTime(scheduleStartTime);
    serviceParameters.setSchedule(schedule);

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

  public final NsiReservationFactory setDesiredBandwidth(int desiredBandwidth) {
    this.desiredBandwidth = desiredBandwidth;
    return this;
  }

  public final NsiReservationFactory setMaxBandwidth(int maxBandwidth) {
    this.maxBandwidth = maxBandwidth;
    return this;
  }

  public final NsiReservationFactory setMinBandwidth(int minBandwidth) {
    this.minBandwidth = minBandwidth;
    return this;
  }

}
