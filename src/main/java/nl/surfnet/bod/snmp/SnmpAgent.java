/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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