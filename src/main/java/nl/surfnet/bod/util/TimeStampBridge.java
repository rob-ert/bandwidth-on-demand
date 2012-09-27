package nl.surfnet.bod.util;

import java.sql.Timestamp;

import nl.surfnet.bod.web.WebUtils;

import org.hibernate.search.bridge.StringBridge;
import org.joda.time.DateTime;

/**
 * Handles parsing of timestamps to a String so it can be searched. Needed since
 * BoD uses joda timestamps and sql time stamps.
 * 
 */
public class TimeStampBridge implements StringBridge {

  @Override
  public String objectToString(Object object) {
    String result = null;

    if (object == null) {
      // null
    }
    else if (DateTime.class.isAssignableFrom(object.getClass())) {
      DateTime dateTime = (DateTime) object;
      result = dateTime.toString(WebUtils.DEFAULT_DATE_TIME_FORMATTER);
    }
    else if (Timestamp.class.isAssignableFrom(object.getClass())) {
      Timestamp timestamp = (Timestamp) object;
      result = WebUtils.DEFAULT_DATE_TIME_FORMATTER.print(timestamp.getTime());
    }
    else {
      throw new IllegalArgumentException("Bridge is not suitable for handling objects of type: " + object);
    }

    return result;
  }

}
