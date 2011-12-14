package nl.surfnet.bod.repo;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/appCtx*.xml")
public class PhysicalResourceGroupRepoTest {

  @Autowired
  private PhysicalResourceGroupRepo subject;

  @Test
  public void testFindByName() {
    String name = "tester";

    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().setName(name).create();
    Collection<PhysicalResourceGroup> physicalResourceGroups = Lists.newArrayList(physicalResourceGroup,
        new PhysicalResourceGroupFactory().setName("notToBeFound").create());

    subject.save(physicalResourceGroups);

    PhysicalResourceGroup foundPhysicalResourceGroup = subject.findByName(name);

    assertThat(foundPhysicalResourceGroup.getName(), is(name));

  }

  @Test
  public void testFindByAdminGroups() {
    String firstAdminGroup = "urn:firstGroup";
    Collection<String> adminGroups = Lists.newArrayList(firstAdminGroup, "urn:secondGroup");
    PhysicalResourceGroup firstPhysicalResourceGroup = new PhysicalResourceGroupFactory().setName("testName")
        .setAdminGroupName(firstAdminGroup).create();

    Collection<PhysicalResourceGroup> physicalResourceGroups = Lists.newArrayList(firstPhysicalResourceGroup,
        new PhysicalResourceGroupFactory().setAdminGroupName("urn:noMatch").create());
    subject.save(physicalResourceGroups);

    Collection<PhysicalResourceGroup> foundAdminGroups = subject.findByAdminGroupIn(adminGroups);

    assertThat(foundAdminGroups, hasSize(1));
    assertThat(foundAdminGroups.iterator().next().getAdminGroup(), is(firstAdminGroup));
  }

}
