package nl.surfnet.bod.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PhyscialResourceGroupControllerTest {

  @InjectMocks
  private PhysicalResourceGroupController subject;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;

  @Test
  public void whenPortDoesNotExistPortAreEmpty() {
    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(null);

    Collection<PhysicalPort> ports = subject.listForPhysicalResourceGroup(1L);

    assertThat(ports, hasSize(0));
  }

  @Test
  public void whenUserIsNotMemberOfAdminGroupPortsAreEmpty() {
    RichUserDetails user = new RichUserDetailsFactory().create();
    Security.setUserDetails(user);
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory()
        .addPhysicalPort(new PhysicalPortFactory().create()).create();

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    Collection<PhysicalPort> ports = subject.listForPhysicalResourceGroup(1L);

    assertThat(ports, hasSize(0));
  }

  @Test
  public void whenUserIsMemberOfAdminGroupReturnPorts() {
    RichUserDetails user = new RichUserDetailsFactory().addUserGroup("urn:group").create();
    Security.setUserDetails(user);

    PhysicalPort onlyPort = new PhysicalPortFactory().create();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setAdminGroupName("urn:group")
        .addPhysicalPort(onlyPort).create();

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    Collection<PhysicalPort> ports = subject.listForPhysicalResourceGroup(1L);

    assertThat(ports, contains(onlyPort));
  }

}
