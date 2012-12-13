package nl.surfnet.bod.nbi.mtosi;

import javax.xml.bind.Marshaller;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.Ignore;
import org.junit.Test;


public class ServiceComponentActivationClientTest {

  static {
    System.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, "true");
//    System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");
//    System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
//    System.setProperty("com.sun.xml.ws.util.pipe.StandaloneTubeAssembler.dump", "true");
//    System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
  }

  private final ServiceComponentActivationClient subject = new ServiceComponentActivationClient("http://62.190.191.48:9006/mtosi/sa/ServiceComponentActivationInterface");

  @Test
  @Ignore("Needs access to london server... is more like integration, but now only for testing..")
  public void reserve() {
    Reservation reservation = new ReservationFactory().create();
    subject.reserve(reservation, false);
  }
}
