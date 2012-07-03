package nl.surfnet.bod.domain;

import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualPortRequestLinkFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class DomainModelTest {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private ReservationFactory reservationFactory = new ReservationFactory();
  private PhysicalResourceGroupFactory prgFactory = new PhysicalResourceGroupFactory();
  private VirtualResourceGroupFactory vrgFactory = new VirtualResourceGroupFactory();
  private VirtualPortFactory vpFactory = new VirtualPortFactory();
  private PhysicalPortFactory ppFactory = new PhysicalPortFactory();
  private VirtualPortRequestLinkFactory vprlFactory = new VirtualPortRequestLinkFactory();

  private Reservation reservation;
  private VirtualPortRequestLink link;

  @Before
  public void onSetup() {
    PhysicalPort pp1 = ppFactory.create();
    PhysicalPort pp2 = ppFactory.create();
    PhysicalResourceGroup prg = prgFactory.addPhysicalPort(pp1, pp2).create();
    pp1.setPhysicalResourceGroup(prg);
    pp2.setPhysicalResourceGroup(prg);

    VirtualResourceGroup vrg = vrgFactory.create();
    VirtualPort vp1 = vpFactory.setVirtualResourceGroup(vrg).create();
    VirtualPort vp2 = vpFactory.setVirtualResourceGroup(vrg).create();
    vrg.setVirtualPorts(Lists.newArrayList(vp1, vp2));

    reservationFactory.setSourcePort(vp1);
    reservationFactory.setDestinationPort(vp2);
    reservation = reservationFactory.create();
    reservation.setVirtualResourceGroup(vrg);
    vrg.setReservations(Lists.newArrayList(reservation));

    link = vprlFactory.setVirtualResourceGroup(vrg).create();
    vrg.setVirtualPortRequestLinks(Lists.newArrayList(link));
  }

  /**
   * Bidirectional relations between the domains are treaded different in the
   * specific toString methods, to prevent stackoverFlow.
   */
  @Test
  public void shouldNotOverflowInReservationToString() {
    logger.info(reservation.toString());
  }

  /**
   * Bidirectional relations between the domains are treaded different in the
   * specific toString methods, to prevent stackoverFlow.
   */
  @Test
  public void shouldNotOverflowInVirtualPortRequestLinkToString() {
    logger.info(link.toString());
  }

}
