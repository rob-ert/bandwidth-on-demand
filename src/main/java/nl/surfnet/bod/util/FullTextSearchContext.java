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

import static nl.surfnet.bod.web.WebUtils.not;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.Version;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.springframework.data.domain.Sort.Order;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

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
    if (not(StringUtils.containsAny(keyword, new char[] { '*', '?', ':' }))) {
      keyword = "*" + keyword + "*";
    }

    Query luceneQuery = parser.parse(keyword);

    return getFullTextQuery(luceneQuery, convertToLuceneSort(springSort, indexedFields));
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
  Sort convertToLuceneSort(org.springframework.data.domain.Sort springSort, String[] indexedFields) {
    List<SortField> sortFields = Lists.newArrayList();

    if (springSort != null) {
      Iterator<Order> orderIt = springSort.iterator();
      while (orderIt.hasNext()) {
        Order order = orderIt.next();
        if (order != null) {
          String propertyName = order.getProperty();
          // Is it an indexed field?
          if (Arrays.asList(indexedFields).contains(propertyName)) {
            int luceneType = LuceneSortFieldType.getLuceneTypeFor(ReflectionUtils.findField(entity, propertyName)
                .getType());

            SortField sortField = new SortField(propertyName, luceneType, !(order.isAscending()));
            sortFields.add(sortField);
          }
        }
      }
    }

    if (CollectionUtils.isEmpty(sortFields)) {
      return null;
    }
    else {
      return new Sort(sortFields.toArray(new SortField[(sortFields.size())]));
    }

  }

  @VisibleForTesting
  Analyzer getAnalyzer(String name) {
    return fullTextEntityManager.getSearchFactory().getAnalyzer(name);
  }

  private FullTextQuery getFullTextQuery(org.apache.lucene.search.Query luceneQuery, Sort sort) {
    FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, entity);

    if (sort != null) {
      fullTextQuery.setSort(sort);
    }

    return fullTextQuery;
  }

}