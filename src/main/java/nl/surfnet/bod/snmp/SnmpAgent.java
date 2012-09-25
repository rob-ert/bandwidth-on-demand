package nl.surfnet.bod.snmp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SnmpAgent {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Value("${snmp.community}")
  private String community;

  @Value("${snmp.oid}")
  private String oid;

  @Value("${snmp.host}")
  private String host;

  @Value("${snmp.port}")
  private String port;

  public void sendPdu(final PDU pdu) {
    try {
      // Create Transport Mapping
      final TransportMapping transportMapping = new DefaultUdpTransportMapping();
      transportMapping.listen();

      // Create Target
      final CommunityTarget communityTarget = new CommunityTarget();
      communityTarget.setCommunity(new OctetString(community));
      communityTarget.setVersion(SnmpConstants.version2c);

      communityTarget.setAddress(new UdpAddress(getHost() + getPort()));
      communityTarget.setRetries(2);
      communityTarget.setTimeout(5000);

      // Send the PDU
      final Snmp snmp = new Snmp(transportMapping);
      log.info("Sending v2 trap: {} to community: {}", pdu, communityTarget);
      snmp.send(pdu, communityTarget);
      snmp.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected final String getCommunity() {
    return community;
  }

  protected final String getOid() {
    return oid;
  }

  protected final String getHost() {
    return host;
  }

  protected final String getPort() {
    return port;
  }
}