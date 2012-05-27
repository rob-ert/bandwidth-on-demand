package nl.surfnet.bod.web.services;

import static junit.framework.Assert.*;

import java.util.UUID;

import javax.annotation.Resource;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import nl.surfnet.bod.support.MockHttpServer;
import nl.surfnet.bod.support.NsiReservationFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogf.schemas.nsi._2011._07.connection._interface.GenericAcknowledgmentType;
import org.ogf.schemas.nsi._2011._07.connection._interface.ReservationRequestType;
import org.ogf.schemas.nsi._2011._07.connection.provider.NSIServiceException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-test.xml",
    "/spring/appCtx-nbi-client.xml", "/spring/appCtx-idd-client.xml" })
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
public class NsiConnectionServiceProviderTest extends AbstractTransactionalJUnit4SpringContextTests {

  @Resource(name = "nsiProvider")
  private NsiConnectionServiceProvider nsiProvider;

  private final String correationId = "urn:uuid:f32cc82e-4d87-45ab-baab-4b7011652a2e";

  private static MockHttpServer requesterEndpoint = new MockHttpServer(NsiReservationFactory.PORT);

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    requesterEndpoint.addResponse("/bod/nsi/requester", new ClassPathResource(
        "web/services/nsi/mockNsiReservationFailedResponse.xml"));
    requesterEndpoint.startServer();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    requesterEndpoint.stopServer();
  }

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test(expected = NSIServiceException.class)
  public void should_throw_exeption_because_of_null_reservervation() throws NSIServiceException {
    nsiProvider.reservation(null);
  }

  @Test(expected = NSIServiceException.class)
  public void should_throw_exeption_because_of_invalid_provider_urn() throws NSIServiceException {
    final ReservationRequestType reservationRequest = new NsiReservationFactory().setNsaProviderUrn(
        "urn:ogf:network:nsa:no:such:provider").createReservation();
    nsiProvider.reservation(reservationRequest);
  }

  @Test(expected = NSIServiceException.class)
  public void should_throw_exeption_because_of_invalid_correlation_id() throws NSIServiceException {
    final ReservationRequestType reservationRequest = new NsiReservationFactory().setCorrelationId(
        UUID.randomUUID().toString()).createReservation();
    nsiProvider.reservation(reservationRequest);
  }

  @Test
  public void should_return_generic_acknowledgement_and_send_reservation_failed() throws Exception {

    final int requesterCountBefore = requesterEndpoint.getCallCounter();
    assertEquals(0, requesterCountBefore);

    final XMLGregorianCalendar startTime = DatatypeFactory.newInstance().newXMLGregorianCalendar();
    startTime.setDay(10);
    startTime.setMonth(10);
    startTime.setYear(2012);
    startTime.setMinute(0);
    startTime.setHour(0);
    startTime.setSecond(0);

    final XMLGregorianCalendar endTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(
        startTime.toGregorianCalendar());
    endTime.setDay(startTime.getDay() + 5);

    final ReservationRequestType reservationRequest = new NsiReservationFactory().setScheduleStartTime(startTime)
        .setScheduleEndTime(endTime).setCorrelationId(correationId).createReservation();

    final GenericAcknowledgmentType genericAcknowledgmentType = nsiProvider.reservation(reservationRequest);
    assertEquals(reservationRequest.getCorrelationId(), genericAcknowledgmentType.getCorrelationId());
    
    final String lastRequest = requesterEndpoint.getOrWaitForLastRequest(5);
    
    assertTrue(lastRequest.contains(correationId));
    assertTrue(lastRequest.contains("reservationFailed"));
    
    assertEquals(requesterCountBefore + 1, requesterEndpoint.getCallCounter());
  }
}
