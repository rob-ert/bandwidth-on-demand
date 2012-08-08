package nl.surfnet.bod.service;

import java.util.List;

import org.springframework.data.domain.Sort;

/**
 * Service interface to abstract full text search functionality
 * 
 * @param <T>
 *          DomainObject
 */
public interface FullTextSearchableService<T> {
  long countSearchFor(String searchText);

  List<T> searchFor(String searchText, int firstResult, int maxResults, Sort sort);
}