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
package nl.surfnet.bod.service;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import nl.surfnet.bod.snmp.SnmpAgent;
import nl.surfnet.bod.snmp.SnmpOfflineManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

public class SnmpAgentServiceTest {

  private final Properties properties = new Properties();
  private final SnmpAgentService snmpAgentService = new SnmpAgentService();
  private final SnmpAgent snmpAgent = new SnmpAgent();
  private final SnmpOfflineManager snmpOfflineManager = new SnmpOfflineManager();

  @Before
  public void setUp() throws Exception {
    properties.load(new ClassPathResource("bod.properties").getInputStream());
    prepareTestInstances();
    snmpOfflineManager.startup();
  }

  @After
  public void tearDown() throws Exception {
    snmpOfflineManager.shutdown();
  }

  @Test
  public void should_send_missing_port_event() {
    final String portSnmpId = "1.2.3.4.5";
    snmpAgentService.sendMissingPortEvent(portSnmpId);
    final String lastPduAsString = snmpOfflineManager.getOrWaitForLastPduAsString(5);
    assertThat(lastPduAsString, containsString(snmpAgent.getOidNmsPortDisappeared(portSnmpId).replaceFirst(".", "")));
  }

  @Test
  public void should_send_missing_institute_event() {
    final String instituteSnmpId = "6.7.8.9.10";
    snmpAgentService.sendMissingInstituteEvent(instituteSnmpId);
    final String lastPduAsString = snmpOfflineManager.getOrWaitForLastPduAsString(5);
    assertThat(lastPduAsString,
        containsString(snmpAgent.getOidIddInstituteDisappeared(instituteSnmpId).replaceFirst(".", "")));
  }

  private void prepareTestInstances() {
    ReflectionTestUtils.setField(snmpAgent, "oidNmsPortDisappeared",
        properties.getProperty("snmp.oid.nms.port.disappeared"));
    ReflectionTestUtils.setField(snmpAgent, "oidIddInstituteDisappeared",
        properties.getProperty("snmp.oid.idd.institute.disappeared"));
    ReflectionTestUtils.setField(snmpAgentService, "snmpAgent", snmpAgent);
    final Object instances[] = { snmpAgent, snmpOfflineManager };
    for (final Object o : instances) {
      ReflectionTestUtils.setField(o, "community", properties.getProperty("snmp.community"));
      ReflectionTestUtils.setField(o, "host", "localhost/1622");
    }
  }

}
