package nl.surfnet.bod.nbi.onecontrol;

import org.joda.time.DateTime;
import org.tmforum.mtop.fmw.wsdl.notc.v1_0.NotificationConsumer;

public interface MonitoredNotificationConsumer extends NotificationConsumer {

  DateTime getTimeOfLastHeartbeat();
}
