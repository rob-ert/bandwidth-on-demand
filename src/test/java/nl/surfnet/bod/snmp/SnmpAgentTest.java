package nl.surfnet.bod.snmp;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
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
