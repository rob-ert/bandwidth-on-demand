package nl.surfnet.bod.nbi.onecontrol.offline;

import nl.surfnet.bod.nbi.onecontrol.MonitoredNotificationConsumer;
import org.joda.time.DateTime;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.Notify;

@Component
@Profile("onecontrol-offline")
public class NotificationConsumerOffline implements MonitoredNotificationConsumer {

  @Override
  public void notify(Header mtopHeader, Notify mtopBody) {
  }

  @Override
  public DateTime getTimeOfLastHeartbeat() {
    return DateTime.now();
  }
}
