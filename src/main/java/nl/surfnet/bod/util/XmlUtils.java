package nl.surfnet.bod.util;

import java.math.BigInteger;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;

public final class XmlUtils {

  private XmlUtils() {
  }

  public static Optional<DateTime> getDateFrom(XMLGregorianCalendar calendar) {
    if (calendar == null) {
      return Optional.absent();
    }

    GregorianCalendar gregorianCalendar = calendar.toGregorianCalendar();
    int timeZoneOffset = gregorianCalendar.getTimeZone().getOffset(gregorianCalendar.getTimeInMillis());
    // Create Timestamp while preserving the timezone, NO conversion
    return Optional.of(new DateTime(gregorianCalendar.getTime(), DateTimeZone.forOffsetMillis(timeZoneOffset)));
  }

  public static Optional<XMLGregorianCalendar> getXmlTimeStampFromDateTime(DateTime timeStamp) {
    if (timeStamp == null) {
      return Optional.absent();
    }

    XMLGregorianCalendar calendar = null;
    try {
      calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(BigInteger.valueOf(timeStamp.getYear()),
          timeStamp.getMonthOfYear(), timeStamp.getDayOfMonth(), timeStamp.getHourOfDay(), timeStamp.getMinuteOfHour(),
          timeStamp.getSecondOfMinute(), null, (timeStamp.getZone().getOffset(timeStamp.getMillis()) / (60 * 1000)));
    }
    catch (DatatypeConfigurationException e) {
      Throwables.propagate(e);
    }

    return Optional.of(calendar);
  }


}
