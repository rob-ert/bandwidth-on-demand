package nl.surfnet.bod.service;

import javax.annotation.Resource;

import org.snmp4j.PDU;
import org.springframework.stereotype.Service;

import nl.surfnet.bod.snmp.SnmpAgent;

@Service
public class SnmpAgentService {

  @Resource
  private SnmpAgent snmpAgent;

  public void sendMissingPortEvent(final String portSnmpId) {
    final PDU pdu = snmpAgent
        .getPdu(snmpAgent.getOidNmsPortDisappeared(portSnmpId), SnmpAgent.SEVERITY_MAJOR, PDU.TRAP);
    snmpAgent.sendPdu(pdu);
  }

  public void sendMissingInstituteEvent(final String instituteSnmpId) {
    final PDU pdu = snmpAgent.getPdu(snmpAgent.getOidIddInstituteDisappeared(instituteSnmpId),
        SnmpAgent.SEVERITY_MAJOR, PDU.TRAP);
    snmpAgent.sendPdu(pdu);
  }

}
