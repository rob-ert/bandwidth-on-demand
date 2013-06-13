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
package nl.surfnet.bod.service;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;

import nl.surfnet.bod.snmp.SnmpAgent;
import nl.surfnet.bod.snmp.SnmpOfflineManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

@Ignore("Will enable when we really do SNMP")
public class SnmpAgentServiceTest {

  private final Properties properties = new Properties();
  private final SnmpAgentService snmpAgentService = new SnmpAgentService();
  private final SnmpAgent snmpAgent = new SnmpAgent();
  private final SnmpOfflineManager snmpOfflineManager = new SnmpOfflineManager();
  private ServerSocket serverSocket;

  @Before
  public void setUp() throws Exception {
    properties.load(new ClassPathResource("bod.properties").getInputStream());
    serverSocket = new ServerSocket(0);
    prepareTestInstances();
    snmpOfflineManager.startup();
  }

  @After
  public void tearDown() throws Exception {
    snmpOfflineManager.shutdown();
    serverSocket.close();
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

  private void prepareTestInstances() throws IOException {
    ReflectionTestUtils.setField(snmpAgent, "oidNmsPortDisappeared",
        properties.getProperty("snmp.oid.nms.port.disappeared"));
    ReflectionTestUtils.setField(snmpAgent, "oidIddInstituteDisappeared",
        properties.getProperty("snmp.oid.idd.institute.disappeared"));
    final Object instances[] = { snmpAgent, snmpOfflineManager };
    for (final Object o : instances) {
      ReflectionTestUtils.setField(o, "community", properties.getProperty("snmp.community"));
      ReflectionTestUtils.setField(o, "host", "localhost/" + serverSocket.getLocalPort());
    }
    ReflectionTestUtils.setField(snmpAgentService, "snmpAgent", snmpAgent);
  }

}
