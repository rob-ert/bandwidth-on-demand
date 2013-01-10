package nl.surfnet.bod.web.noc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.Collection;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;

import com.google.common.collect.ImmutableList;


@RunWith(MockitoJUnitRunner.class)
public class VirtualPortControllerTest {

  @InjectMocks
  private VirtualPortController subject;

  @Mock
  private VirtualPortService virtualPortServiceMock;

  @Mock
  private MessageSource messageSource;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
  }

  @Test
  public void deleteAVirtualPort() throws Exception {
    RichUserDetails nocUser = new RichUserDetailsFactory().addNocRole().create();
    Security.setUserDetails(nocUser);

    VirtualPort virtualPort = new VirtualPortFactory().setId(2L).create();

    when(virtualPortServiceMock.find(2L)).thenReturn(virtualPort);

    mockMvc.perform(delete("/noc/virtualports/delete").param("id", "2"))
      .andExpect(status().isMovedTemporarily())
      .andExpect(flash().<Collection<?>>attribute("infoMessages", hasSize(1)))
      .andExpect(view().name("redirect:/noc/virtualports"));

    verify(virtualPortServiceMock).delete(virtualPort, nocUser);
  }

  @Test
  public void deleteNonExistingVirtualPort() throws Exception {
    RichUserDetails nocUser = new RichUserDetailsFactory().addNocRole().create();
    Security.setUserDetails(nocUser);

    when(virtualPortServiceMock.find(2L)).thenReturn(null);

    mockMvc.perform(delete("/noc/virtualports/delete").param("id", "2"))
      .andExpect(status().isMovedTemporarily())
      .andExpect(flash().attribute("infoMessages", nullValue()))
      .andExpect(view().name("redirect:/noc/virtualports"));

    verify(virtualPortServiceMock, never()).delete(any(VirtualPort.class), eq(nocUser));
  }

  @Test
  public void deleteWithoutNocRoleSelected() throws Exception {
    RichUserDetails nocUser = new RichUserDetailsFactory().addNocRole().addManagerRole().create();
    Security.setUserDetails(nocUser);
    Security.switchToManager();

    VirtualPort virtualPort = new VirtualPortFactory().setId(2L).create();

    when(virtualPortServiceMock.find(2L)).thenReturn(virtualPort);

    mockMvc.perform(delete("/noc/virtualports/delete").param("id", "2"))
      .andExpect(status().isMovedTemporarily())
      .andExpect(flash().attribute("infoMessages", nullValue()))
      .andExpect(view().name("redirect:/noc/virtualports"));

    verify(virtualPortServiceMock, never()).delete(any(VirtualPort.class), any(RichUserDetails.class));
  }

  @Test
  public void listVirtualPorts() throws Exception {
    when(virtualPortServiceMock.findEntries(eq(0), anyInt(), any(Sort.class)))
      .thenReturn(ImmutableList.of(new VirtualPortFactory().create(), new VirtualPortFactory().create()));

    mockMvc.perform(get("/noc/virtualports"))
      .andExpect(status().isOk())
      .andExpect(model().<Collection<?>>attribute("list", hasSize(2)))
      .andExpect(model().attribute("maxPages", is(1)))
      .andExpect(view().name("/noc/virtualports/list"));
  }
}
