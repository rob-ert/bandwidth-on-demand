package nl.surfnet.bod.web.noc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.Collection;

import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;

import com.google.common.collect.ImmutableList;


@RunWith(MockitoJUnitRunner.class)
public class VirtualResourceGroupControllerTest {

  @InjectMocks
  private VirtualResourceGroupController subject;

  @Mock
  private VirtualResourceGroupService virtualResourceGroupServiceMock;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
  }

  @Test
  public void shouldDisplayListOfTeams() throws Exception {
    when(virtualResourceGroupServiceMock.findEntries(eq(0), anyInt(), any(Sort.class)))
      .thenReturn(ImmutableList.of(new VirtualResourceGroupFactory().create()));

    mockMvc.perform(get("/noc/teams"))
      .andExpect(status().isOk())
      .andExpect(model().<Collection<?>>attribute("list", hasSize(1)))
      .andExpect(view().name("noc/teams/list"));
  }
}
