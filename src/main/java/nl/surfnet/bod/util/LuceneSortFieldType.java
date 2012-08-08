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

  private static final Map<Class<?>, Integer> lookup = new HashMap<>();

  static {
    for (LuceneSortFieldType luceneType : EnumSet.allOf(LuceneSortFieldType.class))
      lookup.put(luceneType.getJavaType(), luceneType.getLuceneType());
  }

  private Integer luceneType;
  private Class<?> javaType;

  LuceneSortFieldType(int luceneType, Class<?> javaType) {
    this.luceneType = luceneType;
    this.javaType = javaType;
  }

  public static int getLuceneTypeFor(Class<?> javaType) {
    Integer luceneTypeCode = lookup.get(javaType);
    return luceneTypeCode == null ? SortField.STRING_VAL : luceneTypeCode;
  }

  public Integer getLuceneType() {
    return luceneType;
  }

  public Class<?> getJavaType() {
    return javaType;
  }

}
