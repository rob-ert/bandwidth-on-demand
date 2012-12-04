/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.snmp;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.*;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SnmpOfflineManager implements CommandResponder {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final LinkedBlockingDeque<PDU> receivedPdus = new LinkedBlockingDeque<>();

  private TransportMapping<UdpAddress> transportMapping = null;

  @Value("${snmp.community}")
  private String community;

  @Value("${snmp.host}")
  private String host;

  @Value("${snmp.development}")
  private boolean isDevelopment = true;

  @Value("${snmp.enabled}")
  private boolean isEnabled = false;

  @PostConstruct
  public void startup() {

    if (!isEnabled()) {
      return;
    }

    if (isDevelopment) {
      log.info("USING OFFLINE SNMP MANAGER!");

      registerShutdownHook();
      SecurityProtocols.getInstance().addDefaultProtocols();

      final CommunityTarget communityTarget = new CommunityTarget();
      communityTarget.setCommunity(new OctetString(community));

      final MessageDispatcher messageDispatcher = new MultiThreadedMessageDispatcher(ThreadPool.create(
          "DispatcherPool", 10), new MessageDispatcherImpl());
      messageDispatcher.addMessageProcessingModel(new MPv2c());

      try {
        transportMapping = new DefaultUdpTransportMapping(new UdpAddress(host));
        final Snmp snmp = new Snmp(messageDispatcher, transportMapping);
        snmp.addCommandResponder(this);
        log.info("Starting listener on: " + transportMapping.getListenAddress());
        transportMapping.listen();
      }
      catch (IOException e) {
        log.error("Error: ", e);
      }
    }
    else {
      log.info("Using NOC's SNMP Manager");

    }
  }

  private boolean isEnabled() {
    if (!isEnabled) {
      log.warn("All SNMP activities are disabled. Set snmp.enabled = true to re-enable it.");
    }
    return isEnabled;
  }

  @PreDestroy
  public void shutdown() {
    if (!isEnabled()) {
      return;
    }

    if (transportMapping != null && transportMapping.isListening()) {
      try {
        log.info("Closing listener on: " + transportMapping.getListenAddress());
        transportMapping.close();
      }
      catch (IOException e) {
        log.error("Error: ", e);
      }
    }
  }

  @Override
  public void processPdu(final CommandResponderEvent commandResponderEvent) {
    if (!isEnabled()) {
      return;
    }

    log.info("Received CommandResponderEvent: " + commandResponderEvent);
    final PDU pdu = commandResponderEvent.getPDU();
    if (pdu != null) {
      receivedPdus.add(pdu);
      log.info("Trap Type = " + pdu.getType());
      log.info("Variable Bindings = " + pdu.getVariableBindings());
    }
  }

  public final String getOrWaitForLastPduAsString(final long seconds) {
    return getOrWaitForLastPdu(seconds).toString();
  }

  public final PDU getOrWaitForLastPdu(final long seconds) {
    if (isDevelopment) {
      try {
        return receivedPdus.pollLast(seconds, TimeUnit.SECONDS);
      }
      catch (InterruptedException e) {
        log.error("Error: ", e);
      }
    }
    return null;
  }

  public final boolean isRunning() {
    return transportMapping != null && transportMapping.isListening();
  }

  private void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        shutdown();
      }
    });
  }

}