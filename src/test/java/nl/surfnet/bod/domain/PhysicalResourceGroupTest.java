/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.junit.Test;

public class PhysicalResourceGroupTest {

  @Test
  public void physicalResourceGroupgetNameShouldContainInstituteName() {
    Institute institute = new InstituteFactory().setName("InstituteOne").create();

    PhysicalResourceGroup subject = new PhysicalResourceGroupFactory().setInstitute(institute).create();

    assertThat(subject.getName(), is("InstituteOne"));
  }

  @Test
  public void physicalResourceGroupShouldCountItsPorts() {
    PhysicalResourceGroup subject = new PhysicalResourceGroupFactory().addPhysicalPort(
        new PhysicalPortFactory().create(), new PhysicalPortFactory().create()).create();

    assertThat(subject.getPhysicalPortCount(), is(2));
  }

  @Test
  public void shouldCalculateAdminGroups() {
    PhysicalResourceGroup subject = new PhysicalResourceGroupFactory().setAdminGroup("urn:surfguest:ict-managers").create();

    assertThat(subject.getAdminGroups(), hasSize(1));
    assertThat(subject.getAdminGroups(), contains("urn:surfguest:ict-managers"));
  }
}
