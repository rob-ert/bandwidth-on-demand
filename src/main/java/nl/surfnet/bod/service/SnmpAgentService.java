package nl.surfnet.bod.service;

import javax.annotation.Resource;

import org.snmp4j.PDU;
import org.springframework.stereotype.Service;

import nl.surfnet.bod.snmp.SnmpAgent;

@Service
public class SnmpAgentService {

  @Resource
  private SnmpAgent snmpAgent;

  public void sendMissingPortEvent(final String snmpPortId) {
    final PDU pdu = snmpAgent
        .getPdu(snmpAgent.getOidNmsPortDisappeared(snmpPortId), SnmpAgent.SEVERITY_MAJOR, PDU.TRAP);
    snmpAgent.sendPdu(pdu);
  }

  public void sendMissingInstituteEvent(final String snmpInstituteId) {
    final PDU pdu = snmpAgent.getPdu(snmpAgent.getOidIddInstituteDisappeared(snmpInstituteId),
        SnmpAgent.SEVERITY_MAJOR, PDU.TRAP);
    snmpAgent.sendPdu(pdu);
  }

}
