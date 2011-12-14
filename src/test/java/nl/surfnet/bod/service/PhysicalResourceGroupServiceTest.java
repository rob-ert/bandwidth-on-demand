package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.UserGroupFactory;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class PhysicalResourceGroupServiceTest {

  private PhysicalResourceGroupService physicalResourceGroupService;

  private GroupService groupServiceMock;
  private PhysicalResourceGroupRepo groupRepoMock;

  @Before
  public void init() {
    groupRepoMock = mock(PhysicalResourceGroupRepo.class);
    groupServiceMock = mock(GroupService.class);

    physicalResourceGroupService = new PhysicalResourceGroupService();
    physicalResourceGroupService.setGroupService(groupServiceMock);
    physicalResourceGroupService.setPhysicalResourceGroupRepo(groupRepoMock);
  }

  @Test
  public void test() {
    String loggedInUser = "urn:truus";
    String groupOfLoggedInUser = "urn:myfirstgroup";
    UserGroup group = new UserGroupFactory().setId(groupOfLoggedInUser).create();
    PhysicalResourceGroup prGroup = new PhysicalResourceGroupFactory().create();

    when(groupServiceMock.getGroups(loggedInUser)).thenReturn(ImmutableList.of(group));
    when(groupRepoMock.findByAdminGroupIn(Lists.newArrayList(groupOfLoggedInUser))).thenReturn(
        ImmutableList.of(prGroup));

    Collection<PhysicalResourceGroup> groups = physicalResourceGroupService.findAllForUser(loggedInUser);

    assertThat(groups, hasSize(1));
    assertThat(groups, hasItem(prGroup));
  }
}
