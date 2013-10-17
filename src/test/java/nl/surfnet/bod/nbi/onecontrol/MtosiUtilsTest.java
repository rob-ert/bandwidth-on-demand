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
package nl.surfnet.bod.nbi.onecontrol;

import static com.google.common.collect.Iterables.getOnlyElement;
import static nl.surfnet.bod.matchers.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.ReservationStatus;

import org.junit.BeforeClass;
import org.junit.Test;
import org.tmforum.mtop.fmw.xsd.nam.v1.NamingAttributeType;
import org.tmforum.mtop.fmw.xsd.nam.v1.RelativeDistinguishNameType;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.Notify;
import org.tmforum.mtop.msi.xsd.sir.v1.GetServiceInventoryResponse;
import org.tmforum.mtop.sb.xsd.savc.v1.ServiceAttributeValueChangeType;
import org.tmforum.mtop.sb.xsd.svc.v1.ResourceFacingServiceType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceAccessPointType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceStateType;

public class MtosiUtilsTest {

  private static final List<String> packages = Lists.newArrayList(
    "org.tmforum.mtop.fmw.xsd.notmsg.v1",
    "org.tmforum.mtop.fmw.xsd.hdr.v1",
    "org.tmforum.mtop.fmw.xsd.avc.v1",
    "org.tmforum.mtop.fmw.xsd.sc.v1",
    "org.tmforum.mtop.fmw.xsd.gen.v1",
    "org.tmforum.mtop.fmw.xsd.cei.v1",
    "org.tmforum.mtop.fmw.xsd.cornot.v1",
    "org.tmforum.mtop.fmw.xsd.ei.v1",
    "org.tmforum.mtop.fmw.xsd.hbt.v1",
    "org.tmforum.mtop.fmw.xsd.msg.v1",
    "org.tmforum.mtop.fmw.xsd.nam.v1",
    "org.tmforum.mtop.fmw.xsd.oc.v1",
    "org.tmforum.mtop.fmw.xsd.odel.v1",
    "org.tmforum.mtop.nra.xsd.alm.v1",
    "org.tmforum.mtop.nra.xsd.com.v1",
    "org.tmforum.mtop.nra.xsd.prc.v1",
    "org.tmforum.mtop.nrb.xsd.lay.v1",
    "org.tmforum.mtop.sb.xsd.savc.v1",
    "org.tmforum.mtop.sb.xsd.soc.v1",
    "org.tmforum.mtop.sb.xsd.sodel.v1",
    "org.tmforum.mtop.sb.xsd.svc.v1",
    "org.tmforum.mtop.msi.xsd.sir.v1");

  private static JAXBContext jaxbContext;
  private static GetServiceInventoryResponse rfsServiceInventory;
  private static GetServiceInventoryResponse sapServiceInventory;
  private static Notify notifyServiceAttributeValueChange;

  @BeforeClass
  public static void initInventoryObjects() {
    try {
      jaxbContext = JAXBContext.newInstance(Joiner.on(":").join(packages));
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      rfsServiceInventory = (GetServiceInventoryResponse) unmarshaller.unmarshal(new File("src/test/resources/mtosi/RfsInventory.xml"));
      sapServiceInventory = (GetServiceInventoryResponse) unmarshaller.unmarshal(new File("src/test/resources/mtosi/SapInventory.xml"));
      notifyServiceAttributeValueChange = (Notify) unmarshaller.unmarshal(new File("src/test/resources/mtosi/serviceAttributeValueChange.xml"));
    } catch (JAXBException e) {
      throw new AssertionError(e);
    }
  }

  @Test
  public void should_find_secondary_state_in_service_attribute_value_change_notification() throws JAXBException {
    ServiceAttributeValueChangeType valueChange = (ServiceAttributeValueChangeType) getOnlyElement(notifyServiceAttributeValueChange.getMessage().getCommonEventInformation()).getValue();

    Optional<RfsSecondaryState> secondaryState = MtosiUtils.findSecondaryState(valueChange);

    assertThat(secondaryState, isPresent(RfsSecondaryState.INITIAL));
  }

  @Test
  public void convertPtp() {
    assertThat(MtosiUtils.convertToShortPtP("/rack=1/shelf=1/slot=1/port=48"), is("1-1-1-48"));
  }

  @Test
  public void convertPtpWithSubSlot() {
    assertThat(MtosiUtils.convertToShortPtP("/rack=1/shelf=1/slot=3/sub_slot=1/port=5"), is("1-1-3-1-5"));
  }

  @Test
  public void convertNmsPortId() {
    assertThat(MtosiUtils.convertToLongPtP("1-2-3-4"), is("/rack=1/shelf=2/slot=3/port=4"));
  }

  @Test
  public void convertNmsPortIdWithSubSlot() {
    assertThat(MtosiUtils.convertToLongPtP("2-3-4-5-10"), is("/rack=2/shelf=3/slot=4/sub_slot=5/port=10"));
  }

  @Test
  public void should_compose_nms_port_id() {
    assertThat(MtosiUtils.composeNmsPortId("me", "1-1-1-1"), is("me@1-1-1-1"));
  }

  @Test
  public void should_decompose_nms_port_id() {
    assertThat(MtosiUtils.extractPtpFromNmsPortId("me@1-1-1-1"), is(MtosiUtils.convertToLongPtP("1-1-1-1")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_throw_illegal_argument_when_nms_port_id_is_wrong() {
    MtosiUtils.extractPtpFromNmsPortId("1-1-1-1");
  }

  @Test(expected = NullPointerException.class)
  public void should_throw_null_pointer_when_nms_port_id_is_null() {
    MtosiUtils.extractPtpFromNmsPortId(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_not_compose_nms_port_id_when_at_sign_is_present_in_ptp() {
    MtosiUtils.composeNmsPortId("p@p", "me");
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_not_compose_nms_port_id_when_at_sign_is_present_in_me() {
    MtosiUtils.composeNmsPortId("ptp", "m@e");
  }

  @Test
  public void shouldCreateNamingAttribWithArgs() {
    NamingAttributeType attrib = MtosiUtils.createNamingAttributeType("type", "value");

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
    assertThat(mapMtosiState(ServiceStateType.RESERVED, RfsSecondaryState.RESERVING), is(ReservationStatus.REQUESTED));
    assertThat(mapMtosiState(ServiceStateType.RESERVED, RfsSecondaryState.INITIAL), is(ReservationStatus.RESERVED));
    assertThat(mapMtosiState(ServiceStateType.RESERVED, RfsSecondaryState.SCHEDULED), is(ReservationStatus.SCHEDULED));
    assertThat(mapMtosiState(ServiceStateType.RESERVED, RfsSecondaryState.PROVISIONING), is(ReservationStatus.AUTO_START));
    assertThat(mapMtosiState(ServiceStateType.RESERVED, RfsSecondaryState.ACTIVATING), is(ReservationStatus.SCHEDULED));
    assertThat(mapMtosiState(ServiceStateType.RESERVED, RfsSecondaryState.TERMINATING), is(ReservationStatus.CANCELLED));
    assertThat(mapMtosiState(ServiceStateType.PROVISIONED_ACTIVE, RfsSecondaryState.ACTIVATED), is(ReservationStatus.RUNNING));
    assertThat(mapMtosiState(ServiceStateType.PROVISIONED_INACTIVE, RfsSecondaryState.TERMINATING), is(ReservationStatus.SUCCEEDED));
    assertThat(mapMtosiState(ServiceStateType.TERMINATED, null), is(ReservationStatus.SUCCEEDED));
  }

  private ReservationStatus mapMtosiState(ServiceStateType serviceState, RfsSecondaryState secondaryState) {
    ResourceFacingServiceType rfs = new ResourceFacingServiceType()
        .withServiceState(serviceState);
    if (secondaryState != null) {
      rfs.withDescribedByList(MtosiUtils.createSecondaryStateValueType(secondaryState.name()));
    }
    return MtosiUtils.mapToReservationState(rfs);
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

    assertThat(sapName, is("00:03:18:bb:5a:00-1/31"));
  }

}