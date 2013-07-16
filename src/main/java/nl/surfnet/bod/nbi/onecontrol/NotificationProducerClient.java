package nl.surfnet.bod.nbi.onecontrol;

import org.tmforum.mtop.fmw.wsdl.notp.v1_0.SubscribeException;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.UnsubscribeException;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.UnsubscribeResponse;

public interface NotificationProducerClient {

  public enum NotificationTopic {
    FAULT, INVENTORY, SERVICE
  }

  String subscribe(NotificationTopic topic, String consumerErp) throws SubscribeException;

  UnsubscribeResponse unsubscribe(NotificationTopic topic, String id) throws UnsubscribeException;
}
