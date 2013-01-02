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
package nl.surfnet.bod.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.support.VirtualPortFactory;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Test;

import com.google.common.collect.Lists;

public class LogEventTest {
  private static final String USER_ID = "user";
  private static final String GROUP_ID = "urn:group";

  @Test
  public void shouldCreateLogEvent() {
    try {
      DateTime now = DateTime.now();
      DateTimeUtils.setCurrentMillisFixed(now.toDate().getTime());

      VirtualPort virtualPort = new VirtualPortFactory().create();
      LogEvent logEvent = new LogEvent(USER_ID, Lists.newArrayList(GROUP_ID), LogEventType.CREATE, virtualPort);

      assertThat(logEvent.getUserId(), is(USER_ID));
      assertThat(logEvent.getAdminGroups(), hasItem(GROUP_ID));
      assertThat(logEvent.getCreated(), is(now));
      assertThat(logEvent.getDomainObjectClass(), is(virtualPort.getClass().getSimpleName()));
      assertThat(logEvent.getDescription(), is(virtualPort.getLabel()));
      assertThat(logEvent.getSerializedObject().toString(), is(virtualPort.toString()));
      assertThat(logEvent.getDomainObjectId(), is(virtualPort.getId()));
    }
    finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
  }

}
