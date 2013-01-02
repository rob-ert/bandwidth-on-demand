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
package nl.surfnet.bod.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.repo.PhysicalPortRepo;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.util.Environment;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalPortServiceImplTest {

  @InjectMocks
  private PhysicalPortService subject;

  @Mock
  private NbiClient nbiClientMock;

  @Mock
  private PhysicalPortRepo physicalPortRepoMock;

  @Mock
  private Environment environmentMock;

  @Mock
  private LogEventService logEventService;
  
  @Mock
  private SnmpAgentService snmpAgentService;

  private Map<String, PhysicalPort> physicalPortMap = Maps.newHashMap();

  @Before
  public void setUp() {
    
    Security.setUserDetails(new RichUserDetailsFactory().addUserGroup("urn:my-group").addUserGroup("urn:test:group")
        .create());
    
    ArrayList<PhysicalPort> physicalPorts = Lists.newArrayList(new PhysicalPortFactory().setNmsPortId("1").create(),
        new PhysicalPortFactory().setNmsPortId("2").create(), new PhysicalPortFactory().setNmsPortId("3").create());

    physicalPortMap.put(physicalPorts.get(0).getNmsPortId(), physicalPorts.get(0));
    physicalPortMap.put(physicalPorts.get(1).getNmsPortId(), physicalPorts.get(1));
    physicalPortMap.put(physicalPorts.get(2).getNmsPortId(), physicalPorts.get(2));
  }

  @Test
  public void findAllShouldMergePorts() {
    List<PhysicalPort> nbiPorts = Lists.newArrayList(
        new PhysicalPortFactory().setNocLabel("noc label").setNmsPortId("first").setId(null).create(),
        new PhysicalPortFactory().setNocLabel("noc label").setNmsPortId("second").setId(null).create());

    List<PhysicalPort> repoPorts = Lists.newArrayList(new PhysicalPortFactory().setNocLabel("overwrite")
        .setNmsPortId("first").setId(1L).setVersion(2).create());

    when(nbiClientMock.findAllPhysicalPorts()).thenReturn(nbiPorts);
    when(physicalPortRepoMock.findAll()).thenReturn(repoPorts);

    List<PhysicalPort> allPorts = subject.findAll();

    assertThat(allPorts, hasSize(2));
    assertThat(allPorts.get(0).getId(), is(1L));
    assertThat(allPorts.get(0).getVersion(), is(2));
    assertThat(allPorts.get(0).getNmsPortId(), is("first"));
    assertThat(allPorts.get(0).getNocLabel(), is("overwrite"));

    assertThat(allPorts.get(1).getId(), nullValue());
    assertThat(allPorts.get(1).getNmsPortId(), is("second"));
    assertThat(allPorts.get(1).getNocLabel(), is("noc label"));
  }

  @Test
  public void allUnallocatedPortsShouldNotContainOnesWithId() {
    List<PhysicalPort> nbiPorts = Lists.newArrayList(new PhysicalPortFactory().setNmsPortId("first").setId(null)
        .create(), new PhysicalPortFactory().setNmsPortId("second").setId(null).create());

    List<PhysicalPort> repoPorts = Lists.newArrayList(new PhysicalPortFactory().setNmsPortId("first").setId(1L)
        .create());

    when(nbiClientMock.findAllPhysicalPorts()).thenReturn(nbiPorts);
    when(physicalPortRepoMock.findAll()).thenReturn(repoPorts);

    Collection<PhysicalPort> unallocatedPorts = subject.findUnallocated();

    assertThat(unallocatedPorts, hasSize(1));
    assertThat(unallocatedPorts.iterator().next().getNmsPortId(), is("second"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void findAllPortsWithSameNameShouldGiveAnException() {
    List<PhysicalPort> nbiPorts = Lists.newArrayList(new PhysicalPortFactory().setNmsPortId("first").create());
    List<PhysicalPort> repoPorts = Lists.newArrayList(new PhysicalPortFactory().setNmsPortId("first").create(),
        new PhysicalPortFactory().setNmsPortId("first").create());

    when(nbiClientMock.findAllPhysicalPorts()).thenReturn(nbiPorts);
    when(physicalPortRepoMock.findAll()).thenReturn(repoPorts);

    subject.findAll();
  }

  @Test
  public void findByNmsPortIdShouldGiveNullIfNotFound() {
    when(nbiClientMock.findPhysicalPortByNmsPortId("first")).thenReturn(null);
    when(physicalPortRepoMock.findByNmsPortId("first")).thenReturn(null);

    PhysicalPort port = subject.findByNmsPortId("first");

    assertThat(port, nullValue());
  }

  @Test
  public void findByNmsPortIdhouldGiveAMergedPortIfFound() {
    PhysicalPort nbiPort = new PhysicalPortFactory().setNmsPortId("first").create();
    PhysicalPort repoPort = new PhysicalPortFactory().setId(1L).setNmsPortId("first").create();

    when(nbiClientMock.findPhysicalPortByNmsPortId("first")).thenReturn(nbiPort);
    when(physicalPortRepoMock.findByNmsPortId("first")).thenReturn(repoPort);

    PhysicalPort port = subject.findByNmsPortId("first");

    assertThat(port.getNmsPortId(), is("first"));
    assertThat(port.getId(), is(1L));
  }

  @Test
  public void findAllocatedEntriesShouldReturnMaxAvailablePorts() {
    List<PhysicalPort> ports = Lists.newArrayList(new PhysicalPortFactory().setNocLabel("first").create(),
        new PhysicalPortFactory().setNocLabel("second").create());

    when(physicalPortRepoMock.findAll(any(Pageable.class))).thenReturn(new PageImpl<PhysicalPort>(ports));

    List<PhysicalPort> entries = subject.findAllocatedEntries(0, 20, new Sort("id"));

    assertThat(entries, hasSize(2));
  }

  @Test
  public void updateShouldCallSaveOnRepo() {
    PhysicalPort port = new PhysicalPortFactory().create();

    subject.update(port);

    verify(physicalPortRepoMock, only()).save(port);
  }

  @Test
  public void deleteShouldCallDeleteOnRepo() {
    PhysicalPort port = new PhysicalPortFactory().create();

    subject.delete(port);

    verify(physicalPortRepoMock, only()).delete(port);
  }

  @Test
  public void saveShouldCallSaveOnRepo() {
    PhysicalPort port = new PhysicalPortFactory().create();

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
    Set<String> nbiPortIds = Sets.newHashSet("3");

    List<PhysicalPort> dissapearedPorts = subject.markUnalignedWithNMS(physicalPortMap, nbiPortIds);

    assertThat(dissapearedPorts, hasSize(2));
    assertThat(dissapearedPorts, hasItems(physicalPortMap.get("1"), physicalPortMap.get("2")));
  }

  @Test
  public void shouldOnlyFindNonMissingDisappearedPorts() {
    Set<String> nbiPortIds = Sets.newHashSet("3");

    physicalPortMap.get("2").setAlignedWithNMS(false);
    List<PhysicalPort> dissapearedPorts = subject.markUnalignedWithNMS(physicalPortMap, nbiPortIds);

    assertThat(dissapearedPorts, hasSize(1));
    assertThat(dissapearedPorts, hasItems(physicalPortMap.get("1")));
  }

  @Test
  public void shouldFindNoDissapearedPorts() {
    Set<String> nbiPortIds = Sets.newHashSet("1", "2", "3");

    List<PhysicalPort> dissapearedPorts = subject.markUnalignedWithNMS(physicalPortMap, nbiPortIds);

    assertThat(dissapearedPorts, hasSize(0));
  }

  @Test
  public void shouldFindOneDissapearedPorts() {
    Set<String> nbiPortIds = Sets.newHashSet("1", "3");

    List<PhysicalPort> dissapearedPorts = subject.markUnalignedWithNMS(physicalPortMap, nbiPortIds);

    assertThat(dissapearedPorts, hasSize(1));
    assertThat(dissapearedPorts, hasItems(physicalPortMap.get("2")));
  }

  @Test
  public void shouldFindNoDissapearedPortsWhenWithNewPort() {
    Set<String> nbiPortIds = Sets.newHashSet("4");

    List<PhysicalPort> dissapearedPorts = subject.markUnalignedWithNMS(physicalPortMap, nbiPortIds);

    assertThat(dissapearedPorts, hasSize(3));
    assertThat(dissapearedPorts, hasItems(physicalPortMap.get("1"), physicalPortMap.get("2"), physicalPortMap.get("3")));
  }

  @Test
  public void shouldFindNoReappearedPorts() {
    Set<String> nbiPortIds = Sets.newHashSet();
    physicalPortMap.get("1").setAlignedWithNMS(true);

    List<PhysicalPort> reappearedPorts = subject.markRealignedPortsInNMS(physicalPortMap, nbiPortIds);

    assertThat(reappearedPorts, hasSize(0));
  }

  @Test
  public void shouldFindOneReappearedPorts() {
    Set<String> nbiPortIds = Sets.newHashSet("1", "2");

    // Mark missing
    PhysicalPort portOne = physicalPortMap.get("1");
    portOne.setAlignedWithNMS(false);

    List<PhysicalPort> reappearedPorts = subject.markRealignedPortsInNMS(physicalPortMap, nbiPortIds);

    assertThat(reappearedPorts, hasSize(1));
    assertThat(reappearedPorts, hasItems(portOne));
  }

  @Test
  public void shouldFindNoReappearedPortsSinceItWasNotMissing() {
    Set<String> nbiPortIds = Sets.newHashSet("1");

    // Mark missing
    PhysicalPort portOne = physicalPortMap.get("1");
    portOne.setAlignedWithNMS(true);

    List<PhysicalPort> reappearedPorts = subject.markRealignedPortsInNMS(physicalPortMap, nbiPortIds);

    assertThat(reappearedPorts, hasSize(0));
  }

  @Test
  public void shouldChangeNoPorts() {

    when(nbiClientMock.findAllPhysicalPorts()).thenReturn(Lists.newArrayList(physicalPortMap.values()));
    when(physicalPortRepoMock.findAll()).thenReturn(Lists.newArrayList(physicalPortMap.values()));

    subject.detectAndPersistPortInconsistencies();

    for (PhysicalPort port : physicalPortMap.values()) {
      assertThat(port.isAlignedWithNMS(), is(true));
    }

    verify(physicalPortRepoMock, times(2)).save(anyListOf(PhysicalPort.class));
  }

  @Test
  public void shouldFindOneReappearingPort() {

    physicalPortMap.get("1").setAlignedWithNMS(false);
    when(nbiClientMock.findAllPhysicalPorts()).thenReturn(Lists.newArrayList(physicalPortMap.values()));
    when(physicalPortRepoMock.findAll()).thenReturn(Lists.newArrayList(physicalPortMap.values()));

    subject.detectAndPersistPortInconsistencies();

    for (PhysicalPort port : physicalPortMap.values()) {
      assertThat(port.isAlignedWithNMS(), is(true));
    }

    verify(physicalPortRepoMock, times(2)).save(anyListOf(PhysicalPort.class));
  }

  @Test
  public void shouldFindOneDisappearingPort() {

    when(nbiClientMock.findAllPhysicalPorts()).thenReturn(
        Lists.newArrayList(physicalPortMap.get("1"), physicalPortMap.get("2")));
    when(physicalPortRepoMock.findAll()).thenReturn(Lists.newArrayList(physicalPortMap.values()));

    subject.detectAndPersistPortInconsistencies();

    assertThat(physicalPortMap.get("1").isAlignedWithNMS(), is(true));
    assertThat(physicalPortMap.get("2").isAlignedWithNMS(), is(true));
    assertThat(physicalPortMap.get("3").isAlignedWithNMS(), is(false));

    verify(physicalPortRepoMock, times(2)).save(anyListOf(PhysicalPort.class));
  }

}
