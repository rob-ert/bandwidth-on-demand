package nl.surfnet.bod.repo;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.support.VirtualPortFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/appCtx*.xml")
public class VirtualPortRepoTest {

  @Autowired
  VirtualPortRepo subject;

  @Test
  public void testSave() {
    String name = "vpOne";
    VirtualPort virtualPortOne = new VirtualPortFactory().setName(name).setPhysicalPort(null)
        .setVirtualResourceGroup(null).create();

    subject.save(virtualPortOne);
  }

  @Test
  public void testFindByName() {
    String name = "tester";

    VirtualPort virtualPort = new VirtualPortFactory().setName(name).setVirtualResourceGroup(null)
        .setPhysicalPort(null).create();
    Collection<VirtualPort> virtualPorts = Lists.newArrayList(virtualPort,
        new VirtualPortFactory().setName("notToBeFound").setVirtualResourceGroup(null).setPhysicalPort(null).create());

    subject.save(virtualPorts);

    VirtualPort foundPhysicalResourceGroup = subject.findByName(name);

    assertThat(foundPhysicalResourceGroup.getName(), is(name));

  }

}
