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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.opendrac.NbiService;
import nl.surfnet.bod.repo.PhysicalPortRepo;
import nl.surfnet.bod.support.PhysicalPortFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalPortServiceImplTest {

  private PhysicalPortServiceImpl subject = new PhysicalPortServiceImpl();

  @Mock
  private NbiService nbiServiceMock;
  @Mock
  private PhysicalPortRepo physicalPortRepoMock;

  @Before
  public void init() {
    subject.setNbiPortService(nbiServiceMock);
    subject.setPhysicalPortRepo(physicalPortRepoMock);
  }

  @Test
  public void findAllShouldMergePorts() {
    List<PhysicalPort> nbiPorts = Lists.newArrayList(
        new PhysicalPortFactory().setName("first").setId(1L).create(),
        new PhysicalPortFactory().setName("second").setId(1L).create());
    
    List<PhysicalPort> repoPorts = Lists.newArrayList(
        new PhysicalPortFactory().setName("first").setId(1L).setVersion(2).create());

    when(nbiServiceMock.findAll()).thenReturn(nbiPorts);
    when(physicalPortRepoMock.findAll()).thenReturn(repoPorts);

    List<PhysicalPort> allPorts = subject.findAll();

    assertThat(allPorts, hasSize(2));
    assertThat(allPorts.get(0).getName(), is("first"));
    assertThat(allPorts.get(0).getId(), is(1L));
    assertThat(allPorts.get(0).getVersion(), is(2));
    assertThat(allPorts.get(1).getName(), is("second"));
  }

  @Test
  public void allUnallocatedPortsShouldNotContainOnesWithId() {
    List<PhysicalPort> nbiPorts = Lists.newArrayList(
        new PhysicalPortFactory().setName("first").setId(null).create(),
        new PhysicalPortFactory().setId(null).setName("second").create());

    List<PhysicalPort> repoPorts = Lists.newArrayList(
        new PhysicalPortFactory().setName("first").setId(1L).create());

    when(nbiServiceMock.findAll()).thenReturn(nbiPorts);
    when(physicalPortRepoMock.findAll()).thenReturn(repoPorts);

    Collection<PhysicalPort> unallocatedPorts = subject.findUnallocated();

    assertThat(unallocatedPorts, hasSize(1));
    assertThat(unallocatedPorts.iterator().next().getName(), is("second"));
  }

  @Test(expected = IllegalStateException.class)
  public void findAllPortsWithSameNameShouldGiveAnException() {
    List<PhysicalPort> nbiPorts = Lists.newArrayList(
        new PhysicalPortFactory().setName("first").create());
    List<PhysicalPort> repoPorts = Lists.newArrayList(
        new PhysicalPortFactory().setName("first").create(),
        new PhysicalPortFactory().setName("first").create());

    when(nbiServiceMock.findAll()).thenReturn(nbiPorts);
    when(physicalPortRepoMock.findAll()).thenReturn(repoPorts);

    subject.findAll();
  }

  @Test
  public void findByNameShouldGiveNullIfNotFound() {
    when(nbiServiceMock.findByName("first")).thenReturn(null);
    when(physicalPortRepoMock.findByName("first")).thenReturn(null);

    PhysicalPort port = subject.findByName("first");

    assertThat(port, nullValue());
  }

  @Test
  public void findByNameShouldGiveAMergedPortIfFound() {
    PhysicalPort nbiPort = new PhysicalPortFactory().setName("first").create();
    PhysicalPort repoPort = new PhysicalPortFactory().setId(1L).setName("first").create();

    when(nbiServiceMock.findByName("first")).thenReturn(nbiPort);
    when(physicalPortRepoMock.findByName("first")).thenReturn(repoPort);

    PhysicalPort port = subject.findByName("first");

    assertThat(port.getName(), is("first"));
    assertThat(port.getId(), is(1L));
  }

  @Test
  public void findEntriesShouldReturnMaxTwoPorts() {
    PhysicalPort firstPort = new PhysicalPortFactory().setName("first").create();
    List<PhysicalPort> nbiPorts = Lists.newArrayList(
        firstPort,
        new PhysicalPortFactory().setName("second").create(),
        new PhysicalPortFactory().setName("third").create(),
        new PhysicalPortFactory().setName("fourth").create()
        );

    when(nbiServiceMock.findAll()).thenReturn(nbiPorts);
    when(physicalPortRepoMock.findAll()).thenReturn(Collections.<PhysicalPort>emptyList());

    List<PhysicalPort> entries = subject.findEntries(0, 2);

    assertThat(entries, hasSize(2));
    assertThat(entries.get(0), is(firstPort));
  }

  @Test
  public void findEntriesShouldReturnSecondPortAsFirstResult() {
    PhysicalPort secondPort = new PhysicalPortFactory().setName("second").create();
    List<PhysicalPort> nbiPorts = Lists.newArrayList(
        new PhysicalPortFactory().setName("first").create(),
        secondPort,
        new PhysicalPortFactory().setName("third").create(),
        new PhysicalPortFactory().setName("fourth").create()
        );

    when(nbiServiceMock.findAll()).thenReturn(nbiPorts);
    when(physicalPortRepoMock.findAll()).thenReturn(Collections.<PhysicalPort>emptyList());

    List<PhysicalPort> entries = subject.findEntries(1, 2);

    assertThat(entries, hasSize(2));
    assertThat(entries.get(0), is(secondPort));
  }

  @Test
  public void findEntriesShouldReturnMaxAvailablePorts() {
    List<PhysicalPort> nbiPorts = Lists.newArrayList(
        new PhysicalPortFactory().setName("first").create(),
        new PhysicalPortFactory().setName("second").create()
        );

    when(nbiServiceMock.findAll()).thenReturn(nbiPorts);
    when(physicalPortRepoMock.findAll()).thenReturn(Collections.<PhysicalPort>emptyList());

    List<PhysicalPort> entries = subject.findEntries(0, 20);

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
  public void countShouldCallCountOnNbi() {
    subject.count();

    verify(nbiServiceMock, only()).count();
  }

  @Test
  public void countUnallocatedShouldCalculateDifferenceBetweenNbiAndRepoPorts() {
    when(nbiServiceMock.count()).thenReturn(15L);
    when(physicalPortRepoMock.count()).thenReturn(10L);

    long countUnallocated = subject.countUnallocated();

    assertThat(countUnallocated, is(5L));
  }

}
