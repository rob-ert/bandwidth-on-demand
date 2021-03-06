/**
 * Copyright (c) 2012, 2013 SURFnet BV
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
package nl.surfnet.bod.search;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.TextSearchIndexer;
import nl.surfnet.bod.util.FullTextSearchContext;

import org.apache.lucene.queryParser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractIndexAndSearch<T> {

  protected Logger log = LoggerFactory.getLogger(getClass());

  private EntityManager entityManager;

  private EntityManagerFactory entityManagerFactory;

  private final Class<T> clazz;

  private final TextSearchIndexer textSearchIndexer = new TextSearchIndexer();

  @Before
  public void setUp() {
    initEntityManager();
    index();
  }

  @After
  public void tearDown() {
    closeEntityManager();
  }

  public AbstractIndexAndSearch(final Class<T> clazz) {
    this.clazz = clazz;
  }

  private void initEntityManager() {
    entityManagerFactory = Persistence.createEntityManagerFactory("search-pu");
    entityManager = entityManagerFactory.createEntityManager();
  }

  protected void index() {
    textSearchIndexer.setEntityManager(entityManager);
    textSearchIndexer.indexDatabaseContent();
  }

  @SuppressWarnings("unchecked")
  protected List<T> searchFor(String query) throws ParseException {
    return new FullTextSearchContext<T>(entityManager, clazz).getFullTextQueryForKeywordOnAllAnnotatedFields(query)
        .getResultList();
  }

  private final void closeEntityManager() {
    entityManagerFactory.close();
  }

  protected void persist(Object... entities) {
    entityManager.getTransaction().begin();
    for (Object entity : entities) {
      entityManager.persist(entity);
    }
    entityManager.getTransaction().commit();
  }

  protected void persistReservation(Reservation reservation) {
    persist(
      reservation.getSourcePort().getUniPort().get().getPhysicalResourceGroup().getInstitute(),
      reservation.getSourcePort().getUniPort().get().getPhysicalResourceGroup(),
      reservation.getSourcePort().getPhysicalPort(),
      reservation.getSourcePort().getVirtualPort().get().getVirtualResourceGroup(),
      reservation.getSourcePort().getVirtualPort().get(),
      reservation.getDestinationPort().getUniPort().get().getPhysicalResourceGroup().getInstitute(),
      reservation.getDestinationPort().getUniPort().get().getPhysicalResourceGroup(),
      reservation.getDestinationPort().getPhysicalPort(),
      reservation.getDestinationPort().getVirtualPort().get().getVirtualResourceGroup(),
      reservation.getDestinationPort().getVirtualPort().get(),
      reservation);
  }

}