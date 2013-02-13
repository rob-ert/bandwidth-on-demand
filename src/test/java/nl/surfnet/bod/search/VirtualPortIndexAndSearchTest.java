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
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.support.*;

import org.apache.lucene.queryParser.ParseException;
import org.junit.Before;
import org.junit.Test;

public class VirtualPortIndexAndSearchTest extends AbstractIndexAndSearch<VirtualPort> {

  public VirtualPortIndexAndSearchTest() {
    super(VirtualPort.class);
  }

  @Before
  public void insertTestData() {
    Institute institute = new InstituteFactory().create();
    PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().setInstitute(institute).withNoId().create();
    PhysicalPort pp = new PhysicalPortFactory().setPhysicalResourceGroup(prg).withNoId().create();
    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().setName("unit-test-vrg").withNoId().create();
    VirtualPort virtualPort = new VirtualPortFactory().setUserLabel("unit-test-label").setVirtualResourceGroup(vrg).setPhysicalPort(pp).withNodId().create();

    persist(institute, prg, pp, vrg, virtualPort);
  }

  @Test
  public void searchVirtualPortByName() throws ParseException {
    List<VirtualPort> result = searchFor("userLabel:\"unit-test-label\"");

    assertThat(result, hasSize(1));
  }

  @Test
  public void searchVirtualPortByNameOfVirtualResourceGroup() throws ParseException {
    List<VirtualPort> result = searchFor("virtualResourceGroup.name:\"unit-test-vrg\"");

    assertThat(result, hasSize(1));
  }

}