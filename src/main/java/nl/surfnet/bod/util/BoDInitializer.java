/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
