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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

public class XmlUtilsTest {

  @Test
  public void shouldPreserveTimeZoneWithPositieveOffsetFromXml() throws DatatypeConfigurationException {
    int offSetInHours = 2;
    String xmlDate = String.format("2012-09-26T11:05:10%0+3d:00", offSetInHours);
    XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(xmlDate);

    DateTime timeStamp = XmlUtils.toDateTime(calendar);
    assertThat(timeStamp.getZone().getOffset(timeStamp.getMillis()), is(offSetInHours * 60 * 60 * 1000));
    assertThat(timeStamp.getMillis(), is(calendar.toGregorianCalendar().getTimeInMillis()));
  }

  @Test
  public void shouldPreserveTimeZoneWithNegativeOffsetFromXml() throws DatatypeConfigurationException {
    int offSetInHours = -4;

    String xmlDate = String.format("2012-09-26T11:05:10%0+3d:00", offSetInHours);
    XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(xmlDate);

    DateTime timeStamp = XmlUtils.toDateTime(calendar);
    assertThat(timeStamp.getZone().getOffset(timeStamp.getMillis()), is(offSetInHours * 60 * 60 * 1000));
    assertThat(timeStamp.getMillis(), is(calendar.toGregorianCalendar().getTimeInMillis()));
  }

  @Test
  public void shouldPreserveTimeZoneZeroOffsetFromXml() throws DatatypeConfigurationException {
    int offSetInHours = 0;

    String xmlDate = String.format("2012-09-26T11:05:10%0+3d:00", offSetInHours);
    XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(xmlDate);

    DateTime timeStamp = XmlUtils.toDateTime(calendar);
    assertThat(timeStamp.getZone().getOffset(timeStamp.getMillis()), is(offSetInHours * 60 * 60 * 1000));
    assertThat(timeStamp.getMillis(), is(calendar.toGregorianCalendar().getTimeInMillis()));
  }

  @Test
  public void shouldPreserveTimeZoneUTCFromXml() throws DatatypeConfigurationException {
    String xmlDate = "2012-09-26T11:05:10Z";
    XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(xmlDate);

    DateTime timeStamp = XmlUtils.toDateTime(calendar);
    assertThat(timeStamp.getZone().getOffset(timeStamp.getMillis()), is(0));
    assertThat(timeStamp.getMillis(), is(calendar.toGregorianCalendar().getTimeInMillis()));
  }

  @Test
  public void shouldPreserveTimeZoneWithPositieveOffsetToXml() throws DatatypeConfigurationException {
    int offSetInHours = 4;
    DateTime timeStamp = new DateTime(2012, 9, 26, 14, 40, 0, 0, DateTimeZone.forOffsetHours(offSetInHours));
    XMLGregorianCalendar xmlDateTime = XmlUtils.toGregorianCalendar(timeStamp);

    assertThat(xmlDateTime.getTimezone(), is(offSetInHours * 60));
    assertThat(xmlDateTime.toGregorianCalendar().getTimeInMillis(), is(xmlDateTime.toGregorianCalendar()
        .getTimeInMillis()));
  }

  @Test
  public void shouldPreserveTimeZoneWithNegativeOffsetToXml() throws DatatypeConfigurationException {
    int offSetInHours = -12;
    DateTime timeStamp = new DateTime(2012, 9, 26, 14, 40, 0, 0, DateTimeZone.forOffsetHours(offSetInHours));
    XMLGregorianCalendar xmlDateTime = XmlUtils.toGregorianCalendar(timeStamp);

    assertThat(xmlDateTime.getTimezone(), is(offSetInHours * 60));
    assertThat(xmlDateTime.toGregorianCalendar().getTimeInMillis(), is(xmlDateTime.toGregorianCalendar()
        .getTimeInMillis()));
  }

  @Test
  public void shouldPreserveTimeZoneWithZeroOffsetToXml() throws DatatypeConfigurationException {
    int offSetInHours = 0;
    DateTime timeStamp = new DateTime(2012, 9, 26, 14, 40, 0, 0, DateTimeZone.forOffsetHours(offSetInHours));
    XMLGregorianCalendar xmlDateTime = XmlUtils.toGregorianCalendar(timeStamp);

    assertThat(xmlDateTime.getTimezone(), is(offSetInHours * 60));
    assertThat(xmlDateTime.toGregorianCalendar().getTimeInMillis(), is(xmlDateTime.toGregorianCalendar()
        .getTimeInMillis()));
  }

  @Test
  public void shouldPreserveTimeZoneWithUTCToXml() throws DatatypeConfigurationException {
    DateTime timeStamp = new DateTime(2012, 9, 26, 14, 40, 0, 0, DateTimeZone.UTC);
    XMLGregorianCalendar xmlDateTime = XmlUtils.toGregorianCalendar(timeStamp);

    assertThat(xmlDateTime.getTimezone(), is(0));
    assertThat(xmlDateTime.toGregorianCalendar().getTimeInMillis(), is(xmlDateTime.toGregorianCalendar()
        .getTimeInMillis()));
  }

  @Test
  public void shouldConverStringToDateTimeUTCPlusOneHour() {
    DateTime dateTime = XmlUtils.getDateTimeFromXml("2013-02-19T15:02:01+01:00");

    assertThat(dateTime.getYear(), is(2013));
    assertThat(dateTime.getMonthOfYear(), is(2));
    assertThat(dateTime.getDayOfMonth(), is(19));
    assertThat(dateTime.getHourOfDay(), is(15));
    assertThat(dateTime.getMinuteOfHour(), is(02));
    assertThat(dateTime.getSecondOfMinute(), is(01));
  }

  @Test
  public void shouldConverStringToDateTimeUTC() {
    DateTime dateTime = XmlUtils.getDateTimeFromXml("2013-02-19T15:02:01Z");

    assertThat(dateTime.getYear(), is(2013));
    assertThat(dateTime.getMonthOfYear(), is(2));
    assertThat(dateTime.getDayOfMonth(), is(19));
    assertThat("Should add one hour, due to timezone", dateTime.getHourOfDay(), is(16));
    assertThat(dateTime.getMinuteOfHour(), is(02));
    assertThat(dateTime.getSecondOfMinute(), is(01));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowOnGarbage() {
    XmlUtils.getDateTimeFromXml("wrong-format");
  }

}
