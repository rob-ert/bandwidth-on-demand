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

  public static final String NSI_REQUESTER_ENDPOINT = "http://localhost:9082/bod/nsi/requester";

  private final Logger log = LoggerFactory.getLogger(getClass());

  private String correlationId = NsiConnectionService.getCorrelationId();
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
    final ReservationRequestType reservationRequest = new ReservationRequestType();
    reservationRequest.setCorrelationId(this.correlationId);
    reservationRequest.setReplyTo(NSI_REQUESTER_ENDPOINT);

    final ReservationType reservationType = new ReservationType();
    reservationType.setProviderNSA(this.nsaProviderUrn);

    final ReservationInfoType reservationInfoType = new ReservationInfoType();
    final ServiceParametersType serviceParameters = new ServiceParametersType();

    final BandwidthType bandwidth = new BandwidthType();
    bandwidth.setDesired(BigInteger.valueOf(desiredBandwidth));
    bandwidth.setMaximum(BigInteger.valueOf(maxBandwidth));
    bandwidth.setMinimum(BigInteger.valueOf(minBandwidth));
    serviceParameters.setBandwidth(bandwidth);

    final ScheduleType schedule = new ScheduleType();
    schedule.setEndTime(scheduleEndTime);
    schedule.setStartTime(scheduleStartTime);

    if (schedule.getEndTime() != null && schedule.getStartTime() != null) {
      try {
        schedule.setDuration(DatatypeFactory.newInstance().newDuration(
            schedule.getEndTime().getMillisecond() - schedule.getStartTime().getMillisecond()));
      }
      catch (DatatypeConfigurationException e) {
        log.error("Error: ", e);
      }
    }
    serviceParameters.setSchedule(schedule);
    reservationInfoType.setServiceParameters(serviceParameters);
    reservationType.setReservation(reservationInfoType);
    reservationRequest.setReservation(reservationType);
    return reservationRequest;
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

}
