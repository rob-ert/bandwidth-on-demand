/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.snmp;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
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

  @Value("${snmp.disabled}")
  private boolean isDisabled = true;

  @PostConstruct
  public void startup() {

    if (disabledMessage()) {
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

  private boolean disabledMessage() {
    if (isDisabled) {
      log.warn("All SNMP activities are disabled. Set snmp.disabled = false to re-enable it.");
      return true;
    }
    return false;
  }

  @PreDestroy
  public void shutdown() {
    if (disabledMessage()) {
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
    if (disabledMessage()) {
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
    if (disabledMessage()) {
      return null;
    }
    return getOrWaitForLastPdu(seconds).toString();
  }

  public final PDU getOrWaitForLastPdu(final long seconds) {
    if (disabledMessage()) {
      return null;
    }
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
      public void run() {
        shutdown();
      }
    });
  }

}