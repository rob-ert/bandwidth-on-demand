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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.repo.PhysicalPortRepo;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.util.Environment;

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

@RunWith(MockitoJUnitRunner.class)
public class PhysicalPortServiceImplTest {

  @InjectMocks
  private PhysicalPortServiceImpl subject;

  @Mock
  private NbiClient nbiClientMock;

  @Mock
  private PhysicalPortRepo physicalPortRepoMock;

  @Mock
  private Environment environmentMock;

  private Map<String, PhysicalPort> physicalPortMap = Maps.newHashMap();

  @Before
  public void setUp() {
    ArrayList<PhysicalPort> physicalPorts = Lists.newArrayList(new PhysicalPortFactory().setNetworkElementPk("1")
        .create(), new PhysicalPortFactory().setNetworkElementPk("2").create(), new PhysicalPortFactory()
        .setNetworkElementPk("3").create());

    physicalPortMap.put(physicalPorts.get(0).getNetworkElementPk(), physicalPorts.get(0));
    physicalPortMap.put(physicalPorts.get(1).getNetworkElementPk(), physicalPorts.get(1));
    physicalPortMap.put(physicalPorts.get(2).getNetworkElementPk(), physicalPorts.get(2));
  }

  @Test
  public void findAllShouldMergePorts() {
    List<PhysicalPort> nbiPorts = Lists.newArrayList(new PhysicalPortFactory().setNocLabel("noc label")
        .setNetworkElementPk("first").setId(null).create(), new PhysicalPortFactory().setNocLabel("noc label")
        .setNetworkElementPk("second").setId(null).create());

    List<PhysicalPort> repoPorts = Lists.newArrayList(new PhysicalPortFactory().setNocLabel("overwrite")
        .setNetworkElementPk("first").setId(1L).setVersion(2).create());

    when(nbiClientMock.findAllPhysicalPorts()).thenReturn(nbiPorts);
    when(physicalPortRepoMock.findAll()).thenReturn(repoPorts);

    List<PhysicalPort> allPorts = subject.findAll();

    assertThat(allPorts, hasSize(2));
    assertThat(allPorts.get(0).getId(), is(1L));
    assertThat(allPorts.get(0).getVersion(), is(2));
    assertThat(allPorts.get(0).getNetworkElementPk(), is("first"));
    assertThat(allPorts.get(0).getNocLabel(), is("overwrite"));

    assertThat(allPorts.get(1).getId(), nullValue());
    assertThat(allPorts.get(1).getNetworkElementPk(), is("second"));
    assertThat(allPorts.get(1).getNocLabel(), is("noc label"));
  }

  @Test
  public void allUnallocatedPortsShouldNotContainOnesWithId() {
    List<PhysicalPort> nbiPorts = Lists.newArrayList(new PhysicalPortFactory().setNetworkElementPk("first").setId(null)
        .create(), new PhysicalPortFactory().setNetworkElementPk("second").setId(null).create());

    List<PhysicalPort> repoPorts = Lists.newArrayList(new PhysicalPortFactory().setNetworkElementPk("first").setId(1L)
        .create());

    when(nbiClientMock.findAllPhysicalPorts()).thenReturn(nbiPorts);
    when(physicalPortRepoMock.findAll()).thenReturn(repoPorts);

    Collection<PhysicalPort> unallocatedPorts = subject.findUnallocated();

    assertThat(unallocatedPorts, hasSize(1));
    assertThat(unallocatedPorts.iterator().next().getNetworkElementPk(), is("second"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void findAllPortsWithSameNameShouldGiveAnException() {
    List<PhysicalPort> nbiPorts = Lists.newArrayList(new PhysicalPortFactory().setNetworkElementPk("first").create());
    List<PhysicalPort> repoPorts = Lists.newArrayList(new PhysicalPortFactory().setNetworkElementPk("first").create(),
        new PhysicalPortFactory().setNetworkElementPk("first").create());

    when(nbiClientMock.findAllPhysicalPorts()).thenReturn(nbiPorts);
    when(physicalPortRepoMock.findAll()).thenReturn(repoPorts);

    subject.findAll();
  }

  @Test
  public void findByNetworkElementPkShouldGiveNullIfNotFound() {
    when(nbiClientMock.findPhysicalPortByNetworkElementId("first")).thenReturn(null);
    when(physicalPortRepoMock.findByNetworkElementPk("first")).thenReturn(null);

    PhysicalPort port = subject.findByNetworkElementPk("first");

    assertThat(port, nullValue());
  }

  @Test
  public void findByNetworkElementPkShouldGiveAMergedPortIfFound() {
    PhysicalPort nbiPort = new PhysicalPortFactory().setNetworkElementPk("first").create();
    PhysicalPort repoPort = new PhysicalPortFactory().setId(1L).setNetworkElementPk("first").create();

    when(nbiClientMock.findPhysicalPortByNetworkElementId("first")).thenReturn(nbiPort);
    when(physicalPortRepoMock.findByNetworkElementPk("first")).thenReturn(repoPort);

    PhysicalPort port = subject.findByNetworkElementPk("first");

    assertThat(port.getNetworkElementPk(), is("first"));
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

    List<PhysicalPort> dissapearedPorts = subject.markDisappearedPortsFromNMS(physicalPortMap, nbiPortIds);

    assertThat(dissapearedPorts, hasSize(2));
    assertThat(dissapearedPorts, hasItems(physicalPortMap.get("1"), physicalPortMap.get("2")));
  }

  @Test
  public void shouldOnlyFindNonMissingDisappearedPorts() {
    Set<String> nbiPortIds = Sets.newHashSet("3");

    physicalPortMap.get("2").setMissing(true);
    List<PhysicalPort> dissapearedPorts = subject.markDisappearedPortsFromNMS(physicalPortMap, nbiPortIds);

    assertThat(dissapearedPorts, hasSize(1));
    assertThat(dissapearedPorts, hasItems(physicalPortMap.get("1")));
  }

  @Test
  public void shouldFindNoDissapearedPorts() {
    Set<String> nbiPortIds = Sets.newHashSet("1", "2", "3");

    List<PhysicalPort> dissapearedPorts = subject.markDisappearedPortsFromNMS(physicalPortMap, nbiPortIds);

    assertThat(dissapearedPorts, hasSize(0));
  }

  @Test
  public void shouldFindOneDissapearedPorts() {
    Set<String> nbiPortIds = Sets.newHashSet("1", "3");

    List<PhysicalPort> dissapearedPorts = subject.markDisappearedPortsFromNMS(physicalPortMap, nbiPortIds);

    assertThat(dissapearedPorts, hasSize(1));
    assertThat(dissapearedPorts, hasItems(physicalPortMap.get("2")));
  }

  @Test
  public void shouldFindNoDissapearedPortsWhenWithNewPort() {
    Set<String> nbiPortIds = Sets.newHashSet("4");

    List<PhysicalPort> dissapearedPorts = subject.markDisappearedPortsFromNMS(physicalPortMap, nbiPortIds);

    assertThat(dissapearedPorts, hasSize(3));
    assertThat(dissapearedPorts, hasItems(physicalPortMap.get("1"), physicalPortMap.get("2"), physicalPortMap.get("3")));
  }

  @Test
  public void shouldFindNoReappearedPorts() {
    Set<String> nbiPortIds = Sets.newHashSet();
    physicalPortMap.get("1").setMissing(true);

    List<PhysicalPort> reappearedPorts = subject.markReappearedPortsInNMS(physicalPortMap, nbiPortIds);

    assertThat(reappearedPorts, hasSize(0));
  }

  @Test
  public void shouldFindOneReappearedPorts() {
    Set<String> nbiPortIds = Sets.newHashSet("1");

    // Mark missing
    PhysicalPort portOne = physicalPortMap.get("1");
    portOne.setMissing(true);

    List<PhysicalPort> reappearedPorts = subject.markReappearedPortsInNMS(physicalPortMap, nbiPortIds);

    assertThat(reappearedPorts, hasSize(1));
    assertThat(reappearedPorts, hasItems(portOne));
  }

  @Test
  public void shouldFindNoReappearedPortsSinceItWasNotMissing() {
    Set<String> nbiPortIds = Sets.newHashSet("1");

    // Mark missing
    PhysicalPort portOne = physicalPortMap.get("1");
    portOne.setMissing(false);

    List<PhysicalPort> reappearedPorts = subject.markReappearedPortsInNMS(physicalPortMap, nbiPortIds);

    assertThat(reappearedPorts, hasSize(0));
  }

  @Test
  public void shouldChangeNoPortsDetectAndPersistPortInconsistencies() {

    when(nbiClientMock.findAllPhysicalPorts()).thenReturn(Lists.newArrayList(physicalPortMap.values()));
    when(physicalPortRepoMock.findAll()).thenReturn(Lists.newArrayList(physicalPortMap.values()));

    subject.detectAndPersistPortInconsistencies();

    for (PhysicalPort port : physicalPortMap.values()) {
      assertThat(port.isMissing(), is(false));
    }

    verify(physicalPortRepoMock, times(2)).save(anyListOf(PhysicalPort.class));
  }

  @Test
  public void shouldFindOneReappearingPortChangeNoPortsDetectAndPersistPortInconsistencies() {

    physicalPortMap.get("1").setMissing(true);
    when(nbiClientMock.findAllPhysicalPorts()).thenReturn(Lists.newArrayList(physicalPortMap.values()));
    when(physicalPortRepoMock.findAll()).thenReturn(Lists.newArrayList(physicalPortMap.values()));

    subject.detectAndPersistPortInconsistencies();

    for (PhysicalPort port : physicalPortMap.values()) {
      assertThat(port.isMissing(), is(false));
    }

    verify(physicalPortRepoMock, times(2)).save(anyListOf(PhysicalPort.class));
  }

  @Test
  public void shouldFindOneDisappearingPortChangeNoPortsDetectAndPersistPortInconsistencies() {

    when(nbiClientMock.findAllPhysicalPorts()).thenReturn(
        Lists.newArrayList(physicalPortMap.get("1"), physicalPortMap.get("2")));
    when(physicalPortRepoMock.findAll()).thenReturn(Lists.newArrayList(physicalPortMap.values()));

    subject.detectAndPersistPortInconsistencies();

    assertThat(physicalPortMap.get("1").isMissing(), is(false));
    assertThat(physicalPortMap.get("2").isMissing(), is(false));
    assertThat(physicalPortMap.get("3").isMissing(), is(true));

    verify(physicalPortRepoMock, times(2)).save(anyListOf(PhysicalPort.class));
  }

}
