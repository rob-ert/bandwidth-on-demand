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

import java.sql.Timestamp;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class TimeStampBridgeTest {

  private TimeStampBridge timeStampBridge;

  private Timestamp sqlTimeStamp;

  private DateTime dateTime;

  @Before
  public void onSetup() {
    dateTime = new DateTime().withDate(2012, 9, 17).withTime(16, 40, 0, 0);
    sqlTimeStamp = new Timestamp(dateTime.toDate().getTime());
    timeStampBridge = new TimeStampBridge();
  }

  @Test
  public void shouldHandleJodaDateTimeWithNegativeTimeZoneOffset() {
    dateTime = new DateTime(DateTimeZone.forOffsetHours(-4)).withDate(2012, 9, 17).withTime(16, 40, 0, 0);
    assertThat(timeStampBridge.objectToString(sqlTimeStamp), is("2012-09-17 16:40:00"));
  }

  @Test
  public void shouldHandleJodaDateTimeWithPositiveTimeZoneOffset() {
    dateTime = new DateTime(DateTimeZone.forOffsetHours(+4)).withDate(2012, 9, 17).withTime(16, 40, 0, 0);
    assertThat(timeStampBridge.objectToString(sqlTimeStamp), is("2012-09-17 16:40:00"));
  }

  @Test
  public void shouldHandleJodaDateTimeWithUTCZone() {
    dateTime = new DateTime(DateTimeZone.UTC).withDate(2012, 9, 17).withTime(16, 40, 0, 0);
    assertThat(timeStampBridge.objectToString(sqlTimeStamp), is("2012-09-17 16:40:00"));
  }

  @Test
  public void shouldHandleJodaDateTime() {
    assertThat(timeStampBridge.objectToString(sqlTimeStamp), is("2012-09-17 16:40:00"));
  }

  @Test
  public void shouldHandleSqlDateTime() {
    assertThat(timeStampBridge.objectToString(dateTime), is("2012-09-17 16:40:00"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenOtherClass() {
    timeStampBridge.objectToString(new Date());
  }

  @Test
  public void shouldReturnStringWhenArgumentIsNull() {
    assertThat(timeStampBridge.objectToString(null), nullValue());
  }
}
