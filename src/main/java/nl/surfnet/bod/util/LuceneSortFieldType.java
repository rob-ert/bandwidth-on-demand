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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.search.SortField;

/**
 * Maps java types to LuceneTypes as defined in {@link SortField}
 *
 */
public enum LuceneSortFieldType {

  STRING(SortField.STRING, String.class), //
  INTEGER(SortField.INT, Integer.class), INTEGER_PRIMITIVE(SortField.INT, int.class), //
  FLOAT(SortField.FLOAT, Float.class), FLOAT_PRIMITIVE(SortField.FLOAT, float.class), //
  LONG(SortField.LONG, Long.class), LONG_PRIMITIVE(SortField.LONG, long.class), //
  DOUBLE(SortField.DOUBLE, Double.class), DOUBLE_PRIMITIVE(SortField.DOUBLE, double.class), //
  SHORT(SortField.SHORT, Short.class), SHORT_PRIMITIVE(SortField.SHORT, short.class), //
  BYTE(SortField.BYTE, Byte.class), BYTE_PRIMITIVE(SortField.BYTE, byte.class);

  private static final Map<Class<?>, Integer> LOOKUP = new HashMap<>();

  static {
    for (LuceneSortFieldType luceneType : EnumSet.allOf(LuceneSortFieldType.class)) {
      LOOKUP.put(luceneType.getJavaType(), luceneType.getLuceneType());
    }
  }

  private Integer luceneType;
  private Class<?> javaType;

  LuceneSortFieldType(int luceneType, Class<?> javaType) {
    this.luceneType = luceneType;
    this.javaType = javaType;
  }

  public static int getLuceneTypeFor(Class<?> javaType) {
    Integer luceneTypeCode = LOOKUP.get(javaType);
    return luceneTypeCode == null ? SortField.STRING_VAL : luceneTypeCode;
  }

  public Integer getLuceneType() {
    return luceneType;
  }

  public Class<?> getJavaType() {
    return javaType;
  }

}
