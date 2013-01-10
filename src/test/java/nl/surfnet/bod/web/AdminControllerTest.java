package nl.surfnet.bod.web;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import nl.surfnet.bod.service.TextSearchIndexer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;


@RunWith(MockitoJUnitRunner.class)
public class AdminControllerTest {

  @InjectMocks
  private AdminController subject;

  @Mock
  private ReloadableResourceBundleMessageSource messageSourceMock;

  @Mock
  private TextSearchIndexer textSearchIndexerMock;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
  }

  @Test
  public void refreshMessagesShouldClearMessageSourceCache() throws Exception {
    mockMvc.perform(get("/admin/refreshMessages").header("Referer", "/noc/teams"))
     .andExpect(status().isMovedTemporarily())
     .andExpect(view().name("redirect:/noc/teams"));

    verify(messageSourceMock).clearCache();
  }

  @Test
  public void reindexData() throws Exception {
    mockMvc.perform(get("/admin/index"))
     .andExpect(status().isMovedTemporarily())
     .andExpect(view().name("redirect:/"));

    verify(textSearchIndexerMock).indexDatabaseContent();
  }

  @Test(expected = NestedServletException.class)
  public void error() throws Exception {
    mockMvc.perform(get("/admin/error"));
  }
}
