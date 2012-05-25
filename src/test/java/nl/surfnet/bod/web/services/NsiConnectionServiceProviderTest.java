package nl.surfnet.bod.web.services;

import static org.junit.Assert.*;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReserveType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
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

  @Test
  public void should_throw_exeption_after_null_reservervation() {
    assertNotNull(nsiConnectionServiceProvider);
    try {
      nsiConnectionServiceProvider.reserve(null);
      fail("should throw ServiceException because request was null");
    }
    catch (ServiceException e) {

    }
  }

  @Test(expected = ServiceException.class)
  public void should_throw_exeption_because_of_invalid_provider_urn() throws ServiceException {
    final String nsaProviderUrn = "NOT:VALID:urn:ogf:network:nsa:netherlight";
    final ReserveRequestType reservationRequest = createReservation(nsaProviderUrn);
    final GenericAcknowledgmentType genericAcknowledgmentType = nsiConnectionServiceProvider
        .reserve(reservationRequest);
    assertEquals(reservationRequest.getCorrelationId(), genericAcknowledgmentType.getCorrelationId());
  }

  @Test
  public void should_return_generic_acknowledgement() {
    try {
      final String nsaProviderUrn = "urn:ogf:network:nsa:netherlight";
      final ReserveRequestType reservationRequest = createReservation(nsaProviderUrn);
      final GenericAcknowledgmentType genericAcknowledgmentType = nsiConnectionServiceProvider
          .reserve(reservationRequest);
      assertEquals(reservationRequest.getCorrelationId(), genericAcknowledgmentType.getCorrelationId());

    }
    catch (ServiceException e) {
      fail("should throw ServiceException because request was null");
    }
  }

  /**
   * @param nsaProviderUrn
   * @return
   */
  private ReserveRequestType createReservation(final String nsaProviderUrn) {
    final ReserveRequestType reservationRequest = new ReserveRequestType();
    reservationRequest.setCorrelationId(UUID.randomUUID().toString());
    reservationRequest.setReplyTo("http://localhost:8082/bod/nsi/requester");
    final ReserveType reservationType = new ReserveType();

    reservationType.setProviderNSA(nsaProviderUrn);
    final ReservationInfoType reservationInfoType = new ReservationInfoType();
    final ServiceParametersType serviceParameters = new ServiceParametersType();
    serviceParameters.setBandwidth(null);
    serviceParameters.setSchedule(null);
    reservationInfoType.setServiceParameters(serviceParameters);
    reservationType.setReservation(reservationInfoType);
    reservationRequest.setReserve(reservationType);
    return reservationRequest;
  }
}
