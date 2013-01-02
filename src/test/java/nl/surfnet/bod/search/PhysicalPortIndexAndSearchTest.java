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
package nl.surfnet.bod.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;

import org.apache.lucene.queryParser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PhysicalPortIndexAndSearchTest extends AbstractIndexAndSearch<PhysicalPort> {

  public PhysicalPortIndexAndSearchTest() {
    super(PhysicalPort.class);
  }

  @Before
  public void setUp() {
    initEntityManager();
  }

  @After
  public void tearDown() {
    closeEntityManager();
  }

  @Test
  public void testIndexAndSearch() throws Exception {
    List<PhysicalPort> physicalPorts = getSearchQuery("ut");
    assertThat("nothing indexed so nothing should be found", physicalPorts, hasSize(0));

    index();

    physicalPorts = getSearchQuery("gamma");
    // (N.A.)
    assertThat(physicalPorts, hasSize(0));

    physicalPorts = getSearchQuery("ut");
    // (UT One, UT Two)
    assertThat(physicalPorts, hasSize(2));

    physicalPorts = getSearchQuery("Ut");
    // (UT One, UT Two)
    assertThat(physicalPorts, hasSize(2));

    physicalPorts = getSearchQuery("Mock");
    // (All available (4) PP's)
    assertThat(physicalPorts, hasSize(4));

    physicalPorts = getSearchQuery("ETH-1-13-4");
    // (Noc label 4)
    assertThat(physicalPorts, hasSize(1));
    assertThat(physicalPorts.get(0).getNocLabel(), equalTo("Noc 4 label"));

    physicalPorts = getSearchQuery("OME");
    // (Mock_Ut002A_OME01_ETH-1-2-4, Mock_Ut001A_OME01_ETH-1-2-1)
    assertThat(physicalPorts, hasSize(2));
    assertThat(physicalPorts.get(0).getNocLabel(), equalTo("Mock_Ut002A_OME01_ETH-1-2-1"));
    assertThat(physicalPorts.get(1).getNocLabel(), equalTo("Mock_Ut001A_OME01_ETH-1-2-2"));

    physicalPorts = getSearchQuery("ETH-1-");
    // (All available (4) PP's)
    assertThat(physicalPorts, hasSize(4));
    assertThat(physicalPorts.get(0).getNocLabel(), equalTo("Mock_Ut002A_OME01_ETH-1-2-1"));
    assertThat(physicalPorts.get(1).getNocLabel(), equalTo("Mock_Ut001A_OME01_ETH-1-2-2"));
    assertThat(physicalPorts.get(2).getNocLabel(), equalTo("Noc 3 label"));
    assertThat(physicalPorts.get(3).getNocLabel(), equalTo("Noc 4 label"));

    physicalPorts = getSearchQuery("1");
    // (All available (4) PP's)
    assertThat(physicalPorts, hasSize(4));
    assertThat(physicalPorts.get(0).getNocLabel(), equalTo("Mock_Ut002A_OME01_ETH-1-2-1"));
    assertThat(physicalPorts.get(1).getNocLabel(), equalTo("Mock_Ut001A_OME01_ETH-1-2-2"));
    assertThat(physicalPorts.get(2).getNocLabel(), equalTo("Noc 3 label"));
    assertThat(physicalPorts.get(3).getNocLabel(), equalTo("Noc 4 label"));

    physicalPorts = getSearchQuery("1de");
    // Mock_port 1de verdieping toren1a
    assertThat(physicalPorts, hasSize(1));
    assertThat(physicalPorts.get(0).getBodPortId(), equalTo("Mock_port 1de verdieping toren1a"));

    physicalPorts = getSearchQuery("2de");
    // Mock_port 2de verdieping toren1b
    assertThat(physicalPorts, hasSize(1));
    assertThat(physicalPorts.get(0).getBodPortId(), equalTo("Mock_port 2de verdieping toren1b"));
  }

  @Test
  public void shouldNotCrashOnColon() throws ParseException {
    index();
    List<PhysicalPort> physicalPorts = getSearchQuery("nocLabel:\"Noc 3 label\"");

    assertThat(physicalPorts, hasSize(1));
    assertThat(physicalPorts.get(0).getNocLabel(), equalTo("Noc 3 label"));
  }

}