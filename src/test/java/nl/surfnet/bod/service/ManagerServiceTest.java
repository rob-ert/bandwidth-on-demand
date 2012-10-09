package nl.surfnet.bod.service;

import java.util.Collection;
import java.util.Set;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ManagerServiceTest {

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Mock
  private VirtualResourceGroupService virtualResourceGroupService;

  @Mock
  private VirtualPortService virtualPortService;

  @InjectMocks
  private ManagerService subject;

  @Test
  public void shouldNotFindDuplicateAdminGroupForManager() {
    String adminGroupOne = "adminGroupOne";
    String adminGroupThree = "adminGroupThree";

    PhysicalResourceGroup prgOne = new PhysicalResourceGroupFactory().setId(1L).setAdminGroup(adminGroupOne).create();
    RichUserDetails manager = new RichUserDetailsFactory().addManagerRole(prgOne).create();

    VirtualResourceGroup vrgOne = new VirtualResourceGroupFactory().setSurfconextGroupId(adminGroupOne).create();

    // Link to physicalports with groups one and three
    VirtualPort vpOneVrgOne = new VirtualPortFactory().setVirtualResourceGroup(vrgOne).setPhysicalPortAdminGroup(
        adminGroupOne).create();
    VirtualPort vpTwoVrgOne = new VirtualPortFactory().setVirtualResourceGroup(vrgOne).setPhysicalPortAdminGroup(
        adminGroupThree).create();
    Collection<VirtualPort> vrgOneVirtualPorts = Lists.newArrayList(vpOneVrgOne, vpTwoVrgOne);
    vrgOne.setVirtualPorts(vrgOneVirtualPorts);

    when(virtualResourceGroupService.findEntriesForManager(manager.getSelectedRole())).thenReturn(
        Lists.newArrayList(vrgOne));

    Set<String> groupsForManager = subject.findAllAdminGroupsForManager(manager.getSelectedRole());
    assertThat(groupsForManager, hasSize(1));
    assertThat(groupsForManager, hasItem(adminGroupOne));
  }
  

  @Test
  public void shouldFindAdminGroupsForManager() {
    String adminGroupOne = "adminGroupOne";
    String adminGroupThree = "adminGroupThree";

    PhysicalResourceGroup prgOne = new PhysicalResourceGroupFactory().setId(1L).setAdminGroup(adminGroupOne).create();
    RichUserDetails manager = new RichUserDetailsFactory().addManagerRole(prgOne).create();

    VirtualResourceGroup vrgThree = new VirtualResourceGroupFactory().setSurfconextGroupId(adminGroupThree).create();

    // Link to physicalports with groups one and three
    VirtualPort vpOneVrgOne = new VirtualPortFactory().setVirtualResourceGroup(vrgThree).setPhysicalPortAdminGroup(
        adminGroupOne).create();
    VirtualPort vpTwoVrgOne = new VirtualPortFactory().setVirtualResourceGroup(vrgThree).setPhysicalPortAdminGroup(
        adminGroupThree).create();
    Collection<VirtualPort> vrgOneVirtualPorts = Lists.newArrayList(vpOneVrgOne, vpTwoVrgOne);
    vrgThree.setVirtualPorts(vrgOneVirtualPorts);

    when(virtualResourceGroupService.findEntriesForManager(manager.getSelectedRole())).thenReturn(
        Lists.newArrayList(vrgThree));

    Set<String> groupsForManager = subject.findAllAdminGroupsForManager(manager.getSelectedRole());
    assertThat(groupsForManager, hasSize(2));
    assertThat(groupsForManager, hasItems(adminGroupOne, adminGroupThree));
  }

}
