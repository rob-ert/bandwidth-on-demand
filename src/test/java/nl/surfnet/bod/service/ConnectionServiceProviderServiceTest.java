package nl.surfnet.bod.service;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.nsi.ws.v1sc.ConnectionServiceProviderTest;
import nl.surfnet.bod.repo.ConnectionRepo;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;

import com.google.common.base.Optional;

import static nl.surfnet.bod.nsi.ws.v1sc.ConnectionServiceProviderFunctions.RESERVE_REQUEST_TO_CONNECTION;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceProviderServiceTest {

  @Mock
  public ConnectionRepo connectionRepoMock;

  @Mock
  private ReservationService reservationServiceMock;

  @Mock
  private VirtualPortService virtualPortServiceMock;

  @InjectMocks
  private ConnectionServiceProviderService subject;

  @Test
  public void shouldUseRichUserDetailsAsCreater() throws ServiceException {
    RichUserDetails userDetails = new RichUserDetailsFactory().setUsername("me").create();
    Security.setUserDetails(userDetails);

    ReserveRequestType reserveRequest = ConnectionServiceProviderTest.createReservationRequestType(512, Optional
        .<String> absent());
    reserveRequest.getReserve().setProviderNSA("nsa:surfnet.nl");
    reserveRequest.getReserve().getReservation().setConnectionId("123");
    NsiRequestDetails nsiRequestDetails = new NsiRequestDetails("replyTo", "123");

    Connection connection = RESERVE_REQUEST_TO_CONNECTION.apply(reserveRequest);
    when(virtualPortServiceMock.findByNsiStpId(anyString())).thenReturn(new VirtualPortFactory().create());
    when(connectionRepoMock.saveAndFlush(any(Connection.class))).thenReturn(connection);

    subject.reserve(connection, nsiRequestDetails, true);

    assertThat(connection.getReservation().getUserCreated(), is("me"));
  }

}
