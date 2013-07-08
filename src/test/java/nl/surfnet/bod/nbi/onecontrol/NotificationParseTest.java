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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.junit.Test;
import org.tmforum.mtop.fmw.xsd.cei.v1.CommonEventInformationType;
import org.tmforum.mtop.fmw.xsd.hbt.v1.HeartbeatType;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.Notify;
import org.tmforum.mtop.sb.xsd.savc.v1.ServiceAttributeValueChangeType;
import org.tmforum.mtop.sb.xsd.soc.v1.ServiceObjectCreationType;
import org.tmforum.mtop.sb.xsd.svc.v1.ResourceFacingServiceType;

public class NotificationParseTest {

  private List<String> packages = Lists.newArrayList(
    "org.tmforum.mtop.fmw.xsd.notmsg.v1",
    "org.tmforum.mtop.fmw.xsd.cei.v1",
    "org.tmforum.mtop.fmw.xsd.ei.v1",
    "org.tmforum.mtop.fmw.xsd.oc.v1",
    "org.tmforum.mtop.sb.xsd.soc.v1",
    "org.tmforum.mtop.fmw.xsd.hbt.v1",
    "org.tmforum.mtop.sb.xsd.savc.v1",
     "org.tmforum.mtop.sb.xsd.svc.v1");

  @Test
  public void parse_service_object_creation_notification() throws Exception {
    Unmarshaller unmarshaller = JAXBContext.newInstance(Joiner.on(":").join(packages)).createUnmarshaller();

    Notify notify = (Notify) unmarshaller.unmarshal(new File("src/test/resources/mtosi/serviceObjectCreation.xml"));

    List<JAXBElement<? extends CommonEventInformationType>> events = notify.getMessage().getCommonEventInformation();
    assertThat(events, hasSize(1));

    CommonEventInformationType event = events.get(0).getValue();

    assertThat(event, instanceOf(ServiceObjectCreationType.class));
  }

  @Test
  public void parse_service_state_change_provisioned() throws Exception {
    Unmarshaller unmarshaller = JAXBContext.newInstance(Joiner.on(":").join(packages)).createUnmarshaller();

    Notify notify = (Notify) unmarshaller.unmarshal(new File("src/test/resources/mtosi/serviceAttributeValueChange-provisioned.xml"));

    List<JAXBElement<? extends CommonEventInformationType>> events = notify.getMessage().getCommonEventInformation();

    assertThat(events, hasSize(1));


    CommonEventInformationType event = events.get(0).getValue();
    ServiceAttributeValueChangeType serviceAttributeValueChangeType = (ServiceAttributeValueChangeType) event;

    Object any = serviceAttributeValueChangeType.getAttributeList().getAny();

    assertThat(any, instanceOf(ResourceFacingServiceType.class));
  }

  @Test
  public void parse_heartbeat_notification() throws Exception {
    Unmarshaller unmarshaller = JAXBContext.newInstance(Joiner.on(":").join(packages)).createUnmarshaller();

    Notify notify = (Notify) unmarshaller.unmarshal(new File("src/test/resources/mtosi/heartbeat.xml"));

    List<JAXBElement<? extends CommonEventInformationType>> events = notify.getMessage().getCommonEventInformation();
    assertThat(events, hasSize(1));

    CommonEventInformationType event = events.get(0).getValue();

    assertThat(event, instanceOf(HeartbeatType.class));
  }

}
