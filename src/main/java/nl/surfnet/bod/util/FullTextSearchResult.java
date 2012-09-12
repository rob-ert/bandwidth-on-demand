package nl.surfnet.bod.util;

import java.util.List;

public class FullTextSearchResult<T> {

  private final long count;
  private final List<T> resultList;

  public FullTextSearchResult(final long count, final List<T> resultList) {
    this.count = count;
    this.resultList = resultList;
  }

  public long getCount() {
    return count;
  }

  public List<T> getResultList() {
    return resultList;
  }

}
