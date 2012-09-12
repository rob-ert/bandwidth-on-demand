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