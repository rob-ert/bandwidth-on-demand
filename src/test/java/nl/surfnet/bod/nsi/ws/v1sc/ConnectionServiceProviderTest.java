package nl.surfnet.bod.nsi.ws.v1sc;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.UUID;

import javax.annotation.Resource;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import nl.surfnet.bod.support.MockHttpServer;
import nl.surfnet.bod.support.NsiReservationFactory;

import org.junit.*;
import org.junit.runner.RunWith;
import org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-test.xml",
    "/spring/appCtx-nbi-client.xml", "/spring/appCtx-idd-client.xml" })
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
public class ConnectionServiceProviderTest extends AbstractTransactionalJUnit4SpringContextTests {

  private static MockHttpServer requesterEndpoint = new MockHttpServer(NsiReservationFactory.PORT);

  @Resource(name = "nsiProvider_v1_sc")
  private ConnectionServiceProvider nsiProvider;

  private final String correationId = "urn:uuid:f32cc82e-4d87-45ab-baab-4b7011652a2e";

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
  public void makeItFaster() {
    nsiProvider.setDelayBeforeResponseSend(10);
  }

  @Test(expected = ServiceException.class)
  public void should_throw_exeption_because_of_null_reservervation() throws ServiceException {
    nsiProvider.reserve(null);
  }

  @Test(expected = ServiceException.class)
  public void should_throw_exeption_because_of_invalid_provider_urn() throws ServiceException {
    final ReserveRequestType reservationRequest = new NsiReservationFactory().setNsaProviderUrn(
        "urn:ogf:network:nsa:no:such:provider").createReservation();

    nsiProvider.reserve(reservationRequest);
  }

  @Test(expected = ServiceException.class)
  public void should_throw_exeption_because_of_invalid_correlation_id() throws ServiceException {
    final ReserveRequestType reservationRequest = new NsiReservationFactory().setCorrelationId(
        UUID.randomUUID().toString()).createReservation();

    nsiProvider.reserve(reservationRequest);
  }

  @Test
  @Ignore("Flacky test..")
  public void should_return_generic_acknowledgement_and_send_reservation_failed() throws Exception {
    XMLGregorianCalendar startTime = DatatypeFactory.newInstance().newXMLGregorianCalendar();
    startTime.setDay(10);
    startTime.setMonth(10);
    startTime.setYear(2012);
    startTime.setMinute(0);
    startTime.setHour(0);
    startTime.setSecond(0);

    XMLGregorianCalendar endTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(
        startTime.toGregorianCalendar());
    endTime.setDay(startTime.getDay() + 5);

    ReserveRequestType reservationRequest = new NsiReservationFactory().setScheduleStartTime(startTime)
        .setScheduleEndTime(endTime).setCorrelationId(correationId).createReservation();

    GenericAcknowledgmentType genericAcknowledgmentType = nsiProvider.reserve(reservationRequest);

    assertThat(genericAcknowledgmentType.getCorrelationId(), is(reservationRequest.getCorrelationId()));

    String lastRequest = requesterEndpoint.getOrWaitForRequest(5);

    assertTrue(lastRequest.contains(correationId));
    assertTrue(lastRequest.contains("reserveFailed"));

    assertThat(requesterEndpoint.getCallCounter(), is(1));
  }
}
