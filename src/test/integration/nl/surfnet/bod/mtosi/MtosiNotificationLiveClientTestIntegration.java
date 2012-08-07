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
package nl.surfnet.bod.mtosi;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class MtosiNotificationLiveClientTestIntegration {

  private final Properties properties = new Properties();

  private MtosiNotificationLiveClient mtosiNotificationLiveClient;

  @Before
  public void setup() throws IOException {
    properties.load(ClassLoader.class.getResourceAsStream("/bod-default.properties"));
    mtosiNotificationLiveClient = new MtosiNotificationLiveClient(properties.get("mtosi.notification.retrieval.endpoint")
        .toString(), properties.get("mtosi.notification.sender.uri").toString());
  }

  @Test
  public void subscribeAndUnsubscribe() throws Exception {
    final String topic = "fault";
    final String subscriberId = mtosiNotificationLiveClient.subscribe(topic,
        "http://localhost:9009/mtosi/fmw/NotificationConsumer");
    assertThat(subscriberId, notNullValue());
    mtosiNotificationLiveClient.unsubscribe(subscriberId, topic);
  }

}
