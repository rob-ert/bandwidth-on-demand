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
package nl.surfnet.bod.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.support.VirtualPortFactory;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Test;

public class LogEventTest {
  private static final String USER_ID = "user";
  private static final String GROUP_ID = "urn:group";

  @Test
  public void shouldCreateLogEvent() {
    try {
      DateTime now = DateTime.now();
      DateTimeUtils.setCurrentMillisFixed(now.toDate().getTime());

      VirtualPort virtualPort = new VirtualPortFactory().create();
      LogEvent logEvent = new LogEvent(USER_ID, GROUP_ID, LogEventType.CREATE, virtualPort);

      assertThat(logEvent.getUserId(), is(USER_ID));
      assertThat(logEvent.getAdminGroup(), is(GROUP_ID));
      assertThat(logEvent.getShortAdminGroup(), is("group"));
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

  @Test
  public void shouldLogNullDomainObject() {
    LogEvent logEvent = new LogEvent(USER_ID, GROUP_ID, LogEventType.CREATE, null);

    assertThat(logEvent.getDescription(), nullValue());
    assertThat(logEvent.getDomainObjectClass(), nullValue());
    assertThat(logEvent.getDomainObjectId(), nullValue());
  }

}
