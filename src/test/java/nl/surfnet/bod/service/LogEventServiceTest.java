package nl.surfnet.bod.service;

import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.event.LogEventType;
import nl.surfnet.bod.repo.LogEventRepo;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LogEventServiceTest {

  private static final String GROUP_ID = "urn:groupie";
  private static final String LOG_DETAILS = "The reason why";

  @Mock
  private LogEventRepo repoMock;

  @Mock
  private Logger logMock;

  @InjectMocks
  private LogEventService subject;

  private RichUserDetails user = new RichUserDetailsFactory().addUserGroup(GROUP_ID).create();

  private VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();

  @Test
  public void shouldCreateLogEvent() {
    try {
      LocalDateTime now = LocalDateTime.now();
      DateTimeUtils.setCurrentMillisFixed(now.toDate().getTime());

      LogEvent logEvent = subject.createLogEvent(user, LogEventType.CREATE, vrg, LOG_DETAILS);

      assertThat(logEvent.getUserId(), is(user.getUsername()));
      assertThat(logEvent.getGroupIds(), is("[" + GROUP_ID + "]"));
      assertThat(logEvent.getEventType(), is(LogEventType.CREATE));

      assertThat(logEvent.getClassName(), is(vrg.getClass().getName()));
      assertThat(logEvent.getDetails(), is(LOG_DETAILS));

      assertThat(logEvent.getSerializedObject(), is(vrg.toString()));
      assertThat(logEvent.getCreated(), is(now));
    }
    finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
  }

  @Test
  public void shouldPersistEvent() {
    LogEvent logEvent = new LogEvent(user.getUsername(), GROUP_ID, LogEventType.UPDATE, vrg);

    subject.handleEvent(logMock, logEvent);

    verify(logMock).info(anyString(), eq(logEvent));
    verify(repoMock).save(logEvent);
  }

}