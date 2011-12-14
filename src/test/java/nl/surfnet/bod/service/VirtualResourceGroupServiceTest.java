package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.support.UserGroupFactory;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class VirtualResourceGroupServiceTest {

  private VirtualResourceGroupService virtualResourceGroupService;

  private GroupService groupServiceMock;
  private VirtualResourceGroupRepo groupRepoMock;

  @Before
  public void init() {
    groupRepoMock = mock(VirtualResourceGroupRepo.class);
    groupServiceMock = mock(GroupService.class);

    virtualResourceGroupService = new VirtualResourceGroupService();
    virtualResourceGroupService.setGroupService(groupServiceMock);
    virtualResourceGroupService.setVirtualResourceGroupRepo(groupRepoMock);
  }

  @Test
  public void test() {
    String loggedInUser = "urn:truus";
    String groupOfLoggedInUser = "urn:myfirstgroup";
    UserGroup group = new UserGroupFactory().setId(groupOfLoggedInUser).create();
    VirtualResourceGroup vGroup = new VirtualResourceGroupFactory().create();

    when(groupServiceMock.getGroups(loggedInUser)).thenReturn(ImmutableList.of(group));
    when(groupRepoMock.findBySurfConnextGroupNameIn(Lists.newArrayList(groupOfLoggedInUser))).thenReturn(
        ImmutableList.of(vGroup));

    Collection<VirtualResourceGroup> groups = virtualResourceGroupService.findAllForUser(loggedInUser);

    assertThat(groups, hasSize(1));
    assertThat(groups, hasItem(vGroup));
  }
}
