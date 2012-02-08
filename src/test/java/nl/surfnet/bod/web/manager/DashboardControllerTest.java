package nl.surfnet.bod.web.manager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class DashboardControllerTest {

  @InjectMocks
  private DashboardController subject;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;

  @Test
  public void managerWhithInactivePhysicalResourceGroupsShouldGetRedirected() {
    RichUserDetails user = new RichUserDetailsFactory().create();
    Security.setUserDetails(user);
    RedirectAttributes model = new ModelStub();
    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().setId(101L).setActive(false)
        .create();

    when(physicalResourceGroupServiceMock.findAllForManager(user))
        .thenReturn(Lists.newArrayList(physicalResourceGroup));

    String page = subject.index(model);

    assertThat(page, startsWith("redirect:"));
    assertThat(page, endsWith("id=101"));
    assertThat(model.getFlashAttributes(), hasKey("infoMessages"));
  }

  @Test
  public void managerWhithActivePhysicalResourceGroupShouldGoToIndex() {
    RichUserDetails user = new RichUserDetailsFactory().create();
    Security.setUserDetails(user);
    RedirectAttributes model = new ModelStub();
    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().setActive(true).create();

    when(physicalResourceGroupServiceMock.findAllForManager(user))
        .thenReturn(Lists.newArrayList(physicalResourceGroup));

    String page = subject.index(model);

    assertThat(page, is("index"));
  }

}
