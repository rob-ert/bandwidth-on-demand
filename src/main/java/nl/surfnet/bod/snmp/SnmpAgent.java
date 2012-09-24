package nl.surfnet.bod.snmp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TransportIpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpAgent {

  private final Logger log = LoggerFactory.getLogger(getClass());

  public static final String DEFAULT_COMMUNITY = "public";

  public static final String DEFAULT_OID = ".1.3.6.1.2.1.1.8";

  public static final String DEFAULT_IP_ADDRESS = "127.0.0.1";

  public static final int DEFAULT_PORT = 1620;

  private final TransportIpAddress transportIpAddress;

  public SnmpAgent() {
    transportIpAddress = new UdpAddress(DEFAULT_IP_ADDRESS + "/" + DEFAULT_PORT);
  }

  public SnmpAgent(final TransportIpAddress transportIpAddress) {
    this.transportIpAddress = transportIpAddress;
  }

  public void sendTrap(final PDU pdu) {
    try {
      // Create Transport Mapping
      final TransportMapping transportMapping = new DefaultUdpTransportMapping();
      transportMapping.listen();

      // Create Target
      final CommunityTarget communityTarget = new CommunityTarget();
      communityTarget.setCommunity(new OctetString(DEFAULT_COMMUNITY));
      communityTarget.setVersion(SnmpConstants.version2c);

      communityTarget.setAddress(transportIpAddress);
      communityTarget.setRetries(2);
      communityTarget.setTimeout(5000);

      // Send the PDU
      Snmp snmp = new Snmp(transportMapping);
      log.info("Sending V2 Trap: {} to DEFAULT_COMMUNITY: {}", pdu, communityTarget);
      snmp.send(pdu, communityTarget);
      snmp.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}