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
package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.AbstractRequestLink.RequestStatus;
import nl.surfnet.bod.domain.NsiVersion;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualPortCreateRequestLink;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.repo.VirtualPortRepo;
import nl.surfnet.bod.repo.VirtualPortCreateRequestLinkRepo;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualPortCreateRequestLinkFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@RunWith(MockitoJUnitRunner.class)
public class VirtualPortServiceTest {

  @InjectMocks private VirtualPortService subject;

  @Mock private VirtualPortRepo virtualPortRepoMock;
  @Mock private ReservationService reservationService;
  @Mock private VirtualPortCreateRequestLinkRepo virtualPortRequestLinkRepoMock;
  @Mock private VirtualResourceGroupRepo virtualResourceGroupRepoMock;
  @Mock private EmailSender emailSenderMock;
  @Mock private LogEventService logEventService;
  @Mock private NsiHelper nsiHelper;

  private RichUserDetails user;

  @org.junit.Before
  public void setUp() {
    user = new RichUserDetailsFactory().create();
    Security.setUserDetails(user);
  }

  @Test
  public void countShouldCount() {
    when(virtualPortRepoMock.count()).thenReturn(2L);

    long count = subject.count();

    assertThat(count, is(2L));
  }

  @Test
  public void delete() {
    VirtualPort virtualPort = new VirtualPortFactory().create();
    when(reservationService.findByVirtualPort(virtualPort)).thenReturn(new ArrayList<Reservation>());
    subject.delete(virtualPort, user);

    verify(virtualPortRepoMock).delete(virtualPort);
    verify(virtualResourceGroupRepoMock).delete(virtualPort.getVirtualResourceGroup());
  }

  @Test
  public void update() {
    VirtualPort virtualPort = new VirtualPortFactory().create();

    subject.update(virtualPort);

    verify(virtualPortRepoMock).save(virtualPort);
  }

  @Test
  public void findAll() {
    VirtualPort port = new VirtualPortFactory().create();
    when(virtualPortRepoMock.findAll()).thenReturn(Lists.newArrayList(port));

    List<VirtualPort> ports = subject.findAll();

    assertThat(ports, contains(port));
  }

  @Test(expected = IllegalArgumentException.class)
  public void findEntriesWithMaxResultZeroShouldGiveAnException() {
    subject.findEntries(1, 0, new Sort("id"));
  }

  @Test
  public void findEntries() {
    VirtualPort port = new VirtualPortFactory().create();

    when(virtualPortRepoMock.findAll(any(PageRequest.class))).thenReturn(
        new PageImpl<VirtualPort>(Lists.newArrayList(port)));

    List<VirtualPort> ports = subject.findEntries(5, 10, new Sort("id"));

    assertThat(ports, contains(port));
  }

  @Test
  public void findAllForUserWithoutGroupsShouldNotGoToRepo() {
    List<VirtualPort> ports = subject.findAllForUser(new RichUserDetailsFactory().create());

    assertThat(ports, hasSize(0));
    verifyZeroInteractions(virtualPortRepoMock);
  }

  @Test
  public void findAllEntriesForUserWithoutGroupsShouldNotGoToRepo() {
    List<VirtualPort> ports = subject.findEntriesForUser(new RichUserDetailsFactory().create(), 2, 5, new Sort("id"));

    assertThat(ports, hasSize(0));
    verifyZeroInteractions(virtualPortRepoMock);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void findAllForUser() {
    when(virtualPortRepoMock.findAll(any(Specification.class))).thenReturn(
        ImmutableList.of(new VirtualPortFactory().create()));

    List<VirtualPort> ports = subject.findAllForUser(new RichUserDetailsFactory().addUserGroup("urn:mygroup").create());

    assertThat(ports, hasSize(1));
  }

  @Test
  public void requestNewVirtualPort() {
    RichUserDetails user = new RichUserDetailsFactory().create();
    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();
    PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().create();

    ArgumentCaptor<VirtualPortCreateRequestLink> request = ArgumentCaptor.forClass(VirtualPortCreateRequestLink.class);

    subject.requestCreateVirtualPort(user, vrg, prg, "new port", 1000L, "I would like to have this port, now");

    verify(virtualPortRequestLinkRepoMock).save(request.capture());
    VirtualPortCreateRequestLink link = request.getValue();

    verify(emailSenderMock).sendVirtualPortCreateRequestMail(user, link);

    assertThat(link.getMessage(), is("I would like to have this port, now"));
    assertThat(link.getMinBandwidth(), is(1000L));
    assertThat(link.getRequestorEmail(), is(user.getEmail().get()));
    assertThat(link.getRequestorName(), is(user.getDisplayName()));
    assertThat(link.getPhysicalResourceGroup(), is(prg));
    assertThat(link.getVirtualResourceGroup(), is(vrg));
  }

  @Test
  public void linkApprovedShouldChangesStatusAndSentMail() {
    VirtualPortCreateRequestLink link = new VirtualPortCreateRequestLinkFactory().setStatus(RequestStatus.PENDING).create();
    VirtualPort port = new VirtualPortFactory().create();

    subject.requestLinkApproved(link, port);

    assertThat(link.getStatus(), is(RequestStatus.APPROVED));

    verify(virtualPortRequestLinkRepoMock).save(link);
    verify(emailSenderMock).sendVirtualPortRequestApproveMail(link, port);
  }

  @Test
  public void linkDeclinedShouldChangeStatusAndSendMail() {
    VirtualPortCreateRequestLink link = new VirtualPortCreateRequestLinkFactory().setStatus(RequestStatus.PENDING).create();

    subject.requestLinkDeclined(link, "I don't like you");

    assertThat(link.getStatus(), is(RequestStatus.DECLINED));

    verify(virtualPortRequestLinkRepoMock).save(link);
    verify(emailSenderMock).sendVirtualPortRequestDeclineMail(link, "I don't like you");
  }

  @Test
  public void findByNsiV1StpId() {
    VirtualPort port = new VirtualPortFactory().create();
    when(virtualPortRepoMock.findOne(25L)).thenReturn(port);
    when(nsiHelper.parseLocalNsiId("urn:ogf:network:surfnet.nl:25", NsiVersion.ONE)).thenReturn("25");

    VirtualPort foundPort = subject.findByNsiV1StpId("urn:ogf:network:surfnet.nl:25");

    assertThat(foundPort, is(port));
  }
  @Test
  public void findByNsiV2StpId() {
    VirtualPort port = new VirtualPortFactory().create();
    when(virtualPortRepoMock.findOne(25L)).thenReturn(port);
    when(nsiHelper.parseLocalNsiId("urn:ogf:network:surfnet.nl:1990:25", NsiVersion.TWO)).thenReturn("25");

    VirtualPort foundPort = subject.findByNsiV2StpId("urn:ogf:network:surfnet.nl:1990:25");

    assertThat(foundPort, is(port));
  }

  @Test
  public void findByIllegalNsiV1StpIdWithWrongNetworkId() {
    when(nsiHelper.parseLocalNsiId("urn:ogf:network:surfnet.nl:asdfasfasdf", NsiVersion.ONE)).thenReturn(null);

    VirtualPort foundPort = subject.findByNsiV1StpId("urn:ogf:network:surfnet.nl:asdfasfasdf");

    assertThat(foundPort, is(nullValue()));
    verifyZeroInteractions(virtualPortRepoMock);
  }

  @Test
  public void findByIllegalNsiStpIdWithWrongNsNetwork() {
    VirtualPort foundPort = subject.findByNsiV1StpId("urn:ogf:network:stp:zilverline.nl" + ":25");

    assertThat(foundPort, is(nullValue()));
    verifyZeroInteractions(virtualPortRepoMock);
  }
}
