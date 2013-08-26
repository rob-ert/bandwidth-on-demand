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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.NmsAlignmentStatus;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.support.NbiPortFactory;
import nl.surfnet.bod.support.PhysicalPortFactory;

import org.junit.Before;
import org.junit.Test;



public class PortAlignmentCheckerTest {

  private Map<String, PhysicalPort> physicalPortMap;
  private Map<String, NbiPort> nbiPortMap;
  private PortAlignmentChecker subject;

  @Before
  public void setUp() {
    subject = new PortAlignmentChecker();

    List<PhysicalPort> physicalPorts = Lists.<PhysicalPort> newArrayList(
        new PhysicalPortFactory().setId(1L).setNbiPort(new NbiPortFactory().setNmsPortId("1").create()).create(),
        new PhysicalPortFactory().setId(2L).setNbiPort(new NbiPortFactory().setNmsPortId("2").create()).create(),
        new PhysicalPortFactory().setId(3L).setNbiPort(new NbiPortFactory().setNmsPortId("3").create()).create());

    physicalPortMap = PhysicalPortService.buildPhysicalPortIdMap(physicalPorts);
    nbiPortMap = ImmutableMap.of("1", physicalPorts.get(0).getNbiPort(), "2", physicalPorts.get(1).getNbiPort(), "3", physicalPorts.get(2).getNbiPort());
  }

  @Test
  public void shouldFindTwoDissapearedPorts() {
    subject.updateAlignment(allocatedPorts("1", "2", "3"), nbiPorts("3"));

    assertThat(subject.getUnalignedPorts(), hasSize(2));
    assertThat(subject.getUnalignedPorts(), hasItems(physicalPortMap.get("1"), physicalPortMap.get("2")));
  }

  @Test
  public void shouldFindPortTypeChanges() {
    subject.updateAlignment(
      Arrays.<PhysicalPort> asList(
        new PhysicalPortFactory().setNbiPort(new NbiPortFactory().setNmsPortId("1").setVlanRequired(false).create()).create(),
        new PhysicalPortFactory().setNbiPort(new NbiPortFactory().setNmsPortId("2").setVlanRequired(true).create()).create()),
      Arrays.asList(
        new NbiPortFactory().setNmsPortId("1").setVlanRequired(true).create(),
        new NbiPortFactory().setNmsPortId("2").setVlanRequired(false).create()));

    assertThat(subject.getUnalignedPorts(), hasSize(2));
    assertThat(subject.getUnalignedPorts().get(0).getNmsAlignmentStatus(), is(NmsAlignmentStatus.TYPE_CHANGED_TO_VLAN));
    assertThat(subject.getUnalignedPorts().get(1).getNmsAlignmentStatus(), is(NmsAlignmentStatus.TYPE_CHANGED_TO_LAN));
  }

  @Test
  public void shouldOnlyFindNonMissingDisappearedPorts() {
    physicalPortMap.get("2").setNmsAlignmentStatus(NmsAlignmentStatus.DISAPPEARED);
    subject.updateAlignment(allocatedPorts("1", "2", "3"), nbiPorts("3"));

    assertThat(subject.getUnalignedPorts(), hasSize(1));
    assertThat(subject.getUnalignedPorts(), hasItems(physicalPortMap.get("1")));
  }

  @Test
  public void shouldFindNoDissapearedPorts() {
    subject.updateAlignment(allocatedPorts("1", "2", "3"), nbiPorts("1", "2", "3"));

    assertThat(subject.getUnalignedPorts(), hasSize(0));
  }

  @Test
  public void shouldFindOneDissapearedPorts() {
    subject.updateAlignment(allocatedPorts("1", "2", "3"), nbiPorts("1", "3"));

    assertThat(subject.getUnalignedPorts(), hasSize(1));
    assertThat(subject.getUnalignedPorts(), hasItems(physicalPortMap.get("2")));
  }

  @Test
  public void shouldFindNoDissapearedPortsWhenWithNewPort() {
    subject.updateAlignment(allocatedPorts("1", "2", "3"), Arrays.asList(new NbiPortFactory().setNmsPortId("4").create()));

    assertThat(subject.getUnalignedPorts(), hasSize(3));
    assertThat(subject.getUnalignedPorts(), hasItems(physicalPortMap.get("1"), physicalPortMap.get("2"), physicalPortMap.get("3")));
  }

  @Test
  public void shouldFindNoReappearedPorts() {
    physicalPortMap.get("1").setNmsAlignmentStatus(NmsAlignmentStatus.ALIGNED);

    subject.updateAlignment(allocatedPorts("1", "2", "3"), nbiPorts());

    assertThat(subject.getRealignedPorts(), hasSize(0));
  }

  @Test
  public void shouldFindOneReappearedPorts() {
    // Mark missing
    PhysicalPort portOne = physicalPortMap.get("1");
    portOne.setNmsAlignmentStatus(NmsAlignmentStatus.DISAPPEARED);

    subject.updateAlignment(allocatedPorts("1", "2", "3"), nbiPorts("1", "2"));

    assertThat(subject.getRealignedPorts(), hasSize(1));
    assertThat(subject.getRealignedPorts(), hasItems(portOne));
    assertThat(subject.getRealignedPorts().get(0).getNmsAlignmentStatus(), is(NmsAlignmentStatus.ALIGNED));
  }

  @Test
  public void shouldFindOneRealignedPortAfterTypeChange() {
    PhysicalPort portOne = physicalPortMap.get("1");
    portOne.setNmsAlignmentStatus(NmsAlignmentStatus.TYPE_CHANGED_TO_VLAN);
    PhysicalPort portTwo = physicalPortMap.get("2");
    portTwo.setNmsAlignmentStatus(NmsAlignmentStatus.DISAPPEARED);

    subject.updateAlignment(
        Arrays.asList(portOne, portTwo),
        Arrays.asList(
            new NbiPortFactory().setNmsPortId("1").setVlanRequired(false).create(),
            new NbiPortFactory().setNmsPortId("2").setVlanRequired(true).create()));

    assertThat(subject.getRealignedPorts(), hasSize(1));
    assertThat(subject.getRealignedPorts(), hasItems(portOne));
    assertThat(portOne.getNmsAlignmentStatus(), is(NmsAlignmentStatus.ALIGNED));

    assertThat(portTwo.getNmsAlignmentStatus(), is(NmsAlignmentStatus.TYPE_CHANGED_TO_VLAN));
  }

  @Test
  public void shouldFindNoReappearedPortsSinceItWasNotMissing() {
    // Mark missing
    PhysicalPort portOne = physicalPortMap.get("1");
    portOne.setNmsAlignmentStatus(NmsAlignmentStatus.ALIGNED);

    subject.updateAlignment(allocatedPorts("1", "2", "3"), nbiPorts("1"));

    assertThat(subject.getRealignedPorts(), hasSize(0));
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
