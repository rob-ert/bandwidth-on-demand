package nl.surfnet.bod.snmp;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Date;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.snmp4j.PDU;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

public class SnmpAgentTest {

  private final SnmpAgent snmpAgent = new SnmpAgent();

  private final SnmpOfflineManager snmpOfflineManager = new SnmpOfflineManager();

  private final PDU pdu = new PDU();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {

    final Properties properties = new Properties();
    properties.load(new ClassPathResource("bod.properties").getInputStream());

    final Object instances[] = { snmpAgent, snmpOfflineManager };

    for (final Object o : instances) {
      ReflectionTestUtils.setField(o, "community", properties.getProperty("snmp.community"));
      ReflectionTestUtils.setField(o, "oid", properties.getProperty("snmp.oid"));
      ReflectionTestUtils.setField(o, "host", properties.getProperty("snmp.host"));
      ReflectionTestUtils.setField(o, "port", properties.getProperty("snmp.port"));
    }

    snmpOfflineManager.startup();

    // need to specify the system up time
    pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new OctetString(new Date().toString())));
    pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(snmpAgent.getOid())));
    pdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress, new IpAddress(snmpAgent.getHost())));
    pdu.add(new VariableBinding(new OID(snmpAgent.getOid()), new OctetString("Major")));
    pdu.setType(PDU.NOTIFICATION);
  }

  @After
  public void tearDown() throws Exception {
    snmpOfflineManager.shutdown();
  }

  @Test
  public void should_send_and_receive_pdu() {
    snmpAgent.sendPdu(pdu);
    final PDU lastTrap = snmpOfflineManager.getOrWaitForLastTrap(10);
    assertThat(lastTrap.getType(), is(PDU.TRAP));
    final String lastVariableBindingsAsString = lastTrap.getVariableBindings().toString();
    assertThat(lastVariableBindingsAsString, containsString("1.3.6.1.2.1.1.3.0"));
    assertThat(
        lastVariableBindingsAsString,
        containsString("1.3.6.1.6.3.1.1.4.1.0 = 1.3.6.1.2.1.1.8, 1.3.6.1.6.3.18.1.3.0 = 127.0.0.1, 1.3.6.1.2.1.1.8 = Major"));
    assertThat(lastVariableBindingsAsString, containsString(snmpAgent.getOid().replaceFirst(".", "")));
  }

}
