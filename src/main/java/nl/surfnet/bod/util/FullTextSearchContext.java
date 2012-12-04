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

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import static nl.surfnet.bod.web.WebUtils.not;

/**
 * Class which holds state related to creating and executing a full text search
 * 
 */
public class FullTextSearchContext<T> {

  private final Class<T> entity;
  private final FullTextEntityManager fullTextEntityManager;
  private final Analyzer analyzer;

  public FullTextSearchContext(EntityManager em, Class<T> entityClass) {
    this.entity = entityClass;
    this.fullTextEntityManager = getFullTextEntityManager(em);
    this.analyzer = getAnalyzer("customanalyzer");
  }

  public FullTextQuery getFullTextQueryForKeywordOnAllAnnotedFields(String keyword,
      org.springframework.data.domain.Sort springSort) throws ParseException {

    String[] indexedFields = findAllIndexedFields(entity);

    final QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_35, indexedFields, analyzer);
    parser.setAllowLeadingWildcard(true);

    // Add wildcards when not already in search
    if (not(StringUtils.containsAny(keyword, new char[] { '*', '?', ':' }) || keyword.startsWith("\"")
        && keyword.endsWith("\""))) {
      keyword = "*" + keyword + "*";
    }

    Query luceneQuery = parser.parse(keyword);
    return getFullTextQuery(luceneQuery);
  }

  /**
   * Default implementation which returns all fields and related fields of the
   * given entity. Can be overridden to limit the fields to search for.
   * 
   * @param entity
   *          Entity to inspect
   * @return String[] with (nested)fieldnames
   */
  public String[] findAllIndexedFields(Class<?> entity) {
    List<String> indexedFields = Lists.newArrayList();

    indexedFields.addAll(getIndexedFields(entity, Optional.<String> absent()));

    return indexedFields.toArray(new String[indexedFields.size()]);
  }

  /**
   * 
   * @param entity
   *          entity to inspect
   * @param prefix
   *          optional prefix for fieldNames (@see
   *          {@link #getIndexedEmbeddedFields(Class)}
   * @param declaredFields
   * @return String array containing all fieldNames of the given entity which
   *         are annotated with {@link Field} to mark them indexable.
   */
  private List<String> getIndexedFields(Class<?> entity, Optional<String> prefix) {

    java.lang.reflect.Field[] declaredFields = entity.getDeclaredFields();
    List<String> fieldNames = Lists.newArrayList();
    for (java.lang.reflect.Field field : declaredFields) {
      if (field.getAnnotation(Field.class) != null) {
        fieldNames.add(prefix.isPresent() ? prefix.get() + "." + field.getName() : field.getName());
      }
      else if (field.getAnnotation(IndexedEmbedded.class) != null) {
        fieldNames.addAll(getIndexedFields(field.getType(),
            Optional.of((prefix.isPresent() ? prefix.get() + "." : "") + field.getName())));
      }
    }

    return fieldNames;
  }

  /**
   * Just to enable mocking
   */
  @VisibleForTesting
  FullTextEntityManager getFullTextEntityManager(EntityManager em) {
    return Search.getFullTextEntityManager(em);
  }

  @VisibleForTesting
  Analyzer getAnalyzer(String name) {
    return fullTextEntityManager.getSearchFactory().getAnalyzer(name);
  }

  private FullTextQuery getFullTextQuery(org.apache.lucene.search.Query luceneQuery) {
    return fullTextEntityManager.createFullTextQuery(luceneQuery, entity);

  }

}