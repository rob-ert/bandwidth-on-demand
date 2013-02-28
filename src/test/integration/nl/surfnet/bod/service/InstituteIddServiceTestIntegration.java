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
package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Collection;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nl.surfnet.bod.AppConfiguration;
import nl.surfnet.bod.config.IntegrationDbConfiguration;
import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.repo.InstituteRepo;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.util.TestHelper;

import org.hibernate.internal.SessionImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppConfiguration.class, IntegrationDbConfiguration.class })
@Transactional
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class InstituteIddServiceTestIntegration {

  @PersistenceContext
  private EntityManager em;

  @Resource
  private InstituteIddService instituteService;

  @Resource
  private InstituteRepo instituteRepo;

  @BeforeClass
  public static void testEnvironment() {
    TestHelper.useTestEnv();
  }

  @AfterClass
  public static void clearEnvironment() {
    TestHelper.clearEnv();
  }

  @Test
  public void shouldInsertInstitutes() {
    instituteService.refreshInstitutes();

    assertThat(instituteService.findAlignedWithIDD(), hasSize(greaterThan(4)));
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
    Institute instituteNotInIDD = new InstituteFactory().setId(9999L).setName("Wesaidso Software Engineering")
        .setShortName("WSE").setAlignedWithIDD(true).create();

    instituteNotInIDD = instituteRepo.save(instituteNotInIDD);

    instituteService.refreshInstitutes();

    Institute institute = instituteService.find(instituteNotInIDD.getId());

    assertThat(institute.isAlignedWithIDD(), is(false));
  }

  @Test
  public void shouldFindByShortName() {
    Institute instituteOne = new InstituteFactory().setName("Wesaidso Software Engineering")
        .setShortName("WSE").setAlignedWithIDD(true).create();

    instituteOne = instituteRepo.save(instituteOne);

    Institute institute = instituteRepo.findByShortName("WSE");
    assertThat(institute, is(instituteOne));
  }

  @Test
  public void shouldThrowSinceShortNameMustBeUnique() {
    Institute instituteOne = new InstituteFactory().setName("Wesaidso Software Engineering")
        .setShortName("WSE").setAlignedWithIDD(true).create();

    Institute instituteTwo = new InstituteFactory().setName("Wesaidso Software Engineering 2")
        .setShortName("WSE").setAlignedWithIDD(true).create();

    instituteRepo.save(instituteOne);

    instituteRepo.save(instituteTwo);
  }

}