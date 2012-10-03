package nl.surfnet.bod.service;

import java.util.List;
import java.util.Set;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ManagerServiceTest {

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Mock
  private VirtualResourceGroupService virtualResourceGroupService;

  @InjectMocks
  private ManagerService subject;

  @Test
  public void testFindAllAdminGroupsForManager() {
    String adminGroupOne = "adminGroupOne";
    String adminGroupTwo = "adminGroupTwo";
    String adminGroupThree = "adminGroupThree";
    
    PhysicalResourceGroup prgOne = new PhysicalResourceGroupFactory().setId(1L).setAdminGroup(adminGroupOne).create();
    RichUserDetails manager = new RichUserDetailsFactory().addManagerRole(prgOne).create();

    VirtualResourceGroup vrgOne = new VirtualResourceGroupFactory().setSurfconextGroupId(adminGroupTwo).create();
    VirtualResourceGroup vrgTwo = new VirtualResourceGroupFactory().setSurfconextGroupId(adminGroupThree).create();
    List<VirtualResourceGroup> vrgs = Lists.newArrayList(vrgOne, vrgTwo);

    when(physicalResourceGroupService.find(1L)).thenReturn(prgOne);
    when(virtualResourceGroupService.findEntriesForManager(manager.getSelectedRole())).thenReturn(vrgs);

    Set<String> groupsForManager = subject.findAllAdminGroupsForManager(manager.getSelectedRole());
    assertThat(groupsForManager, hasSize(3));
    assertThat(groupsForManager, containsInAnyOrder(adminGroupOne, adminGroupTwo, adminGroupThree));
  }
}
