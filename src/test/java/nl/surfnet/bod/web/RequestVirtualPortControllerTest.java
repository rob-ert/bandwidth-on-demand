package nl.surfnet.bod.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class RequestVirtualPortControllerTest {

  @InjectMocks
  private RequestVirtualPortController subject;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;

  @Before
  public void login() {
    Security.setUserDetails(new RichUserDetailsFactory().create());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void findAllGroups() {
    ModelStub model = new ModelStub();

    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().create();

    when(physicalResourceGroupServiceMock.findAllWithPorts()).thenReturn(Lists.newArrayList(group));

    subject.request(model);

    assertThat(model.asMap(), hasKey("physicalResourceGroups"));
    assertThat(((Collection<PhysicalResourceGroup>) model.asMap().get("physicalResourceGroups")), contains(group));
  }

  @Test
  public void shouldNotCreateFormIfPhysicalResourceGroupDoesNotExist() {
    ModelStub model = new ModelStub();

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(null);

    String page = subject.requestForm(1L, model, model);

    assertThat(page, is("redirect:/virtualports/request"));
  }

  @Test
  public void shouldNotCreateFormIfPhysicalResourceGroupIsNotActive() {
    ModelStub model = new ModelStub();

    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setActive(false).create();

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    String page = subject.requestForm(1L, model, model);

    assertThat(page, is("redirect:/virtualports/request"));
  }

  @Test
  public void shouldCreateFormIfPhysicalResourceGroupIsActive() {
    ModelStub model = new ModelStub();

    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setActive(true).create();

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    String page = subject.requestForm(1L, model, model);

    assertThat(page, is("virtualports/requestform"));

    assertThat(model.asMap(), hasKey("requestCommand"));
    assertThat(model.asMap(), hasKey("user"));
    assertThat(model.asMap(), hasKey("physicalResourceGroup"));
  }

}
