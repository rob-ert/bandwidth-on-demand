package nl.surfnet.bod.nsi.ws;

import java.util.UUID;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import nl.surfnet.bod.service.ReservationService;

public abstract class ConnectionService {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private static final String URN_UUID = "urn:uuid:";

  /*
   * This holds the web service request context which includes all the original
   * HTTP information, including the JAAS authentication and authorisation
   * information.
   */
  @Resource
  private WebServiceContext webServiceContext;

  @Autowired
  private ReservationService reservationService;

  protected boolean isValidCorrelationId(final String correlationId) {
    return correlationId == null ? false : correlationId.startsWith(URN_UUID);
  }

  public static String getCorrelationId() {
    return URN_UUID + UUID.randomUUID().toString();
  }

  protected Logger getLog() {
    return log;
  }

  protected final WebServiceContext getWebServiceContext() {
    return webServiceContext;
  }

  protected ReservationService getReservationService() {
    return reservationService;
  }

  static {
    System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");
  }

}
