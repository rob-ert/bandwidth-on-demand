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
import nl.surfnet.bod.service.InstitutionService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.junit.Test;

public class InstitutionControllerTest {

  private InstitutionService institutionServiceMock = mock(InstitutionService.class);
  private PhysicalResourceGroupService prgServiceMock = mock(PhysicalResourceGroupService.class);

  private InstitutionController subject = new InstitutionController(institutionServiceMock, prgServiceMock);

  @Test
  public void instutionsShouldBeFilteredBySearchParamIgnoringCase() {
    Collection<Institute> unfilteredInstitutions = newArrayList(
        new InstituteFactory().setId(1l).setName("Universiteit Utrecht").setShortName("UU").create(),
        new InstituteFactory().setId(2l).setName("Universiteit Amsterdam").setShortName("UA").create());

    when(institutionServiceMock.getInstitutions()).thenReturn(unfilteredInstitutions);

    Collection<Institute> institutionsInAmsterdam = subject.jsonList("amsterdam");

    assertThat(institutionsInAmsterdam, hasSize(1));
    assertThat(institutionsInAmsterdam.iterator().next().getName(), containsString("Amsterdam"));
  }

  @Test
  public void existingInstitutionNamesShouldBeFiltered() {
    Institute instituteAmsterdam = new InstituteFactory().setId(2L).setName("Universiteit Amsterdam")
        .setShortName("UA").create();

    Collection<Institute> unfilteredInstitutions = newArrayList(
        new InstituteFactory().setId(1L).setName("Universiteit Utrecht").setShortName("UU").create(),
        instituteAmsterdam);

    List<PhysicalResourceGroup> existingGroups = newArrayList(new PhysicalResourceGroupFactory().setInstitute(
        instituteAmsterdam).create());

    when(institutionServiceMock.getInstitutions()).thenReturn(unfilteredInstitutions);
    when(prgServiceMock.findAll()).thenReturn(existingGroups);

    Collection<Institute> institutions = subject.jsonList("");

    assertThat(institutions, hasSize(1));
    assertThat(institutions.iterator().next().getName(), is("Universiteit Utrecht"));
  }

  @Test
  public void institutionsJsonListShouldAcceptNullAsQueryParam() {
    Collection<Institute> institutions = newArrayList(new InstituteFactory().create());
    when(institutionServiceMock.getInstitutions()).thenReturn(institutions);

    Collection<Institute> result = subject.jsonList(null);

    assertThat(result, hasSize(1));
  }
}
