package nl.surfnet.bod.snmp;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SnmpAgent {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Value("${snmp.community}")
  private String community;

  @Value("${snmp.oid.nms.port.disappeared}")
  private String oidNmsPortDisappeared;

  @Value("${snmp.oid.idd.institute.disappeared}")
  private String oidIddInstituteDisappeared;

  @Value("${snmp.host}")
  private String host;

  @Value("${snmp.retries}")
  private int retries;

  @Value("${snmp.timeout.millis}")
  private long timeoutInMillis;

  public void sendPdu(final PDU pdu) {
    try {
      final TransportMapping<UdpAddress> transportMapping = new DefaultUdpTransportMapping();
      transportMapping.listen();

      final CommunityTarget communityTarget = new CommunityTarget();
      communityTarget.setCommunity(new OctetString(community));
      communityTarget.setVersion(SnmpConstants.version2c);
      communityTarget.setAddress(new UdpAddress(host));
      communityTarget.setRetries(retries);
      communityTarget.setTimeout(timeoutInMillis);

      final Snmp snmp = new Snmp(transportMapping);
      log.info("Sending pdu: {} to community: {}", pdu, communityTarget);
      snmp.send(pdu, communityTarget);
      snmp.close();
    }
    catch (IOException e) {
      log.error("Error: ", e);
    }
  }

  public PDU getPdu(final String oid) {
    final PDU pdu = DefaultPDUFactory.createPDU(SnmpConstants.version2c);
    pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
    pdu.setType(PDU.TRAP);
    return pdu;
  }

  public final String getOidNmsPortDisappeared(final String nmsPortId) {
    return oidNmsPortDisappeared + "." + nmsPortId;
  }

  public final String getOidIddInstituteDisappeared(final String instituteId) {
    return oidIddInstituteDisappeared + "." + instituteId;
  }

}