package nl.surfnet.bod.repo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/appCtx*.xml")
public class PhysicalPortRepoTest {

  @Autowired
  private PhysicalPortRepo subject;

  @Autowired
  private PhysicalResourceGroupRepo physicalResourceGroupRepo;

  @Test
  public void testFindAllByPhysicalResourceGroup() {

    String nameOne = "groupOne";
    PhysicalResourceGroup physicalResourceGroupOne = new PhysicalResourceGroupFactory().setName(nameOne).create();
    PhysicalResourceGroup physicalResourceGroupTwo = new PhysicalResourceGroupFactory().create();
    physicalResourceGroupRepo.save(Lists.newArrayList(physicalResourceGroupOne, physicalResourceGroupTwo));

    PhysicalPort pPortOne = new PhysicalPortFactory().setPhysicalResourceGroup(physicalResourceGroupOne).create();
    PhysicalPort pPortTwo = new PhysicalPortFactory().setPhysicalResourceGroup(physicalResourceGroupTwo).create();

    subject.save(Lists.newArrayList(pPortOne, pPortTwo));

    List<PhysicalPort> foundPhysicalPorts = subject.findByPhysicalResourceGroup(physicalResourceGroupOne);

    assertThat(foundPhysicalPorts, hasSize(1));
    assertThat(nameOne, is(foundPhysicalPorts.iterator().next().getPhysicalResourceGroup().getName()));
  }

  @Test
  public void testFindAllByPhysicalResourceGroupNull() {

    List<PhysicalPort> foundPhysicalPorts = subject.findByPhysicalResourceGroup(null);

    assertThat(foundPhysicalPorts, hasSize(0));
  }

}
