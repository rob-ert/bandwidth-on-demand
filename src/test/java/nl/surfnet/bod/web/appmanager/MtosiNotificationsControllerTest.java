package nl.surfnet.bod.web.appmanager;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.List;

import nl.surfnet.bod.nbi.mtosi.NotificationConsumerHttp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.tmforum.mtop.fmw.xsd.hbt.v1.HeartbeatType;
import org.tmforum.mtop.nra.xsd.alm.v1.AlarmType;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class MtosiNotificationsControllerTest {

  @InjectMocks
  private MtosiNotificationsController subject;

  @Mock
  private NotificationConsumerHttp notificationConsumerHttpMock;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
  }

  @Test
  public void notificationsShouldAddAlamsAndHeartbeatsToModel() throws Exception {
    when(notificationConsumerHttpMock.getAlarms()).thenReturn(Lists.newArrayList(new AlarmType()));
    when(notificationConsumerHttpMock.getHeartbeats()).thenReturn(Lists.newArrayList(new HeartbeatType(), new HeartbeatType()));

    mockMvc.perform(get("/appmanager/mtosi/notifications"))
      .andExpect(status().isOk())
      .andExpect(model().attribute("alarms", hasSize(1)))
      .andExpect(model().attribute("heartbeats", hasSize(2)));
  }

  @Test
  public void indexPageShouldBeOk() throws Exception {
    mockMvc.perform(get("/appmanager/mtosi"))
      .andExpect(status().isOk());
  }

  @Test
  public void lastShouldReturnLastFewElements() {
    List<String> input = Lists.newArrayList("one", "two", "three", "four");

    List<String> output = subject.last(input, 2);

    assertThat(output, contains("three", "four"));
  }

  @Test
  public void lastShouldReturnTheWholeList() {
    List<String> input = Lists.newArrayList("one", "two");

    List<String> output = subject.last(input, 20);

    assertThat(output, contains("one", "two"));
  }
}
