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
package nl.surfnet.bod.web;

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
