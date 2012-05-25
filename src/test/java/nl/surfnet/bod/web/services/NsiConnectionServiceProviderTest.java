package nl.surfnet.bod.web.services;

import static org.junit.Assert.*;

import java.util.UUID;

import javax.annotation.Resource;

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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-test.xml",
    "/spring/appCtx-nbi-client.xml", "/spring/appCtx-idd-client.xml" })
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
public class NsiConnectionServiceProviderTest extends AbstractTransactionalJUnit4SpringContextTests {

  @Resource(name = "nsiConnectionServiceProvider")
  private NsiConnectionServiceProvider nsiConnectionServiceProvider;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test(expected = ServiceException.class)
  public void should_throw_exeption_after_null_reservervation() throws ServiceException {
    assertNotNull(nsiConnectionServiceProvider);
    nsiConnectionServiceProvider.reserve(null);
  }

  @Test(expected = ServiceException.class)
  public void should_throw_exeption_because_of_invalid_provider_urn() throws ServiceException {
    final ReserveRequestType reservationRequest = new NsiReservationFactory().setNsaProviderUrn(
        "urn:ogf:network:nsa:no:such:provider").createReservation();
    final GenericAcknowledgmentType genericAcknowledgmentType = nsiConnectionServiceProvider
        .reserve(reservationRequest);
    assertEquals(reservationRequest.getCorrelationId(), genericAcknowledgmentType.getCorrelationId());
  }

  @Test(expected = ServiceException.class)
  public void should_throw_exeption_because_of_invalid_correlation_id() throws ServiceException {
    final ReserveRequestType reservationRequest = new NsiReservationFactory().setCorrelationId(
        UUID.randomUUID().toString()).createReservation();
    final GenericAcknowledgmentType genericAcknowledgmentType = nsiConnectionServiceProvider
        .reserve(reservationRequest);
    assertEquals(reservationRequest.getCorrelationId(), genericAcknowledgmentType.getCorrelationId());
  }

  @Test
  public void should_return_generic_acknowledgement() throws Exception {
    final ReserveRequestType reservationRequest = new NsiReservationFactory().createReservation();
    final GenericAcknowledgmentType genericAcknowledgmentType = nsiConnectionServiceProvider
        .reserve(reservationRequest);
    assertEquals(reservationRequest.getCorrelationId(), genericAcknowledgmentType.getCorrelationId());
  }

}
