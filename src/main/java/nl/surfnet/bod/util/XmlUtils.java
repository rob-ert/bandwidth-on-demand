/**
 * Copyright (c) 2012, 2013 SURFnet BV
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

import java.math.BigInteger;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.format.datetime.joda.DateTimeFormatterFactory;

import com.google.common.base.Function;

public final class XmlUtils {

  private static DateTimeFormatterFactory dateTimeFormatterFactory = new DateTimeFormatterFactory(
      "yyyy-MM-dd'T'HH:mm:ssZ");

  public static final Function<XMLGregorianCalendar, DateTime> xmlCalendarToDateTime = new Function<XMLGregorianCalendar, DateTime>() {
    public DateTime apply(XMLGregorianCalendar calendar) {
      return XmlUtils.toDateTime(calendar);
    }
  };

  public static final Function<DateTime, XMLGregorianCalendar> dateTimeToXmlCalendar = new Function<DateTime, XMLGregorianCalendar>() {
    public XMLGregorianCalendar apply(DateTime dateTime) {
      return XmlUtils.toGregorianCalendar(dateTime);
    }
  };

  private XmlUtils() {
  }

  public static DateTime toDateTime(XMLGregorianCalendar calendar) {
    GregorianCalendar gregorianCalendar = calendar.toGregorianCalendar();
    int timeZoneOffset = gregorianCalendar.getTimeZone().getOffset(gregorianCalendar.getTimeInMillis());

    return new DateTime(gregorianCalendar.getTime(), DateTimeZone.forOffsetMillis(timeZoneOffset));
  }

  public static XMLGregorianCalendar toGregorianCalendar(DateTime timeStamp) {
    try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(
          BigInteger.valueOf(timeStamp.getYear()),
          timeStamp.getMonthOfYear(),
          timeStamp.getDayOfMonth(),
          timeStamp.getHourOfDay(),
          timeStamp.getMinuteOfHour(),
          timeStamp.getSecondOfMinute(),
          null,
          (timeStamp.getZone().getOffset(timeStamp.getMillis()) / (60 * 1000)));
    } catch (DatatypeConfigurationException e) {
      throw new AssertionError(e);
    }
  }

  public static DateTime getDateTimeFromXml(String xmlTimeStamp) {
    return DateTime.parse(xmlTimeStamp, dateTimeFormatterFactory.createDateTimeFormatter());
  }

}
