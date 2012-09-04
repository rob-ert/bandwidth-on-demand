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
package nl.surfnet.bod.util;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.search.MassIndexer;
import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;
import org.hibernate.search.impl.SimpleIndexingProgressMonitor;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

/**
 * Initializes the BoD application, will be executed by spring after loading and
 * decrypting of properties
 */
public class BoDInitializer {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @PersistenceContext
  private EntityManager entityManager;

  public void init() {
    logger.info("Initializing BoD");
    indexDatabaseContent();
    // IBM humor...
    logger.info("Ready for eBusiness");
  }

  public void indexDatabaseContent() {
    final FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
    final MassIndexerProgressMonitor indexMonitor = new SimpleIndexingProgressMonitor(10);
    try {
      final MassIndexer indexer = fullTextEntityManager.createIndexer();
      //Set threads to one, to workaround bug of mass indexer
      //see (https://hibernate.onjira.com/browse/HSEARCH-598)
      indexer.threadsForSubsequentFetching(1);
      indexer.threadsToLoadObjects(1);
      indexer.progressMonitor(indexMonitor);
      indexer.startAndWait();
    }
    catch (InterruptedException e) {
      logger.error("Error indexing current database content", e);
    }

    logger.info("Succesfully indexed database content for full text search");
  }

  @VisibleForTesting
  public final void setEntityManager(final EntityManager entityManager) {
    this.entityManager = entityManager;
  }

}
