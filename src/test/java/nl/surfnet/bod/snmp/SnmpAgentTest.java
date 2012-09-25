package nl.surfnet.bod.snmp;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.snmp4j.PDU;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

public class SnmpAgentTest {

  private final Properties properties = new Properties();

  private final SnmpAgent snmpAgent = new SnmpAgent();
  private final SnmpOfflineManager snmpOfflineManager = new SnmpOfflineManager();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

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
  public void should_send_and_receive_major_pdu_trap() {
    final String severity = "Major";
    final int pduType = PDU.TRAP;

    snmpAgent.sendPdu(snmpAgent.getPdu(properties.getProperty("snmp.oid"), severity, pduType));

    final PDU lastPdu = snmpOfflineManager.getOrWaitForLastPdu(10);
    final String lastVariableBindingsAsString = lastPdu.getVariableBindings().toString();

    assertThat(lastPdu.getType(), is(pduType));
    assertThat(lastVariableBindingsAsString, containsString(severity));
    assertThat(lastVariableBindingsAsString, containsString(properties.getProperty("snmp.oid").replaceFirst(".", "")));
  }

  private void prepareTestInstances() {
    final Object instances[] = { snmpAgent, snmpOfflineManager };
    for (final Object o : instances) {
      ReflectionTestUtils.setField(o, "community", properties.getProperty("snmp.community"));
      ReflectionTestUtils.setField(o, "oid", properties.getProperty("snmp.oid"));
      ReflectionTestUtils.setField(o, "host", properties.getProperty("snmp.host"));
      ReflectionTestUtils.setField(o, "port", properties.getProperty("snmp.port"));
    }
  }

}
