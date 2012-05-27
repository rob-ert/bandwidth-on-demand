package nl.surfnet.bod.support;

import java.math.BigInteger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import nl.surfnet.bod.web.services.NsiConnectionService;

import org.ogf.schemas.nsi._2011._07.connection._interface.ReservationRequestType;
import org.ogf.schemas.nsi._2011._07.connection.types.BandwidthType;
import org.ogf.schemas.nsi._2011._07.connection.types.ReservationInfoType;
import org.ogf.schemas.nsi._2011._07.connection.types.ReservationType;
import org.ogf.schemas.nsi._2011._07.connection.types.ScheduleType;
import org.ogf.schemas.nsi._2011._07.connection.types.ServiceParametersType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NsiReservationFactory {

  public static final int PORT = 9082;

  public static final String NSI_REQUESTER_ENDPOINT = "http://localhost:"+PORT+"/bod/nsi/requester";

  private final Logger log = LoggerFactory.getLogger(getClass());

  private String correlationId = NsiConnectionService.getCorrelationId();
  private String connectionId = NsiConnectionService.getCorrelationId();
  private String nsaProviderUrn = "urn:ogf:network:nsa:netherlight";
  private long desiredBandwidth = 1000;
  private long maxBandwidth = 1000;
  private long minBandwidth = 950;
  private XMLGregorianCalendar scheduleEndTime;
  private XMLGregorianCalendar scheduleStartTime;

  /**
   * @param nsaProviderUrn
   * @param correlationId
   * @return
   */
  public ReservationRequestType createReservation() {

    final BandwidthType bandwidthType = new BandwidthType();
    bandwidthType.setDesired(BigInteger.valueOf(desiredBandwidth));
    bandwidthType.setMaximum(BigInteger.valueOf(maxBandwidth));
    bandwidthType.setMinimum(BigInteger.valueOf(minBandwidth));

    final ScheduleType scheduleType = new ScheduleType();
    scheduleType.setEndTime(scheduleEndTime);
    scheduleType.setStartTime(scheduleStartTime);

    if (scheduleType.getEndTime() != null && scheduleType.getStartTime() != null) {
      try {
        scheduleType.setDuration(DatatypeFactory.newInstance().newDuration(
            scheduleType.getEndTime().getMillisecond() - scheduleType.getStartTime().getMillisecond()));
      }
      catch (DatatypeConfigurationException e) {
        log.error("Error: ", e);
      }
    }

    final ServiceParametersType serviceParametersType = new ServiceParametersType();
    serviceParametersType.setBandwidth(bandwidthType);
    serviceParametersType.setSchedule(scheduleType);

    final ReservationInfoType reservationInfoType = new ReservationInfoType();
    reservationInfoType.setServiceParameters(serviceParametersType);
    reservationInfoType.setConnectionId(connectionId);
    reservationInfoType.setGlobalReservationId(correlationId);
    

    final ReservationType reservationType = new ReservationType();
    reservationType.setProviderNSA(nsaProviderUrn);
    reservationType.setReservation(reservationInfoType);

    final ReservationRequestType reservationRequestType = new ReservationRequestType();
    reservationRequestType.setCorrelationId(this.correlationId);
    reservationRequestType.setReplyTo(NSI_REQUESTER_ENDPOINT);
    reservationRequestType.setReservation(reservationType);

    return reservationRequestType;
  }

  public final NsiReservationFactory setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
    return this;
  }

  public final NsiReservationFactory setNsaProviderUrn(String nsaProviderUrn) {
    this.nsaProviderUrn = nsaProviderUrn;
    return this;
  }

  public final NsiReservationFactory setDesiredBandwidth(long desiredBandwidth) {
    this.desiredBandwidth = desiredBandwidth;
    return this;
  }

  public final NsiReservationFactory setMaxBandwidth(long maxBandwidth) {
    this.maxBandwidth = maxBandwidth;
    return this;
  }

  public final NsiReservationFactory setMinBandwidth(long minBandwidth) {
    this.minBandwidth = minBandwidth;
    return this;
  }

  public final NsiReservationFactory setScheduleEndTime(XMLGregorianCalendar scheduleEndTime) {
    this.scheduleEndTime = scheduleEndTime;
    return this;
  }

  public final NsiReservationFactory setScheduleStartTime(XMLGregorianCalendar scheduleStartTime) {
    this.scheduleStartTime = scheduleStartTime;
    return this;
  }

  public final NsiReservationFactory setConnectionId(String connectionId) {
    this.connectionId = connectionId;
    return this;
  }

}
