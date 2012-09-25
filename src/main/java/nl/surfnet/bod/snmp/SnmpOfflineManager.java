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
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SnmpOfflineManager implements CommandResponder {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final LinkedBlockingDeque<PDU> receivedPdus = new LinkedBlockingDeque<>();

  private AbstractTransportMapping abstractTransportMapping = null;

  private final CommunityTarget communityTarget = new CommunityTarget();

  private final MessageDispatcher messageDispatcher = new MultiThreadedMessageDispatcher(ThreadPool.create(
      "DispatcherPool", 10), new MessageDispatcherImpl());

  @Value("${snmp.community}")
  private String community;

  @Value("${snmp.oid}")
  private String oid;

  @Value("${snmp.host}")
  private String host;

  @Value("${snmp.port}")
  private String port;

  @Async
  public void startup() {

    try {
      abstractTransportMapping = new DefaultUdpTransportMapping(new UdpAddress(host + port));

      messageDispatcher.addMessageProcessingModel(new MPv2c());

      SecurityProtocols.getInstance().addDefaultProtocols();

      communityTarget.setCommunity(new OctetString(community));

      final Snmp snmp = new Snmp(messageDispatcher, abstractTransportMapping);
      snmp.addCommandResponder(this);

      log.info("Starting listener on: " + abstractTransportMapping.getListenAddress());
      abstractTransportMapping.listen();
    }
    catch (IOException e) {
      log.error("Error: ", e);
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
    if (pdu != null) {
      receivedPdus.add(pdu);
      log.info("Trap Type = " + pdu.getType());
      log.info("Variable Bindings = " + pdu.getVariableBindings());
    }
  }

  public final PDU getOrWaitForLastPdu(final long seconds) {
    try {
      return receivedPdus.pollLast(seconds, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      log.error("Error: ", e);
      throw new RuntimeException(e);
    }
  }

  public final boolean isRunning() {
    return abstractTransportMapping != null && abstractTransportMapping.isListening();
  }

}