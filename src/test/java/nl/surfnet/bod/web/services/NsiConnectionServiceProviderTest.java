package nl.surfnet.bod.web.services;

import static org.junit.Assert.*;

import java.util.UUID;

import javax.annotation.Resource;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import nl.surfnet.bod.support.NsiReservationFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.test.client.MockWebServiceServer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-test.xml",
    "/spring/appCtx-nbi-client.xml", "/spring/appCtx-idd-client.xml" })
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
public class NsiConnectionServiceProviderTest extends AbstractTransactionalJUnit4SpringContextTests {

  @Resource(name = "nsiProvider")
  private NsiConnectionServiceProvider nsiProvider;

  @SuppressWarnings("unused")
  private MockWebServiceServer mockServer;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    final WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
    webServiceTemplate.setDefaultUri(NsiReservationFactory.NSI_REQUESTER_ENDPOINT);
    mockServer = MockWebServiceServer.createServer(webServiceTemplate);
  }

  @After
  public void tearDown() throws Exception {
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
  public void should_return_generic_acknowledgement_with_valid_correlation_id() throws Exception {
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

    final ReserveRequestType reservationRequest = new NsiReservationFactory().setScheduleStartTime(startTime)
        .setScheduleEndTime(endTime).createReservation();
    final GenericAcknowledgmentType genericAcknowledgmentType = nsiProvider.reserve(reservationRequest);
    
    assertEquals(reservationRequest.getCorrelationId(), genericAcknowledgmentType.getCorrelationId());
  }

}
