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
package nl.surfnet.bod.web.noc;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.web.noc.InstituteController;

@RunWith(MockitoJUnitRunner.class)
public class InstituteControllerTest {

  @Mock
  private InstituteService instituteServiceMock = mock(InstituteService.class);
  @Mock
  private PhysicalResourceGroupService prgServiceMock = mock(PhysicalResourceGroupService.class);

  @InjectMocks
  private InstituteController subject;

  @Test
  public void instutionsShouldBeFilteredBySearchParamIgnoringCase() {
    Collection<Institute> unfilteredInstitutes = newArrayList(
        new InstituteFactory().setId(1L).setName("Universiteit Utrecht").setShortName("UU").create(),
        new InstituteFactory().setId(2L).setName("Universiteit Amsterdam").setShortName("UA").create());

    when(instituteServiceMock.findAlignedWithIDD()).thenReturn(unfilteredInstitutes);

    Collection<Institute> institutesInAmsterdam = subject.jsonList("amsterdam");

    assertThat(institutesInAmsterdam, hasSize(1));
    assertThat(institutesInAmsterdam.iterator().next().getName(), containsString("Amsterdam"));
  }

  @Test
  public void existingInstituteNamesShouldBeFiltered() {
    Institute instituteAmsterdam = new InstituteFactory().setId(2L).setName("Universiteit Amsterdam")
        .setShortName("UA").create();

    Collection<Institute> unfilteredInstitutes = newArrayList(
        new InstituteFactory().setId(1L).setName("Universiteit Utrecht").setShortName("UU").create(),
        instituteAmsterdam);

    List<PhysicalResourceGroup> existingGroups = newArrayList(new PhysicalResourceGroupFactory().setInstitute(
        instituteAmsterdam).create());

    when(instituteServiceMock.findAlignedWithIDD()).thenReturn(unfilteredInstitutes);
    when(prgServiceMock.findAll()).thenReturn(existingGroups);

    Collection<Institute> institutes = subject.jsonList("");

    assertThat(institutes, hasSize(1));
    assertThat(institutes.iterator().next().getName(), is("Universiteit Utrecht"));
  }

  @Test
  public void institutesJsonListShouldAcceptNullAsQueryParam() {
    Collection<Institute> institutes = newArrayList(new InstituteFactory().create());
    when(instituteServiceMock.findAlignedWithIDD()).thenReturn(institutes);

    Collection<Institute> result = subject.jsonList(null);

    assertThat(result, hasSize(1));
  }

  @Test
  public void aInstituteWithoutANameShouldBeFine() {
    Collection<Institute> institutes = newArrayList(
        new InstituteFactory().setId(1L).setName(null).create());

    when(instituteServiceMock.findAlignedWithIDD()).thenReturn(institutes);

    Collection<Institute> institutesInAmsterdam = subject.jsonList("");

    assertThat(institutesInAmsterdam, hasSize(0));
  }
}
