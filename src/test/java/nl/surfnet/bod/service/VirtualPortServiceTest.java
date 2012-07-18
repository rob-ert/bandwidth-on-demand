/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
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

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.domain.VirtualPortRequestLink.RequestStatus;
import nl.surfnet.bod.nsi.ws.NsiConstants;
import nl.surfnet.bod.repo.VirtualPortRepo;
import nl.surfnet.bod.repo.VirtualPortRequestLinkRepo;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
import nl.surfnet.bod.support.*;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class VirtualPortServiceTest {

  @InjectMocks
  private VirtualPortService subject;

  @Mock
  private VirtualPortRepo virtualPortRepoMock;

  @Mock
  private ReservationService reservationService;

  @Mock
  private VirtualPortRequestLinkRepo virtualPortRequestLinkRepoMock;

  @Mock
  private VirtualResourceGroupRepo virtualResourceGroupRepoMock;

  @Mock
  private EmailSender emailSenderMock;

  @SuppressWarnings(value = "unused")
  @Mock
  private LogEventService logEventService;

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
    when(reservationService.findBySourcePortOrDestinationPort(virtualPort, virtualPort)).thenReturn(
        new ArrayList<Reservation>());
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
    subject.findEntries(1, 0);
  }

  @Test
  public void findEntries() {
    VirtualPort port = new VirtualPortFactory().create();

    when(virtualPortRepoMock.findAll(any(PageRequest.class))).thenReturn(
        new PageImpl<VirtualPort>(Lists.newArrayList(port)));

    List<VirtualPort> ports = subject.findEntries(5, 10);

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
  public void rqeuestNewVirtualPort() {
    RichUserDetails user = new RichUserDetailsFactory().create();
    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();
    PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().create();

    ArgumentCaptor<VirtualPortRequestLink> request = ArgumentCaptor.forClass(VirtualPortRequestLink.class);

    subject.requestNewVirtualPort(user, vrg, prg, "new port", 1000, "I would like to have this port, now");

    verify(virtualPortRequestLinkRepoMock).save(request.capture());
    VirtualPortRequestLink link = request.getValue();

    verify(emailSenderMock).sendVirtualPortRequestMail(user, link);

    assertThat(link.getMessage(), is("I would like to have this port, now"));
    assertThat(link.getMinBandwidth(), is(1000));
    assertThat(link.getRequestorEmail(), is(user.getEmail()));
    assertThat(link.getRequestorName(), is(user.getDisplayName()));
    assertThat(link.getPhysicalResourceGroup(), is(prg));
    assertThat(link.getVirtualResourceGroup(), is(vrg));
  }

  @Test
  public void linkApprovedShouldChangesStatusAndSentMail() {
    VirtualPortRequestLink link = new VirtualPortRequestLinkFactory().setStatus(RequestStatus.PENDING).create();
    VirtualPort port = new VirtualPortFactory().create();

    subject.requestLinkApproved(link, port);

    assertThat(link.getStatus(), is(RequestStatus.APPROVED));

    verify(virtualPortRequestLinkRepoMock).save(link);
    verify(emailSenderMock).sendVirtualPortRequestApproveMail(link, port);
  }

  @Test
  public void linkDeclinedShouldChangeStatusAndSendMail() {
    VirtualPortRequestLink link = new VirtualPortRequestLinkFactory().setStatus(RequestStatus.PENDING).create();

    subject.requestLinkDeclined(link, "I don't like you");

    assertThat(link.getStatus(), is(RequestStatus.DECLINED));

    verify(virtualPortRequestLinkRepoMock).save(link);
    verify(emailSenderMock).sendVirtualPortRequestDeclineMail(link, "I don't like you");
  }

  @Test
  public void findByNsiStpId() {
    VirtualPort port = new VirtualPortFactory().create();
    when(virtualPortRepoMock.findOne(25L)).thenReturn(port);

    VirtualPort foundPort = subject.findByNsiStpId(NsiConstants.NS_NETWORK + ":25");

    assertThat(foundPort, is(port));
  }

  @Test
  public void findByIllegalNsiStpIdWithWrongNetworkId() {
    VirtualPort foundPort = subject.findByNsiStpId(NsiConstants.NS_NETWORK + ":asdfasfasdf");

    assertThat(foundPort, is(nullValue()));
    verifyZeroInteractions(virtualPortRepoMock);
  }

  @Test
  public void findByIllegalNsiStpIdWithWrongNsNetwork() {
    VirtualPort foundPort = subject.findByNsiStpId("urn:ogf:network:nsnetwork:zilverline.nl" + ":25");

    assertThat(foundPort, is(nullValue()));
    verifyZeroInteractions(virtualPortRepoMock);
  }
}
