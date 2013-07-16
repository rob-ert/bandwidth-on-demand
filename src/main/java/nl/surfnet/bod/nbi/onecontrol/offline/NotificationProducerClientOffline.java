package nl.surfnet.bod.nbi.onecontrol.offline;

import nl.surfnet.bod.nbi.onecontrol.NotificationProducerClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.SubscribeException;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.UnsubscribeException;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.UnsubscribeResponse;

@Component
@Profile("onecontrol-offline")
public class NotificationProducerClientOffline implements NotificationProducerClient {
  @Override
  public String subscribe(NotificationTopic topic, String consumerErp) throws SubscribeException {
    return "1";
  }

  @Override
  public UnsubscribeResponse unsubscribe(NotificationTopic topic, String id) throws UnsubscribeException {
    return null;
  }
}
