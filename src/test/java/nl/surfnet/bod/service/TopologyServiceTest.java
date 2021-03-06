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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.EnniPort;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.util.Environment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ogf.schemas.nml._2013._05.base.TopologyType;

@RunWith(MockitoJUnitRunner.class)
public class TopologyServiceTest {

  @InjectMocks private NsiInfraDocumentsService subject;

  @Mock private VirtualPortService virtualPortServiceMock;
  @Mock private PhysicalPortService physicalPortServiceMock;
  @Mock private NsiHelper nsiHelperMock;
  @Mock private Environment bodEnvironmentMock;

  @Before
  public void setup() {
    subject.setAdminContact("John Joe");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void topology_should_equals_even_when_order_of_ports_changes() {
    VirtualPort vpOne = new VirtualPortFactory().create();
    VirtualPort vpTwo = new VirtualPortFactory().create();
    EnniPort enniOne = new PhysicalPortFactory().createEnni();
    EnniPort enniTwo = new PhysicalPortFactory().createEnni();

    when(virtualPortServiceMock.findAll()).thenReturn(Lists.newArrayList(vpOne, vpTwo), Lists.newArrayList(vpTwo, vpOne));
    when(physicalPortServiceMock.findAllAllocatedEnniEntries()).thenReturn(Lists.newArrayList(enniOne, enniTwo), Lists.newArrayList(enniTwo, enniOne));

    TopologyType firstTopology = subject.topology();

    for (int i = 0; i < 10; i++) {
       TopologyType topology = subject.topology();
       assertThat(topology, is(firstTopology));
    }

  }
}
