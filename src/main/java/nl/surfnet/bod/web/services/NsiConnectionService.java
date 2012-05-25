package nl.surfnet.bod.web.services;

import java.util.UUID;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NsiConnectionService {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private static final String URN_UUID = "urn:uuid:";

  /*
   * This holds the web service request context which includes all the original
   * HTTP information, including the JAAS authentication and authorisation
   * information.
   */
  @Resource
  private WebServiceContext webServiceContext;

  protected Logger getLog() {
    return log;
  }

  protected final WebServiceContext getWebServiceContext() {
    return webServiceContext;
  }

  protected boolean isValidCorrelationId(final String correlationId) {
    return correlationId == null ? false : correlationId.startsWith(URN_UUID);
  }

  public static String getCorrelationId() {
    return URN_UUID + UUID.randomUUID().toString();
  }

  static {
    System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");
  }

}
