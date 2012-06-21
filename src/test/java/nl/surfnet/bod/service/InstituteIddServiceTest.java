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

import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.idd.IddClient;
import nl.surfnet.bod.idd.generated.Klanten;
import nl.surfnet.bod.repo.InstituteRepo;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.KlantenFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class InstituteIddServiceTest {

  @InjectMocks
  private InstituteIddService subject;

  @Mock
  private IddClient iddClientMock;

  @Mock
  private InstituteRepo instituteRepoMock;

  @Test
  public void refreshingInstitutes() {
    Institute institute1 = new InstituteFactory().create();
    Institute institute2 = new InstituteFactory().create();
    Klanten klant1 = new KlantenFactory().create();
    Klanten klant2 = new KlantenFactory().create();

    when(instituteRepoMock.findAll()).thenReturn(ImmutableList.of(institute1, institute2));
    when(iddClientMock.getKlanten()).thenReturn(ImmutableList.of(klant1, klant2));

    subject.refreshInstitutes();
  }


}
