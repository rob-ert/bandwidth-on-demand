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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.Institution;
import nl.surfnet.bod.idd.IddClient;
import nl.surfnet.bod.idd.generated.Klanten;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class InstitutionIddServiceTest {

  private IddClient iddClientMock = mock(IddClient.class);

  private InstitutionIddService subject = new InstitutionIddService();

  @Before
  public void setUp() {
    subject.setIddClient(iddClientMock);
  }

  @Test
  public void shouldIgnoreEmptyNames() {
    Klanten klant = newKlantWithName("");

    when(iddClientMock.getKlanten()).thenReturn(Lists.newArrayList(klant));

    Collection<Institution> institutions = subject.getInstitutions();

    assertThat(institutions, hasSize(0));
  }

  @Test
  public void shouldRemoveTrailingWhitespaceFromName() {
    Klanten klant = newKlantWithName("SURFnet\n");

    when(iddClientMock.getKlanten()).thenReturn(Lists.newArrayList(klant));

    Collection<Institution> institutions = subject.getInstitutions();

    assertThat(institutions, hasSize(1));
    assertThat(institutions.iterator().next().getName(), is("SURFnet"));
  }

  private Klanten newKlantWithName(String naam) {
    Klanten klanten = new Klanten();
    klanten.setKlantnaam(naam);
    return klanten;
  }
}
