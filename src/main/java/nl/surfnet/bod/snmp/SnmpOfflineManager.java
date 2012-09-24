package nl.surfnet.bod.snmp;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.TransportIpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

public class SnmpOfflineManager implements CommandResponder, Runnable {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final LinkedBlockingDeque<PDU> lastRequests = new LinkedBlockingDeque<>();

  private AbstractTransportMapping abstractTransportMapping = null;

  private final Thread commandResponder = new Thread(this);

  public SnmpOfflineManager() {
    try {
      abstractTransportMapping = new DefaultUdpTransportMapping(new UdpAddress("localhost/1620"));
    }
    catch (IOException e) {
      log.error("Error: ", e);
    }
  }

  public SnmpOfflineManager(final TransportIpAddress transportIpAddress) {
    try {
      if (transportIpAddress instanceof TcpAddress) {
        abstractTransportMapping = new DefaultTcpTransportMapping((TcpAddress) transportIpAddress);
      }
      else {
        abstractTransportMapping = new DefaultUdpTransportMapping((UdpAddress) transportIpAddress);
      }
    }
    catch (IOException e) {
      log.error("Error: ", e);
    }
  }

  @Override
  public void run() {

    final MessageDispatcher messageDispatcher = new MultiThreadedMessageDispatcher(ThreadPool.create("DispatcherPool",
        10), new MessageDispatcherImpl());

    // add message processing models
    // messageDispatcher.addMessageProcessingModel(new MPv1());
    messageDispatcher.addMessageProcessingModel(new MPv2c());

    // add all security protocols
    SecurityProtocols.getInstance().addDefaultProtocols();
    // SecurityProtocols.getInstance().addPrivacyProtocol(new Pri/v3DES());

    // Create Target
    final CommunityTarget communityTarget = new CommunityTarget();
    communityTarget.setCommunity(new OctetString("public"));

    final Snmp snmp = new Snmp(messageDispatcher, abstractTransportMapping);
    snmp.addCommandResponder(this);

    log.info("Starting listener on: " + abstractTransportMapping.getListenAddress());
    try {
      abstractTransportMapping.listen();
    }
    catch (IOException e) {
      log.error("Error: ", e);
    }

  }

  public void startup() {
    commandResponder.start();
    while (!isListening()) {
      try {
        Thread.sleep(250L);
      }
      catch (InterruptedException e) {
        log.error("Error: ", e);
      }
    }
  }

  public void shutdown() {
    if (abstractTransportMapping != null && abstractTransportMapping.isListening()) {
      try {
        log.info("Closing listener on: " + abstractTransportMapping.getListenAddress());
        abstractTransportMapping.close();
      }
      catch (IOException e) {
        log.error("Error: ", e);
      }
    }
  }

  @Override
  public void processPdu(final CommandResponderEvent commandResponderEvent) {
    log.info("Received CommandResponderEvent: " + commandResponderEvent);
    final PDU pdu = commandResponderEvent.getPDU();
    lastRequests.add(pdu);
    if (pdu != null) {
      log.info("Trap Type = " + pdu.getType());
      log.info("Variable Bindings = " + pdu.getVariableBindings());
    }
  }

  public final PDU getOrWaitForLastTrap(final long seconds) {
    try {
      return lastRequests.pollLast(seconds, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      log.error("Error: ", e);
      throw new RuntimeException(e);
    }
  }

  public final boolean isListening() {
    return abstractTransportMapping != null && abstractTransportMapping.isListening();
  }

}