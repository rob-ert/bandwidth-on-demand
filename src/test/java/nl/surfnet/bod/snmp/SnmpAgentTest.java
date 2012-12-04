/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.snmp;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.snmp4j.PDU;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

public class SnmpAgentTest {

  private final Properties properties = new Properties();
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
  @Ignore("Gives to much trouble with 'Address already in use'")
  public void should_send_and_receive_port_disappeared() {
    snmpAgent.sendPdu(snmpAgent.getPdu(properties.getProperty("snmp.oid.nms.port.disappeared")));

    final PDU lastPdu = snmpOfflineManager.getOrWaitForLastPdu(5);
    final String lastVariableBindingsAsString = lastPdu.getVariableBindings().toString();

    assertThat(lastPdu.getType(), is(PDU.TRAP));
    assertThat(lastVariableBindingsAsString, containsString(properties.getProperty("snmp.oid.nms.port.disappeared")
        .replaceFirst(".", "")));
  }

  private void prepareTestInstances() {
    final Object instances[] = { snmpAgent, snmpOfflineManager };
    for (final Object o : instances) {
      ReflectionTestUtils.setField(o, "community", properties.getProperty("snmp.community"));
      ReflectionTestUtils.setField(o, "host", "localhost/1622");
    }
  }

}
