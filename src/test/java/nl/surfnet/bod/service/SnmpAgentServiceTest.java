package nl.surfnet.bod.service;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import nl.surfnet.bod.snmp.SnmpAgent;
import nl.surfnet.bod.snmp.SnmpOfflineManager;

public class SnmpAgentServiceTest {

  private final Properties properties = new Properties();
  private final SnmpAgentService snmpAgentService = new SnmpAgentService();
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
      ReflectionTestUtils.setField(o, "host", properties.getProperty("snmp.host"));
    }
  }

}