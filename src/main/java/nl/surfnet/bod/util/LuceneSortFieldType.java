/**
 * Copyright (c) 2012, SURFnet BV
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
