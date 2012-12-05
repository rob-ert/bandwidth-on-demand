package nl.surfnet.bod.nbi;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.Test;
import org.tmforum.mtop.sa.xsd.scai.v1.ReserveRequest;

public class MtosiNbiClientTest {

  private final MtosiNbiClient mtosiNbiClient = new MtosiNbiClient(false);

  static {
    System.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, "true");
    System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");
    System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
    System.setProperty("com.sun.xml.ws.util.pipe.StandaloneTubeAssembler.dump", "true");
    System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
  }

  @Test
  public void shouldProduceString() throws JAXBException {
    Reservation reservation = new ReservationFactory().create();
    reservation.getSourcePort().getPhysicalPort().setNmsSapName("sourceNmsSapName");
    reservation.getDestinationPort().getPhysicalPort().setNmsSapName("destinationNmsSapName");    
    reservation.getSourcePort().getPhysicalPort().setNmsNeId("sourceNmsNeId");
    reservation.getDestinationPort().getPhysicalPort().setNmsNeId("sourceNmsNeId");

    ReserveRequest reserveRequest = mtosiNbiClient.createReservationRequest(reservation, false);

    final JAXBContext context = JAXBContext.newInstance(ReserveRequest.class);

    final Marshaller marshaller = context.createMarshaller();

    // Create a stringWriter to hold the XML
    final StringWriter stringWriter = new StringWriter();
    marshaller.marshal(reserveRequest, stringWriter);

    System.out.println(stringWriter);
  }
}
