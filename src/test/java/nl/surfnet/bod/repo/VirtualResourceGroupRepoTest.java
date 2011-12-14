package nl.surfnet.bod.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/appCtx*.xml")
public class VirtualResourceGroupRepoTest {

  @Autowired
  private VirtualResourceGroupRepo subject;

  @Test
  public void testFindBySurfConnextGroupName() {
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
}
