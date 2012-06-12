package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class NocServiceTest {

  @InjectMocks
  private NocService subject;

  @Mock
  private ReservationService reservationServiceMock;

  @Mock
  private VirtualPortService virtualPortServiceMock;

  @Mock
  private PhysicalPortService physicalPortServiceMock;

  private RichUserDetails user = new RichUserDetailsFactory().addNocRole().create();

  @Before
  public void setNocUser() {
    Security.setUserDetails(user);
  }

  @Test
  public void moveShouldRescheduleReservations() {
    LocalDateTime start = LocalDateTime.now().plusDays(2);
    LocalDateTime end = LocalDateTime.now().plusDays(5);

    PhysicalPort oldPort = new PhysicalPortFactory().create();
    PhysicalPort newPort = new PhysicalPortFactory().create();
    VirtualPort vPort = new VirtualPortFactory().create();
    Reservation reservation = new ReservationFactory()
      .setBandwidth(150)
      .setName("My first reservation")
      .setStartDateTime(start)
      .setEndDateTime(end).create();

    when(virtualPortServiceMock.findAllForPhysicalPort(oldPort)).thenReturn(ImmutableList.of(vPort));
    when(reservationServiceMock.findActiveByPhysicalPort(oldPort)).thenReturn(ImmutableList.of(reservation));

    subject.movePort(oldPort, newPort);

    ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
    verify(reservationServiceMock).create(reservationCaptor.capture());

    Reservation newReservation = reservationCaptor.getValue();
    assertThat(newReservation.getBandwidth(), is(150));
    assertThat(newReservation.getName(), is("My first reservation"));
    assertThat(newReservation.getStartDateTime(), is(start));
    assertThat(newReservation.getEndDateTime(), is(end));
  }

  @Test
  public void oldPortShouldBeUnallocated() {
    PhysicalPort oldPort = new PhysicalPortFactory().create();
    PhysicalPort newPort = new PhysicalPortFactory().create();

    subject.movePort(oldPort, newPort);

    verify(physicalPortServiceMock).delete(oldPort);
  }

  @Test
  public void virtualPortsShouldHaveNewPhysicalPort() {
    PhysicalPort oldPort = new PhysicalPortFactory().create();
    PhysicalPort newPort = new PhysicalPortFactory().create();
    VirtualPort port1 = new VirtualPortFactory().create();
    VirtualPort port2 = new VirtualPortFactory().create();

    when(virtualPortServiceMock.findAllForPhysicalPort(oldPort)).thenReturn(ImmutableList.of(port1, port2));

    subject.movePort(oldPort, newPort);

    assertThat(port1.getPhysicalPort(), is(newPort));
    assertThat(port2.getPhysicalPort(), is(newPort));
  }

}
