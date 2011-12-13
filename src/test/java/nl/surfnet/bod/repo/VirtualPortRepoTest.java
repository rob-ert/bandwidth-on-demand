package nl.surfnet.bod.repo;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.support.VirtualPortFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/appCtx*.xml")
public class VirtualPortRepoTest {

  @Autowired
  VirtualPortRepo subject;

  @Test
  public void testSave() {
    String name = "vpOne";
    VirtualPort virtualPortOne = new VirtualPortFactory().setName(name).setPhysicalPort(null).setVirtualResourceGroup(null).create();
        
    subject.save(virtualPortOne);
  }  
  
  
}
