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

import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.VirtualPortFactory;

import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class LogEventTest {
  private final static String USER_ID = "user";
  private final static String GROUP_ID = "urn:group";

  @Test
  public void shouldCreateLogEvent() {
    try {
      LocalDateTime now = LocalDateTime.now();
      DateTimeUtils.setCurrentMillisFixed(now.toDate().getTime());

      VirtualPort virtualPort = new VirtualPortFactory().create();
      LogEvent logEvent = new LogEvent(USER_ID, GROUP_ID, LogEventType.CREATE, virtualPort);

      assertThat(logEvent.getUserId(), is(USER_ID));
      assertThat(logEvent.getGroupIds(), is("[" + GROUP_ID + "]"));
      assertThat(logEvent.getCreated(), is(now));
      assertThat(logEvent.getClassName(), is(virtualPort.getClass().getSimpleName()));
      assertThat(logEvent.getSerializedObject().toString(), is(virtualPort.toString()));
    }
    finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
  }

  @Test
  public void shouldLogClassOfElementInCaseOfList() {
    PhysicalPort port = new PhysicalPortFactory().create();
    List<PhysicalPort> ports = Lists.newArrayList(port);

    LogEvent logEvent = new LogEvent(USER_ID, GROUP_ID, LogEventType.CREATE, ports);
    assertThat(logEvent.getClassName(), is(String.format(LogEvent.LIST_STRING, 1, port.getClass().getSimpleName())));
  }

  @Test
  public void shouldLogEmptyList() {
    LogEvent logEvent = new LogEvent(USER_ID, GROUP_ID, LogEventType.CREATE, Lists.newArrayList());

    assertThat(logEvent.getClassName(), is(LogEvent.LIST_EMPTY));
  }

  @Test
  public void shouldLogNullDomainObject() {
    LogEvent logEvent = new LogEvent(USER_ID, GROUP_ID, LogEventType.CREATE, null);

    assertThat(logEvent.getClassName(), nullValue());
  }
}
