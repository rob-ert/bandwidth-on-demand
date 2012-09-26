package nl.surfnet.bod.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import nl.surfnet.bod.snmp.SnmpAgent;

@Service
public class SnmpAgentService {

  @Resource
  private SnmpAgent snmpAgent;

  public void sendMissingPortEvent(final String snmpPortId) {
    snmpAgent.sendPdu(snmpAgent.getPdu(snmpAgent.getOidNmsPortDisappeared(snmpPortId)));
  }

  public void sendMissingInstituteEvent(final String snmpInstituteId) {
    snmpAgent.sendPdu(snmpAgent.getPdu(snmpAgent.getOidIddInstituteDisappeared(snmpInstituteId)));
  }

}
