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

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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

    when(instituteServiceMock.findAll()).thenReturn(unfilteredInstitutes);

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

    when(instituteServiceMock.findAll()).thenReturn(unfilteredInstitutes);
    when(prgServiceMock.findAll()).thenReturn(existingGroups);

    Collection<Institute> institutes = subject.jsonList("");

    assertThat(institutes, hasSize(1));
    assertThat(institutes.iterator().next().getName(), is("Universiteit Utrecht"));
  }

  @Test
  public void institutesJsonListShouldAcceptNullAsQueryParam() {
    Collection<Institute> institutes = newArrayList(new InstituteFactory().create());
    when(instituteServiceMock.findAll()).thenReturn(institutes);

    Collection<Institute> result = subject.jsonList(null);

    assertThat(result, hasSize(1));
  }

  @Test
  public void aInstituteWithoutANameShouldBeFine() {
    Collection<Institute> institutes = newArrayList(
        new InstituteFactory().setId(1L).setName(null).create());

    when(instituteServiceMock.findAll()).thenReturn(institutes);

    Collection<Institute> institutesInAmsterdam = subject.jsonList("");

    assertThat(institutesInAmsterdam, hasSize(0));
  }
}
