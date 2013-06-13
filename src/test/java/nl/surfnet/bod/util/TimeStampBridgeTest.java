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
