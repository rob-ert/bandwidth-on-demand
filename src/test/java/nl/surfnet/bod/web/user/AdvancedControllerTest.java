package nl.surfnet.bod.web.user;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

public class AdvancedControllerTest {

  private MockMvc mockMvc;

  @Before
  public void setup() {
    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/WEB-INF/views/");
    viewResolver.setSuffix(".jspx");

     mockMvc = standaloneSetup(new AdvancedController()).setViewResolvers(viewResolver).build();
  }

  @Test
  public void teamsShouldAddTeamsToModel() throws Exception {
    Security.setUserDetails(new RichUserDetailsFactory().addUserGroup("first").addUserGroup("second").create());

    mockMvc.perform(get("/teams"))
      .andExpect(model().attribute("teams", hasSize(2)))
      .andExpect(status().isOk());
  }

  @Test
  public void advancedPage() throws Exception {
    mockMvc.perform(get("/advanced"))
      .andExpect(status().isOk())
      .andExpect(view().name("advanced"));
  }
}