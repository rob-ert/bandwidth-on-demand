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
    return convert(object);
  }

  public static String convert(Object object) {
    String result;

    if (object == null) {
      result = null;
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
