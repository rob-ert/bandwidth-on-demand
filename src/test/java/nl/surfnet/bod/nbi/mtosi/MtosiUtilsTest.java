/**
 * Copyright (c) 2012, SURFnet BV
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
package nl.surfnet.bod.nbi.mtosi;

import static nl.surfnet.bod.matchers.OptionalMatchers.isAbsent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.google.common.base.Optional;

import nl.surfnet.bod.domain.ReservationStatus;

import org.junit.BeforeClass;
import org.junit.Test;
import org.tmforum.mtop.fmw.xsd.nam.v1.NamingAttributeType;
import org.tmforum.mtop.fmw.xsd.nam.v1.RelativeDistinguishNameType;
import org.tmforum.mtop.msi.xsd.sir.v1.GetServiceInventoryResponse;
import org.tmforum.mtop.sb.xsd.svc.v1.ResourceFacingServiceType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceAccessPointType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceStateType;

public class MtosiUtilsTest {

  private static GetServiceInventoryResponse rfsServiceInventory;
  private static GetServiceInventoryResponse sapServiceInventory;

  @BeforeClass
  public static void initInvetoryObjects() {
    try{
      Unmarshaller unmarshaller = JAXBContext.newInstance(GetServiceInventoryResponse.class).createUnmarshaller();
      rfsServiceInventory = (GetServiceInventoryResponse) unmarshaller.unmarshal(new File("src/test/resources/mtosi/RfsInventory.xml"));
      sapServiceInventory = (GetServiceInventoryResponse) unmarshaller.unmarshal(new File("src/test/resources/mtosi/SapInventory.xml"));
    }
    catch (JAXBException e) {
      throw new AssertionError(e);
    }
  }

  @Test
  public void convertPtp() {
    assertThat(
        MtosiUtils.convertToShortPtP("/rack=1/shelf=1/slot=1/port=48"),
        is("1-1-1-48"));
  }

  @Test
  public void convertPtpWithSubSlot() {
    assertThat(
        MtosiUtils.convertToShortPtP("/rack=1/shelf=1/slot=3/sub_slot=1/port=5"),
        is("1-1-3-1-5"));
  }

  @Test
  public void convertNmsPortId() {
    assertThat(
        MtosiUtils.convertToLongPtP("1-2-3-4"),
        is("/rack=1/shelf=2/slot=3/port=4"));
  }

  @Test
  public void convertNmsPortIdWithSubSlot() {
    assertThat(
        MtosiUtils.convertToLongPtP("2-3-4-5-10"),
        is("/rack=2/shelf=3/slot=4/sub_slot=5/port=10"));
  }

  @Test
  public void shouldComposeNmsPortId() {
    assertThat(MtosiUtils.composeNmsPortId("me", "1-1-1-1"), is("me@1-1-1-1"));
  }

  @Test
  public void shouldDecomposeNmsPortId() {
    assertThat(MtosiUtils.extractPTPFromNmsPortId("me@1-1-1-1"),
        is(MtosiUtils.convertToLongPtP("1-1-1-1")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotComposeNmsPortIdWhenAtSignIsPresentInPtP() {
    MtosiUtils.composeNmsPortId("p@p", "me");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotComposeNmsPortIdWhenAtSignIsPresentInMe() {
    MtosiUtils.composeNmsPortId("ptp", "m@e");
  }

  @Test
  public void shouldCreateNamingAttrib() {
    NamingAttributeType attrib = MtosiUtils.createNamingAttrib();

    assertThat(attrib.getRdn(), hasSize(0));
  }

  @Test
  public void shouldCreateNamingAttribWithArgs() {
    NamingAttributeType attrib = MtosiUtils.createNamingAttrib("type", "value");

    assertThat(attrib.getRdn(), hasSize(1));
    assertThat(attrib.getRdn().get(0).getType(), is("type"));
    assertThat(attrib.getRdn().get(0).getValue(), is("value"));
  }

  @Test
  public void shouldCreateRdn() {
    RelativeDistinguishNameType rdn = MtosiUtils.createRdn("type", "value");

    assertThat(rdn.getType(), is("type"));
    assertThat(rdn.getValue(), is("value"));
  }

  @Test
  public void shouldMapServiceStatesToReservationStates() {
    assertNull(MtosiUtils.mapToReservationState(ServiceStateType.PLANNING_FEASIBILITY_CHECK));
    assertNull(MtosiUtils.mapToReservationState(ServiceStateType.PLANNING_DESIGNED));

    assertThat(ReservationStatus.SCHEDULED, is(MtosiUtils.mapToReservationState(ServiceStateType.RESERVED)));
    assertThat(MtosiUtils.mapToReservationState(ServiceStateType.PROVISIONED_INACTIVE), is(ReservationStatus.AUTO_START));
    assertThat(MtosiUtils.mapToReservationState(ServiceStateType.PROVISIONED_ACTIVE), is(ReservationStatus.RUNNING));
    assertThat(MtosiUtils.mapToReservationState(ServiceStateType.TERMINATED), is(ReservationStatus.SUCCEEDED));
  }

  @Test
  public void shouldGetRfsNameFromRfs() {
    ResourceFacingServiceType firstRfs = rfsServiceInventory.getInventoryData().getRfsList().getRfs().get(0);

    String rfsName = MtosiUtils.getRfsName(firstRfs);

    assertThat(rfsName, is("mtosiRFS"));
  }

  @Test
  public void shouldGetSapName() {
    ServiceAccessPointType firstSap = sapServiceInventory.getInventoryData().getSapList().getSap().get(0);

    String sapName = MtosiUtils.getSapName(firstSap);

    assertThat(sapName, is("SAP-00:03:18:58:cf:b0-3"));
  }

  @Test
  public void shouldFindVendorExtensionStartTime() {
    ResourceFacingServiceType firstRfs = rfsServiceInventory.getInventoryData().getRfsList().getRfs().get(0);

    Optional<String> startTime = MtosiUtils.findVendorExtension("startTime", firstRfs);

    assertThat(startTime.get(), is("2012-11-24T12:32:52.000Z"));
  }

  @Test
  public void shouldNotFindVendorExtensionFoo() {
    ResourceFacingServiceType firstRfs = rfsServiceInventory.getInventoryData().getRfsList().getRfs().get(0);

    Optional<String> secondaryState = MtosiUtils.findVendorExtension("foo", firstRfs);

    assertThat(secondaryState, isAbsent());
  }

}