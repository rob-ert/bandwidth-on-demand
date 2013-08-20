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
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
  @Mock private SnmpAgentService snmpAgentServiceMock;
  @Mock private VirtualPortService virtualPortServiceMock;

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
    when(virtualPortServiceMock.findAllForPhysicalPort(port)).thenReturn(virtualPorts);

    subject.delete(port.getId());

    verify(virtualPortServiceMock).deleteVirtualPorts(virtualPorts, user);
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
  public void countUnallocatedShouldCalculateDifferenceBetweenNbiAndRepoPorts() {
    when(nbiClientMock.getPhysicalPortsCount()).thenReturn(15L);
    when(physicalPortRepoMock.count()).thenReturn(10L);

    long countUnallocated = subject.countUnallocated();

    assertThat(countUnallocated, is(5L));
  }

  @Test
  public void shouldFindTwoDissapearedPorts() {
    List<PhysicalPort> dissapearedPorts = subject.markUnalignedWithNMS(allocatedPorts("1", "2", "3"), nbiPorts("3"));

    assertThat(dissapearedPorts, hasSize(2));
    assertThat(dissapearedPorts, hasItems(physicalPortMap.get("1"), physicalPortMap.get("2")));
  }

  @Test
  public void shouldFindPortTypeChanges() {
    List<PhysicalPort> unalignedPorts = subject.markUnalignedWithNMS(
      Arrays.<PhysicalPort> asList(
        new PhysicalPortFactory().setNbiPort(new NbiPortFactory().setNmsPortId("1").setVlanRequired(false).create()).create(),
        new PhysicalPortFactory().setNbiPort(new NbiPortFactory().setNmsPortId("2").setVlanRequired(true).create()).create()),
      Arrays.asList(
        new NbiPortFactory().setNmsPortId("1").setVlanRequired(true).create(),
        new NbiPortFactory().setNmsPortId("2").setVlanRequired(false).create()));

    assertThat(unalignedPorts, hasSize(2));
    assertThat(unalignedPorts.get(0).getNmsAlignmentStatus(), is(NmsAlignmentStatus.TYPE_CHANGED_TO_VLAN));
    assertThat(unalignedPorts.get(1).getNmsAlignmentStatus(), is(NmsAlignmentStatus.TYPE_CHANGED_TO_LAN));
  }

  @Test
  public void shouldOnlyFindNonMissingDisappearedPorts() {
    physicalPortMap.get("2").setNmsAlignmentStatus(NmsAlignmentStatus.DISAPPEARED);
    List<PhysicalPort> dissapearedPorts = subject.markUnalignedWithNMS(allocatedPorts("1", "2", "3"), nbiPorts("3"));

    assertThat(dissapearedPorts, hasSize(1));
    assertThat(dissapearedPorts, hasItems(physicalPortMap.get("1")));
  }

  @Test
  public void shouldFindNoDissapearedPorts() {
    List<PhysicalPort> dissapearedPorts = subject.markUnalignedWithNMS(allocatedPorts("1", "2", "3"), nbiPorts("1", "2", "3"));

    assertThat(dissapearedPorts, hasSize(0));
  }

  @Test
  public void shouldFindOneDissapearedPorts() {
    List<PhysicalPort> dissapearedPorts = subject.markUnalignedWithNMS(allocatedPorts("1", "2", "3"), nbiPorts("1", "3"));

    assertThat(dissapearedPorts, hasSize(1));
    assertThat(dissapearedPorts, hasItems(physicalPortMap.get("2")));

    verify(snmpAgentServiceMock, times(1)).sendMissingPortEvent("2");
  }

  @Test
  public void shouldFindNoDissapearedPortsWhenWithNewPort() {
    List<PhysicalPort> dissapearedPorts = subject.markUnalignedWithNMS(allocatedPorts("1", "2", "3"), Arrays.asList(new NbiPortFactory().setNmsPortId("4").create()));

    assertThat(dissapearedPorts, hasSize(3));
    assertThat(dissapearedPorts, hasItems(physicalPortMap.get("1"), physicalPortMap.get("2"), physicalPortMap.get("3")));
  }

  @Test
  public void shouldFindNoReappearedPorts() {
    physicalPortMap.get("1").setNmsAlignmentStatus(NmsAlignmentStatus.ALIGNED);

    List<PhysicalPort> reappearedPorts = subject.markRealignedPortsInNMS(allocatedPorts("1", "2", "3"), nbiPorts());

    assertThat(reappearedPorts, hasSize(0));
  }

  @Test
  public void shouldFindOneReappearedPorts() {
    // Mark missing
    PhysicalPort portOne = physicalPortMap.get("1");
    portOne.setNmsAlignmentStatus(NmsAlignmentStatus.DISAPPEARED);

    List<PhysicalPort> reappearedPorts = subject.markRealignedPortsInNMS(allocatedPorts("1", "2", "3"), nbiPorts("1", "2"));

    assertThat(reappearedPorts, hasSize(1));
    assertThat(reappearedPorts, hasItems(portOne));
    assertThat(reappearedPorts.get(0).getNmsAlignmentStatus(), is(NmsAlignmentStatus.ALIGNED));
  }

  @Test
  public void shouldFindOneRealignedPortAfterTypeChange() {
    PhysicalPort portOne = physicalPortMap.get("1");
    portOne.setNmsAlignmentStatus(NmsAlignmentStatus.TYPE_CHANGED_TO_VLAN);
    PhysicalPort portTwo = physicalPortMap.get("2");
    portTwo.setNmsAlignmentStatus(NmsAlignmentStatus.DISAPPEARED);

    List<PhysicalPort> realignedPorts = subject.markRealignedPortsInNMS(
        Arrays.asList(portOne, portTwo),
        Arrays.asList(
            new NbiPortFactory().setNmsPortId("1").setVlanRequired(false).create(),
            new NbiPortFactory().setNmsPortId("2").setVlanRequired(true).create()));

    assertThat(realignedPorts, hasSize(1));
    assertThat(realignedPorts, hasItems(portOne));
    assertThat(portOne.getNmsAlignmentStatus(), is(NmsAlignmentStatus.ALIGNED));

    assertThat(portTwo.getNmsAlignmentStatus(), is(NmsAlignmentStatus.TYPE_CHANGED_TO_VLAN));
  }

  @Test
  public void shouldFindNoReappearedPortsSinceItWasNotMissing() {
    // Mark missing
    PhysicalPort portOne = physicalPortMap.get("1");
    portOne.setNmsAlignmentStatus(NmsAlignmentStatus.ALIGNED);

    List<PhysicalPort> reappearedPorts = subject.markRealignedPortsInNMS(allocatedPorts("1", "2", "3"), nbiPorts("1"));

    assertThat(reappearedPorts, hasSize(0));
  }

  @Test
  public void shouldChangeNoPorts() {
    when(nbiClientMock.findAllPorts()).thenReturn(Lists.newArrayList(nbiPortMap.values()));
    when(physicalPortRepoMock.findAll()).thenReturn(Lists.newArrayList(physicalPortMap.values()));

    subject.detectAndPersistPortInconsistencies();

    for (PhysicalPort port : physicalPortMap.values()) {
      assertThat(port.isAlignedWithNMS(), is(true));
    }

    verify(physicalPortRepoMock, times(2)).save(anyListOf(UniPort.class));
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

    verify(physicalPortRepoMock, times(2)).save(anyListOf(UniPort.class));
  }

  @Test
  public void shouldFindOneDisappearingPort() {
    when(nbiClientMock.findAllPorts()).thenReturn(Lists.newArrayList(nbiPortMap.get("1"), nbiPortMap.get("2")));
    when(physicalPortRepoMock.findAll()).thenReturn(Lists.newArrayList(physicalPortMap.values()));

    subject.detectAndPersistPortInconsistencies();

    assertThat(physicalPortMap.get("1").isAlignedWithNMS(), is(true));
    assertThat(physicalPortMap.get("2").isAlignedWithNMS(), is(true));
    assertThat(physicalPortMap.get("3").isAlignedWithNMS(), is(false));

    verify(physicalPortRepoMock, times(2)).save(anyListOf(UniPort.class));
  }

  private List<PhysicalPort> allocatedPorts(String... portIds) {
    List<PhysicalPort> result = Lists.newArrayList();
    for (String portId: portIds) {
      assertThat("port not found", physicalPortMap, hasKey(portId));
      result.add(physicalPortMap.get(portId));
    }
    return result;
  }

  private List<NbiPort> nbiPorts(String... portIds) {
    List<NbiPort> result = Lists.newArrayList();
    for (String portId: portIds) {
      assertThat("port not found", physicalPortMap, hasKey(portId));
      result.add(nbiPortMap.get(portId));
    }
    return result;
  }
}
