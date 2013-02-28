package nl.surfnet.bod.nbi.mtosi;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.support.ReservationFactory;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ReserveException;

@RunWith(MockitoJUnitRunner.class)
public class ServiceComponentActivationClientTest {
  
  
  @InjectMocks
  private ServiceComponentActivationClient subject;

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void shouldHandleInitialReservationException() {
    
    Reservation reservation = new ReservationFactory().setStartDateTime(DateTime.now().plusYears(2))
        .setEndDateTime(DateTime.now().plusYears(2).plusDays(3)).setName("mtosiSurfTest2").create();

    reservation.getSourcePort().getPhysicalPort().setNmsSapName("SAP-00:03:18:58:cf:b0-50");
    reservation.getSourcePort().getPhysicalPort().setNmsPortId("1-1-1-8");
    reservation.getSourcePort().getPhysicalPort().setNmsNeId("00:03:18:58:cf:b0");

    reservation.getDestinationPort().getPhysicalPort().setNmsSapName("SAP-00:03:18:58:ce:20-50");
    reservation.getDestinationPort().getPhysicalPort().setNmsPortId("1-1-1-1");
    reservation.getDestinationPort().getPhysicalPort().setNmsNeId("00:03:18:58:ce:20");
    subject.handleInitialReservationException(reservation , new ReserveException("SAP is in use", null));
    assertThat(reservation.getFailedReason(), is("SAP is in use"));
    assertThat(reservation.getStatus(), is(ReservationStatus.NOT_ACCEPTED));
    
  }

}
