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
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.idd.IddClient;
import nl.surfnet.bod.idd.generated.Klanten;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class InstituteIddServiceTest {

  @InjectMocks
  private InstituteIddService subject;

  @Mock
  private IddClient iddClientMock;

  private PhysicalPort portOne = new PhysicalPortFactory().setNocLabel("onePort").create();

  private PhysicalPort portTwo = new PhysicalPortFactory().setNocLabel("twoPort").create();

  private List<PhysicalPort> ports = ImmutableList.of(portOne, portTwo);

  private PhysicalResourceGroup groupOne = new PhysicalResourceGroupFactory().create();

  private Klanten klantOne;

  private Klanten klantTwo;

  @Before
  public void setUp() {
    klantOne = newKlantWithName("klantOne");
    klantOne.setKlant_id(1);

    klantTwo = newKlantWithName("klantTwo");
    klantTwo.setKlant_id(2);
  }

  @Test
  public void shouldIgnoreEmptyNames() {
    Klanten klant = newKlantWithName("");

    when(iddClientMock.getKlanten()).thenReturn(Lists.newArrayList(klant));

    Collection<Institute> institutes = subject.getInstitutes();

    assertThat(institutes, hasSize(0));
  }

  @Test
  public void shouldRemoveTrailingWhitespaceFromName() {
    Klanten klant = newKlantWithName("SURFnet\n");

    when(iddClientMock.getKlanten()).thenReturn(Lists.newArrayList(klant));

    Collection<Institute> institutes = subject.getInstitutes();

    assertThat(institutes, hasSize(1));
    assertThat(institutes.iterator().next().getName(), is("SURFnet"));
  }

  @Test
  public void shouldFillInstituteForPhysicalPorts() {

    when(iddClientMock.getKlantById(1L)).thenReturn(klantOne);
    when(iddClientMock.getKlantById(2L)).thenReturn(klantTwo);

    portOne.getPhysicalResourceGroup().setInstitute(null);
    portOne.getPhysicalResourceGroup().setInstituteId(1L);

    portTwo.getPhysicalResourceGroup().setInstitute(null);
    portTwo.getPhysicalResourceGroup().setInstituteId(2L);

    subject.fillInstituteForPhysicalPorts(ports);

    assertThat(portOne.getPhysicalResourceGroup().getInstituteId(), is(portOne.getPhysicalResourceGroup()
        .getInstitute().getId()));
    assertThat(portTwo.getPhysicalResourceGroup().getInstituteId(), is(portTwo.getPhysicalResourceGroup()
        .getInstitute().getId()));
  }

  @Test
  public void shouldFillInstituteForPhysicalResourceGroup() {
    when(iddClientMock.getKlantById(1L)).thenReturn(klantOne);

    groupOne.setInstitute(null);
    groupOne.setInstituteId(1L);

    subject.fillInstituteForPhysicalResourceGroup(groupOne);

    assertThat(groupOne.getInstituteId(), is(groupOne.getInstitute().getId()));
  }

  private Klanten newKlantWithName(String naam) {
    Klanten klanten = new Klanten();
    klanten.setKlantnaam(naam);
    return klanten;
  }
}
