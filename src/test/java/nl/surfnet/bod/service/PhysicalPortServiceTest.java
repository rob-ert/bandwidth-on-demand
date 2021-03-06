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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.NmsAlignmentStatus;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.UniPort;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.nbi.PortNotAvailableException;
import nl.surfnet.bod.repo.PhysicalPortRepo;
import nl.surfnet.bod.repo.UniPortRepo;
import nl.surfnet.bod.support.NbiPortFactory;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalPortServiceTest {

  @InjectMocks
  private PhysicalPortService subject;

  @Mock private NbiClient nbiClientMock;
  @Mock private PhysicalPortRepo physicalPortRepoMock;
  @Mock private UniPortRepo uniPortRepoMock;
  @Mock private Environment environmentMock;
  @Mock private LogEventService logEventServiceMock;
  @Mock private VirtualPortService virtualPortServiceMock;
  @Mock private ReservationService reservationServiceMock;

  private Map<String, PhysicalPort> physicalPortMap;
  private Map<String, NbiPort> nbiPortMap;
  private RichUserDetails user;

  @Before
  public void setUp() {
    user = new RichUserDetailsFactory().addUserGroup("urn:my-group").addUserGroup("urn:test:group").create();
    Security.setUserDetails(user);

    List<PhysicalPort> physicalPorts = Lists.<PhysicalPort> newArrayList(
        new PhysicalPortFactory().setId(1L).setNbiPort(new NbiPortFactory().setNmsPortId("1").create()).create(),
        new PhysicalPortFactory().setId(2L).setNbiPort(new NbiPortFactory().setNmsPortId("2").create()).create(),
        new PhysicalPortFactory().setId(3L).setNbiPort(new NbiPortFactory().setNmsPortId("3").create()).create());

    physicalPortMap = PhysicalPortService.buildPhysicalPortIdMap(physicalPorts);
    nbiPortMap = ImmutableMap.of("1", physicalPorts.get(0).getNbiPort(), "2", physicalPorts.get(1).getNbiPort(), "3", physicalPorts.get(2).getNbiPort());
  }

  @Test
  public void allUnallocatedPortsShouldNotContainAllocatedPorts() {
    List<NbiPort> nbiPorts = Lists.newArrayList(
      new NbiPortFactory().setNmsPortId("first").create(),
      new NbiPortFactory().setNmsPortId("second").create());

    List<PhysicalPort> repoPorts = Lists.<PhysicalPort> newArrayList(
      new PhysicalPortFactory().setNbiPort(new NbiPortFactory().setNmsPortId("first").create()).setId(1L).create()
    );

    when(nbiClientMock.findAllPorts()).thenReturn(nbiPorts);
    when(physicalPortRepoMock.findAll()).thenReturn(repoPorts);

    Collection<NbiPort> unallocatedPorts = subject.findUnallocated();

    assertThat(unallocatedPorts, hasSize(1));
    assertThat(unallocatedPorts.iterator().next().getNmsPortId(), is("second"));
  }

  @Test
  public void findByNmsPortIdShouldGiveNullIfNotFound() throws PortNotAvailableException {
    when(nbiClientMock.findPhysicalPortByNmsPortId("first")).thenReturn(null);
    when(physicalPortRepoMock.findByNbiPortNmsPortId("first")).thenReturn(null);

    PhysicalPort port = subject.findByNmsPortId("first");

    assertThat(port, nullValue());
  }

  @Test
  public void findByNmsPortIdhouldGiveAMergedPortIfFound() throws PortNotAvailableException {
    NbiPort nbiPort = new NbiPortFactory().setNmsPortId("first").create();
    UniPort repoPort = new PhysicalPortFactory().setId(1L).setNbiPort(nbiPort).create();

    when(nbiClientMock.findPhysicalPortByNmsPortId("first")).thenReturn(nbiPort);
    when(physicalPortRepoMock.findByNbiPortNmsPortId("first")).thenReturn(repoPort);

    PhysicalPort port = subject.findByNmsPortId("first");

    assertThat(port.getNmsPortId(), is("first"));
    assertThat(port.getId(), is(1L));
  }

  @Test
  public void findAllocatedEntriesShouldReturnMaxAvailablePorts() {
    List<UniPort> ports = Lists.newArrayList(
      new PhysicalPortFactory().setNocLabel("first").create(),
      new PhysicalPortFactory().setNocLabel("second").create());

    when(uniPortRepoMock.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(ports));

    List<UniPort> entries = subject.findAllocatedUniEntries(0, 20, new Sort("id"));

    assertThat(entries, hasSize(2));
  }

  @Test
  public void updateShouldCallSaveOnRepo() {
    UniPort port = new PhysicalPortFactory().create();

    subject.update(port);

    verify(physicalPortRepoMock, only()).save(port);
  }

  @Test
  public void delete_should_delete_virtual_ports() {
    UniPort port = new PhysicalPortFactory().create();
    Collection<VirtualPort> virtualPorts = Lists.newArrayList(new VirtualPortFactory().create());

    when(physicalPortRepoMock.findOne(port.getId())).thenReturn(port);
    when(virtualPortServiceMock.findAllForUniPort(port)).thenReturn(virtualPorts);

    subject.delete(port.getId());

    verify(physicalPortRepoMock).delete(port);
  }

  @Test
  public void saveShouldCallSaveOnRepo() {
    UniPort port = new PhysicalPortFactory().create();

    subject.save(port);

    verify(physicalPortRepoMock, only()).save(port);
  }

  @Test
  public void findShouldCallFindOnRepo() {
    subject.find(1L);

    verify(physicalPortRepoMock, only()).findOne(1L);
  }

  @Test
  public void countAllocatedShouldCallCountOnRepo() {
    subject.countAllocated();

    verify(physicalPortRepoMock, only()).count();
  }

  @Test
  public void shouldChangeNoPorts() {
    when(nbiClientMock.findAllPorts()).thenReturn(Lists.newArrayList(nbiPortMap.values()));
    when(physicalPortRepoMock.findAll()).thenReturn(Lists.newArrayList(physicalPortMap.values()));

    subject.detectAndPersistPortInconsistencies();

    for (PhysicalPort port : physicalPortMap.values()) {
      assertThat(port.isAlignedWithNMS(), is(true));
    }

    verify(physicalPortRepoMock, times(3)).save(anyListOf(UniPort.class));
  }

  @Test
  public void shouldFindOneReappearingPort() {
    physicalPortMap.get("1").setNmsAlignmentStatus(NmsAlignmentStatus.DISAPPEARED);
    when(nbiClientMock.findAllPorts()).thenReturn(Lists.newArrayList(nbiPortMap.values()));
    when(physicalPortRepoMock.findAll()).thenReturn(Lists.newArrayList(physicalPortMap.values()));

    subject.detectAndPersistPortInconsistencies();

    for (PhysicalPort port : physicalPortMap.values()) {
      assertThat(port.isAlignedWithNMS(), is(true));
    }

    verify(physicalPortRepoMock, times(3)).save(anyListOf(UniPort.class));
  }

  @Test
  public void shouldFindOneDisappearingPort() {
    when(nbiClientMock.findAllPorts()).thenReturn(Lists.newArrayList(nbiPortMap.get("1"), nbiPortMap.get("2")));
    when(physicalPortRepoMock.findAll()).thenReturn(Lists.newArrayList(physicalPortMap.values()));

    subject.detectAndPersistPortInconsistencies();

    assertThat(physicalPortMap.get("1").isAlignedWithNMS(), is(true));
    assertThat(physicalPortMap.get("2").isAlignedWithNMS(), is(true));
    assertThat(physicalPortMap.get("3").isAlignedWithNMS(), is(false));

    verify(physicalPortRepoMock, times(3)).save(anyListOf(UniPort.class));
  }

}
