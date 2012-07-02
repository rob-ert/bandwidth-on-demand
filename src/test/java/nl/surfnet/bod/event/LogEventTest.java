package nl.surfnet.bod.event;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.support.VirtualPortFactory;

import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LogEventTest {
  private final static String USER_ID = "user";
  private final static String GROUP_ID = "urn:group";

  @Test
  public void shouldCreateLogEvent() {
    try {
      LocalDateTime now = LocalDateTime.now();
      DateTimeUtils.setCurrentMillisFixed(now.toDate().getTime());

      VirtualPort virtualPort = new VirtualPortFactory().create();
      LogEvent logEvent = new LogEvent(USER_ID, GROUP_ID, LogEventType.CREATE, virtualPort);

      assertThat(logEvent.getUserId(), is(USER_ID));
      assertThat(logEvent.getGroupIds(), is("["+GROUP_ID+"]"));
      assertThat(logEvent.getCreated(), is(now));
      assertThat(logEvent.getClassName(), is(virtualPort.getClass().getName()));
      assertThat(logEvent.getSerializedObject().toString(), is(virtualPort.toString()));
    }
    finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
  }
}
