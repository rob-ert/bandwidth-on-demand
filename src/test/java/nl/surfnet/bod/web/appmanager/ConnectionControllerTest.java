package nl.surfnet.bod.web.appmanager;

import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import nl.surfnet.bod.service.ConnectionService;
import nl.surfnet.bod.support.ConnectionFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.web.servlet.MockMvc;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionControllerTest {

  @InjectMocks
  private ConnectionController subject;

  @Mock
  private ConnectionService connectionServiceMock;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
  }

  @Test
  public void listConnectionsShouldAddConnectionToModel() throws Exception {

    when(connectionServiceMock.findEntries(0, MAX_ITEMS_PER_PAGE, new Sort(Direction.ASC, "startTime")))
      .thenReturn(Lists.newArrayList(new ConnectionFactory().create(), new ConnectionFactory().create()));

    mockMvc.perform(get("/appmanager/connections"))
      .andExpect(model().attribute("list", hasSize(2)))
      .andExpect(status().isOk());
  }

  @Test
  public void listIllegalConnectionsShouldAddConnectionToModel() throws Exception {

    when(connectionServiceMock.findWithIllegalState())
      .thenReturn(Lists.newArrayList(new ConnectionFactory().create(), new ConnectionFactory().create()));

    mockMvc.perform(get("/appmanager/connections/illegal"))
      .andExpect(model().attribute("list", hasSize(2)))
      .andExpect(status().isOk());
  }
}
