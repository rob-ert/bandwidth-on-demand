package nl.surfnet.bod.web.manager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import nl.surfnet.bod.service.InstituteIddService;
import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalPortControllerTest {

  @InjectMocks
  private PhysicalPortController subject;

  @SuppressWarnings("unused")
  @Mock
  private PhysicalPortService physicalPortServiceMock;

  @Mock
  private InstituteService instituteService;

  @Before
  public void setAuthenticatedUser() {
    Security.setUserDetails(new RichUserDetailsFactory().create());
  }

  @Test
  public void listPorts() {
    ModelStub model = new ModelStub();

    subject.list(null, model);

    assertThat(model.asMap(), hasKey("physicalPorts"));
    assertThat(model.asMap(), hasKey("maxPages"));
  }

}
