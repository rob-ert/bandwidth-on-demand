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

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.repo.InstituteRepo;
import nl.surfnet.bod.support.InstituteFactory;

import org.hibernate.internal.SessionImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/spring/appCtx.xml", "/spring/appCtx-jpa-integration.xml", "/spring/appCtx-nbi-client.xml",
    "/spring/appCtx-idd-client.xml" })
@Transactional
public class InstituteIddServiceTestIntegration {

  @PersistenceContext
  private EntityManager em;

  @Autowired
  private InstituteIddService instituteService;

  @Autowired
  private InstituteRepo instituteRepo;

  @Test
  public void shouldInsertInstitutes() {
    instituteService.refreshInstitutes();

    Assert.assertTrue(instituteService.findAlignedWithIDD().size() > 200);
  }

  @Test
  public void shouldUpdateInstitutesThisIsWhyTheVersionAttributeIsRemovedInInstitute() {
    instituteService.refreshInstitutes();
    Collection<Institute> institutes = instituteService.findAlignedWithIDD();

    for (Institute institute : instituteService.findAlignedWithIDD()) {
      ((SessionImpl) em.getDelegate()).evict(institute);
    }

    instituteService.refreshInstitutes();
    Collection<Institute> foundInstitutes = instituteService.findAlignedWithIDD();

    // Size should be the same, not doubled or so due to additional inserts
    assertThat(foundInstitutes.size(), is(institutes.size()));
  }

  @Test
  public void shouldMarkInstituteAsNotAlignedWithIDD() {
    final Long BIG_ID = 999999999L;

    // Pretend it is alignedWithIDD
    Institute instituteNotInIDD = new InstituteFactory().setId(BIG_ID).setName("Wesaidso Software Engineering")
        .setShortName("WSE").setAlignedWithIDD(true).create();
    instituteRepo.save(instituteNotInIDD);

    instituteService.refreshInstitutes();

    Institute institute = instituteService.find(BIG_ID);
    assertThat(institute.isAlignedWithIDD(), is(false));
  }
}
