package nl.surfnet.bod.nsi.ws.v1sc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nsi.ws.v1sc.ConnectionServiceProvider.RequestDetails;
import nl.surfnet.bod.repo.ConnectionRepo;
import nl.surfnet.bod.repo.VirtualPortRepo;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ConnectionFactory;
import nl.surfnet.bod.support.VirtualPortFactory;

import org.hamcrest.text.IsEmptyString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.*;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceProviderTest {

  @InjectMocks
  private ConnectionServiceProvider subject;

  @Mock
  private ConnectionRepo connectionRepoMock;
  @Mock
  private VirtualPortRepo virtualPortRepoMock;
  @Mock
  private ReservationService reservationServiceMock;

  private String nsaProvider = "nsa:surfnet.nl";

  @Before
  public void setup() {
    subject.addNsaProvider(nsaProvider);
  }

  @Test(expected = ServiceException.class)
  public void shouldComplainAboutTheProviderNsa() throws ServiceException {
    RequestDetails request = new RequestDetails("http://localhost", "123456");
    Connection connection = new ConnectionFactory()
      .setSourceStpId("Source Port").setDestinationStpId("Destination Port")
      .setProviderNSA("non:existingh").create();

    VirtualPort sourcePort = new VirtualPortFactory().create();
    VirtualPort destinationPort = new VirtualPortFactory().setVirtualResourceGroup(sourcePort.getVirtualResourceGroup()).create();

    when(virtualPortRepoMock.findByManagerLabel("Source Port")).thenReturn(sourcePort);
    when(virtualPortRepoMock.findByManagerLabel("Destination Port")).thenReturn(destinationPort);

    subject.reserve(connection, request);
  }

  @Test(expected = ServiceException.class)
  public void shouldComplainAboutNonExistingPort() throws ServiceException {
    RequestDetails request = new RequestDetails("http://localhost", "123456");
    Connection connection = new ConnectionFactory()
      .setSourceStpId("Source Port").setDestinationStpId("Destination Port")
      .setProviderNSA(nsaProvider).create();

    VirtualPort sourcePort = new VirtualPortFactory().create();

    when(virtualPortRepoMock.findByManagerLabel("Source Port")).thenReturn(sourcePort);
    when(virtualPortRepoMock.findByManagerLabel("Destination Port")).thenReturn(null);

    subject.reserve(connection, request);
  }

  @Test
  public void shouldMakeAReservation() throws ServiceException {
    RequestDetails request = new RequestDetails("http://localhost", "123456");
    Connection connection = new ConnectionFactory()
      .setSourceStpId("Source Port").setDestinationStpId("Destination Port")
      .setProviderNSA(nsaProvider).create();

    VirtualPort sourcePort = new VirtualPortFactory().create();
    VirtualPort destinationPort = new VirtualPortFactory().setVirtualResourceGroup(sourcePort.getVirtualResourceGroup()).create();

    when(virtualPortRepoMock.findByManagerLabel("Source Port")).thenReturn(sourcePort);
    when(virtualPortRepoMock.findByManagerLabel("Destination Port")).thenReturn(destinationPort);

    subject.reserve(connection, request);
  }

  @Test
  public void reserveTypeWithoutGlobalReservationIdShouldGetOne() throws DatatypeConfigurationException {
    ReserveRequestType reserveRequestType = createReservationRequestType(1000);

    Connection connection = ConnectionServiceProvider.TO_CONNECTION.apply(reserveRequestType);

    assertThat(connection.getGlobalReservationId(), not(IsEmptyString.isEmptyOrNullString()));
    assertThat(connection.getDesiredBandwidth(), is(1000));
  }

  private ReserveRequestType createReservationRequestType(int desiredBandwidth) throws DatatypeConfigurationException {
    PathType path = new PathType();

    ScheduleType schedule = new ScheduleType();
    XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar();
    schedule.setStartTime(xmlGregorianCalendar);
    schedule.setEndTime(xmlGregorianCalendar);

    BandwidthType bandwidth = new BandwidthType();
    bandwidth.setDesired(desiredBandwidth);

    ServiceParametersType serviceParameters = new ServiceParametersType();
    serviceParameters.setSchedule(schedule);
    serviceParameters.setBandwidth(bandwidth);

    ReservationInfoType reservation = new ReservationInfoType();
    reservation.setPath(path);
    reservation.setServiceParameters(serviceParameters);

    ReserveType reserve = new ReserveType();
    reserve.setReservation(reservation);

    ReserveRequestType reserveRequestType = new ReserveRequestType();
    reserveRequestType.setReserve(reserve);

    return reserveRequestType;
  }

}
