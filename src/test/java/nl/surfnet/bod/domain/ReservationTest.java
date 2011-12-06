package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Calendar;

import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.junit.Before;
import org.junit.Test;

public class ReservationTest {

  private Long id;

  private Integer version;

  private String surfConnextGroupId;

  private VirtualResourceGroup virtualResourceGroup;

  private ReservationStatus reservationStatus;

  private VirtualPort sourcePort;

  private VirtualPort endPort;

  private Calendar startTimeStamp;

  private Calendar endTimeStamp;
  
  private String user;

  @Before
  public void setUp() {

    id = 1l;
    version = 1;
    surfConnextGroupId = "smurfId";
    reservationStatus = ReservationStatus.NEW;
    sourcePort = new VirtualPortFactory().setPhysicalPort(new PhysicalPortFactory().setName("startPort").create())
        .create();
    endPort = new VirtualPortFactory().setPhysicalPort(new PhysicalPortFactory().setName("endPort").create()).create();
    startTimeStamp = Calendar.getInstance();
    endTimeStamp = Calendar.getInstance();
    endTimeStamp.add(Calendar.DAY_OF_MONTH, 1);
    user= "SurfUser";

    virtualResourceGroup = new VirtualResourceGroupFactory().addVirtualPorts(sourcePort, endPort).create();
  }

  @Test
  public void testSetters() {
    Reservation reservation = new ReservationFactory().setEndPort(endPort).setEndTimeStamp(endTimeStamp.getTime())
        .setId(id).setReservationStatus(reservationStatus).setSourcePort(sourcePort)
        .setStartTimeStamp(startTimeStamp.getTime()).setSurfConnextGroupId(surfConnextGroupId).setVersion(version)
        .setVirtualResourceGroup(virtualResourceGroup).setUser(user).create();

    assertThat(reservation.getEndPort(), is(endPort));
    assertThat(reservation.getEndTimeStamp(), is(endTimeStamp.getTime()));
    assertThat(reservation.getId(), is(id));
    assertThat(reservation.getReservationStatus(), is(reservationStatus));
    assertThat(reservation.getSourcePort(), is(sourcePort));
    assertThat(reservation.getStartTimeStamp(), is(startTimeStamp.getTime()));
    assertThat(reservation.getSurfConnextGroupId(), is(surfConnextGroupId));
    assertThat(reservation.getVersion(), is(version));
    assertThat(reservation.getVirtualResourceGroup(), is(virtualResourceGroup));
    assertThat(reservation.getUser(), is(user));
  }
  
}
