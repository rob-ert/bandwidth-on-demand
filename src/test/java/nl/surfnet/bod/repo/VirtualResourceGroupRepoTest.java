package nl.surfnet.bod.repo;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collection;

import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/appCtx*.xml")
public class VirtualResourceGroupRepoTest {

  @Autowired
  private VirtualResourceGroupRepo subject;

  @Test
  public void testSave() {
    String nameOne = "groupOne";
    VirtualResourceGroup vrGroup = new VirtualResourceGroupFactory().setName("one").setSurfConnextGroupName(nameOne).create();

    subject.save(vrGroup);
  }

  @Test(expected = JpaSystemException.class)
  public void testSaveNameNotUnique() {
    String nameOne = "groupOne";
    VirtualResourceGroup vrGroupOne = new VirtualResourceGroupFactory().setName("one").setSurfConnextGroupName(nameOne).create();

    subject.save(vrGroupOne);

    VirtualResourceGroup vrGroupTwo = new VirtualResourceGroupFactory().setSurfConnextGroupName(nameOne).create();

    try {
      subject.save(vrGroupTwo);

      fail("ConstraintViolation excpected");
    }
    catch (JpaSystemException exc) {
      assertEquals(exc.getCause().getClass(), ConstraintViolationException.class);
    }
  }
  
  @Test
  public void testfindBySurfConnextGroupName() {
    String firstAdminGroup = "urn:firstGroup";
    Collection<String> adminGroups = Lists.newArrayList(firstAdminGroup, "urn:secondGroup");
    VirtualResourceGroup firstVirtualResourceGroup =new VirtualResourceGroupFactory().setName("testName").setSurfConnextGroupName(firstAdminGroup).create(); 
    
    Collection<VirtualResourceGroup> virtualResourceGroups = Lists.newArrayList(firstVirtualResourceGroup, new VirtualResourceGroupFactory().setName("noMatcher").setSurfConnextGroupName("urn:noMatch").create());    
    subject.save(virtualResourceGroups);
    
    Collection<VirtualResourceGroup> foundAdminGroups = subject.findBySurfConnextGroupNameIn(adminGroups);
    
    assertThat(foundAdminGroups, hasSize(1));
    assertThat(foundAdminGroups.iterator().next().getSurfConnextGroupName(), is(firstAdminGroup));
  }
  
  
  
}
