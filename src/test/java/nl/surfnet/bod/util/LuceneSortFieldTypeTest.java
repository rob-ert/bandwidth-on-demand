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

import nl.surfnet.bod.domain.PhysicalPort;

import org.apache.lucene.search.SortField;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class LuceneSortFieldTypeTest {

  @Test
  public void testLuceneTypeInt() {
    assertThat(LuceneSortFieldType.getLuceneTypeFor(int.class), is(SortField.INT));
  }

  @Test
  public void testLucenTypeInteger() {
    assertThat(LuceneSortFieldType.getLuceneTypeFor(Integer.class), is(SortField.INT));
  }

  @Test
  public void testLucenTypeNonExisting() {
    assertThat(LuceneSortFieldType.getLuceneTypeFor(PhysicalPort.class), is(SortField.STRING_VAL));
  }

  @Test
  public void testLucenTypeNull() {
    assertThat(LuceneSortFieldType.getLuceneTypeFor(null), is(SortField.STRING_VAL));
  }

}
