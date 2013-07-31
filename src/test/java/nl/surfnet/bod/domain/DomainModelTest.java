/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.domain;

import nl.surfnet.bod.support.ConnectionV1Factory;
import nl.surfnet.bod.support.NsiV1RequestDetailsFactory;
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
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DomainModelTest {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final ReservationFactory reservationFactory = new ReservationFactory();
  private final PhysicalResourceGroupFactory prgFactory = new PhysicalResourceGroupFactory();
  private final VirtualResourceGroupFactory vrgFactory = new VirtualResourceGroupFactory();
  private final VirtualPortFactory vpFactory = new VirtualPortFactory();
  private final PhysicalPortFactory ppFactory = new PhysicalPortFactory();
  private final VirtualPortRequestLinkFactory vprlFactory = new VirtualPortRequestLinkFactory();

  private Reservation reservation;
  private Reservation reservationTwo;
  private VirtualPortRequestLink link;
  private VirtualResourceGroup vrg;

  private PhysicalResourceGroup prg;

  private PhysicalPort pp1;

  @Before
  public void onSetup() {
    pp1 = ppFactory.create();
    PhysicalPort pp2 = ppFactory.create();
    prg = prgFactory.addPhysicalPort(pp1, pp2).create();
    pp1.setPhysicalResourceGroup(prg);
    pp2.setPhysicalResourceGroup(prg);

    vrg = vrgFactory.create();
    VirtualPort vp1 = vpFactory.setVirtualResourceGroup(vrg).create();
    VirtualPort vp2 = vpFactory.setVirtualResourceGroup(vrg).create();
    vrg.setVirtualPorts(Lists.newArrayList(vp1, vp2));

    reservationFactory.setSourcePort(vp1);
    reservationFactory.setDestinationPort(vp2);
    reservation = reservationFactory.create();
    reservation.setConnectionV1(new ConnectionV1Factory().create());

    reservationTwo = reservationFactory.create();

    ReflectionTestUtils.setField(reservationTwo, "creationDateTime", reservation.getCreationDateTime());

    reservation.setVirtualResourceGroup(vrg);
    reservationTwo.setVirtualResourceGroup(vrg);
    vrg.setReservations(Lists.newArrayList(reservation, reservationTwo));

    link = vprlFactory.setVirtualResourceGroup(vrg).create();
    vrg.setVirtualPortRequestLinks(Lists.newArrayList(link));
  }

  /**
   * Bidirectional relations between the domains are treaded different in the
   * specific toString methods, to prevent stackoverFlow.
   */
  @Test
  public void shouldNotOverflowInReservationToString() {
    ConnectionV1 connection = new ConnectionV1Factory().create();
    connection.setReservation(reservation);
    reservation.setConnectionV1(connection);
    logger.info(reservation.toString());
  }

  @Test
  public void shouldOnlyContainLabelsOfConnectionAndVirtualResourceGroupInReservation() {
    ConnectionV1 connection = new ConnectionV1Factory().create();
    connection.setReservation(reservation);
    reservation.setConnectionV1(connection);
    String reservationString = reservation.toString();

    assertThat(reservationString, not(containsString("Connection [")));
    assertThat(reservationString, not(containsString("VirtualResourceGroup [")));
  }

  /**
   * Bidirectional relations between the domains are treaded different in the
   * specific toString methods, to prevent stackoverFlow.
   */
  @Test
  public void shouldNotOverflowInVirtualPortRequestLinkToString() {
    logger.info(link.toString());
  }

  /**
   * Bidirectional relations between the domains are treaded different in the
   * specific equal methods, to prevent stackoverFlow.
   */
  @Test
  public void shouldNotOverflowInReservationEquals() {
    reservation.setConnectionV1(new ConnectionV1Factory().create());
    reservation.equals(reservationTwo);
  }

  @Test
  public void shouldNotOverflowInConnectionToString() {
    Connection connection = new ConnectionV1Factory().create();
    connection.toString();
  }

  /**
   * Bidirectional relations between the domains are treaded different in the
   * specific equal methods, to prevent stackoverFlow.
   */
  @Test
  public void shouldNotOverflowInReservationHashCode() {
    reservation.setConnectionV1(new ConnectionV1Factory().create());
    reservation.hashCode();
  }

  @Test
  public void shouldOnlyConsiderIdAndVersionInReservationEquals() {
    reservation.setId(reservationTwo.getId() + 1);
    reservation.setName("myReservation");
    assertThat(reservation, not(reservationTwo));

    reservation.setId(reservationTwo.getId());
    reservation.setVersion(reservationTwo.getVersion());

    assertThat(reservation, is(reservationTwo));
  }

  @Test
  public void shouldOnlyConsiderIdAndVersionInReservationHashcode() {
    reservation.setId(reservationTwo.getId() + 1);
    reservation.setName("myReservation");
    assertFalse(reservation.hashCode() == reservationTwo.hashCode());

    reservation.setId(reservationTwo.getId());
    reservation.setVersion(reservationTwo.getVersion());

    assertTrue(reservation.hashCode() == reservationTwo.hashCode());
  }

  /**
   * Bidirectional relations between the domains are treaded different in the
   * specific equal methods, to prevent stackoverFlow.
   */
  @Test
  public void shouldOnlyConsiderIdAndVersionInVirtualPortRequestLinkEquals() {
    VirtualPortRequestLink requestLink = new VirtualPortRequestLinkFactory().create();
    requestLink.setId(3l);

    assertThat("Only on Id and version", link, not(requestLink));

    requestLink.setId(link.getId());
    requestLink.setVersion(link.getVersion());

    assertThat("Only on Id and version", link, is(requestLink));
  }

  /**
   * Bidirectional relations between the domains are treaded different in the
   * specific equal methods, to prevent stackoverFlow.
   */
  @Test
  public void shouldOnlyConsiderIdAndVersionInVirtualPortRequestLinkHashCode() {
    VirtualPortRequestLink requestLink = new VirtualPortRequestLinkFactory().create();
    requestLink.setId(3l);

    assertThat("Only on Id and version", link.hashCode(), not(requestLink.hashCode()));

    requestLink.setId(link.getId());
    requestLink.setVersion(link.getVersion());
    assertThat("Only on Id and version", link.hashCode(), is(requestLink.hashCode()));
  }

  /**
   * Bidirectional relations between the domains are treaded different in the
   * specific equal methods, to prevent stackoverFlow.
   */
  @Test
  public void shouldOnlyConsiderIdAndVersionInVirtualResourceGroupEquals() {
    VirtualResourceGroup vrgTwo = new VirtualResourceGroupFactory().create();

    assertThat("Only on Id and version", vrg, not(vrgTwo));

    vrgTwo.setId(vrg.getId());
    vrgTwo.setVersion(vrg.getVersion());
    assertThat("Only on Id and version", vrg, is(vrgTwo));
  }

  @Test
  public void shouldOnlyconsiderIdAndVersionInVirturalResourceGroupHashCode() {
    VirtualResourceGroup vrgTwo = new VirtualResourceGroupFactory().create();

    assertFalse("Only on Id and version", vrg.hashCode() == vrgTwo.hashCode());

    vrgTwo.setId(vrg.getId());
    vrgTwo.setVersion(vrg.getVersion());
    assertTrue("Only on Id and version", vrg.hashCode() == vrgTwo.hashCode());
  }

  @Test
  public void shouldOnlyConsiderIdAndVersionInPhysicalResourceGroupEquals() {
    PhysicalResourceGroup prgTwo = new PhysicalResourceGroupFactory().create();

    assertThat("Only on id and version", prg, not(prgTwo));

    prgTwo.setId(prg.getId());
    prgTwo.setVersion(prg.getVersion());
    assertThat("Only on id and version", prg, is(prgTwo));
  }

  @Test
  public void shouldOnlyConsiderIdAndVersionInPhysicalResourceGroupHashcode() {
    PhysicalResourceGroup prgTwo = new PhysicalResourceGroupFactory().create();

    assertFalse("Only on id and version", prg.hashCode() == prgTwo.hashCode());

    prgTwo.setId(prg.getId());
    prgTwo.setVersion(prg.getVersion());
    assertTrue("Only on id and version", prg.hashCode() == prgTwo.hashCode());
  }

  @Test
  public void shouldOnlyConsiderIdAndVersionInPhysicalPortEquals() {
    PhysicalPort physicalPort = new PhysicalPortFactory().create();

    assertThat("Only on id and version", physicalPort, not(pp1));

    physicalPort.setId(pp1.getId());
    physicalPort.setVersion(pp1.getVersion());
    assertThat("Only on id and version", physicalPort, is(pp1));
  }

  @Test
  public void shouldOnlyConsiderIdAndVersionInPhysicalPortHashcode() {
    PhysicalPort physicalPort = new PhysicalPortFactory().create();

    assertFalse("Only consider id and version", physicalPort.hashCode() == pp1.hashCode());

    physicalPort.setId(pp1.getId());
    physicalPort.setVersion(pp1.getVersion());
    assertTrue("Only consider id and version", physicalPort.hashCode() == pp1.hashCode());
  }

}
