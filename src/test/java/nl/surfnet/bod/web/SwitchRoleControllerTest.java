package nl.surfnet.bod.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.BodRoleFactory;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.manager.ActivationEmailController;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RunWith(MockitoJUnitRunner.class)
public class SwitchRoleControllerTest {

  @Mock
  private Environment environmentMock;

  @Mock
  private HttpServletRequest mockRequest;

  @Mock
  private HttpSession mockSession;
  
  @Mock
  private MessageSource messageSourceMock;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupService;

  @InjectMocks
  private SwitchRoleController subject = new SwitchRoleController();

  private RichUserDetails user;

  @Before
  public void setUp() {
    user = new RichUserDetailsFactory().create();
    Security.setUserDetails(user);
  }

  @Test
  public void testSwitchRoleWithActivePrg() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(101L).setActive(true).create();
    BodRole role = new BodRoleFactory().setPhysicalResourceGroup(group).create();
    user.setSelectedRole(role);

    when(physicalResourceGroupService.find(group.getId())).thenReturn(group);

    Model uiModel = new ModelStub();
    Model redirectAttribs = new ModelStub();
    String view = subject.switchRole(String.valueOf(role.getId()), uiModel, (RedirectAttributes) redirectAttribs);

     assertThat(view, is(role.getRole().getViewName()));     
  }
  
  @Test
  public void testSwitchRoleWithNotActivePrg() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(101L).create();
    BodRole role = new BodRoleFactory().setPhysicalResourceGroup(group).create();
    user.setSelectedRole(role);

    when(physicalResourceGroupService.find(group.getId())).thenReturn(group);

    Model uiModel = new ModelStub();
    Model redirectAttribs = new ModelStub();
    String view = subject.switchRole(String.valueOf(role.getId()), uiModel, (RedirectAttributes) redirectAttribs);

     assertThat(view, is("redirect:manager/physicalresourcegroups/edit?id="+ group.getId()));     
  }

  @Test
  public void testLogout() {
    when(mockRequest.getSession()).thenReturn(mockSession);
    when(environmentMock.getShibbolethLogoutUrl()).thenReturn("shibUrl");

    String view = subject.logout(mockRequest);
    assertThat(view, is("redirect:" + environmentMock.getShibbolethLogoutUrl()));
  }

  @Test
  public void shouldCreateNewLinkForm() {
    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();

    String linkForm = subject.createNewActivationLinkForm(new Object[] {
        environmentMock.getExternalBodUrl() + ActivationEmailController.ACTIVATION_MANAGER_PATH,
        physicalResourceGroup.getId().toString(), "Yes new email was sent" });

    assertThat(linkForm, containsString(physicalResourceGroup.getId().toString()));
    assertThat(linkForm, containsString(ActivationEmailController.ACTIVATION_MANAGER_PATH));
  }

}
