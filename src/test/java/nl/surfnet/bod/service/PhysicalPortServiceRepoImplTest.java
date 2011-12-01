package nl.surfnet.bod.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import junit.framework.Assert;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.support.PhysicalPortDataOnDemand;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/appCtx*.xml")
@Transactional
public class PhysicalPortServiceRepoImplTest {

  @Autowired
  private PhysicalPortServiceRepoImpl physicalPortServiceRepoImpl;

  @Autowired
  private PhysicalPortDataOnDemand physicalPortDataOnDemand;

  @Before
  public void setUp() {
    physicalPortDataOnDemand.init();
  }

  @Test
  public void testFindAll() {

    // Execute
    List<PhysicalPort> ports = physicalPortServiceRepoImpl.findAll();

    // Verify
    assertEquals(10, ports.size());
  }

  @Test
  public void testFindEntriesById() {

    PhysicalPort port = physicalPortDataOnDemand.getRandomPhysicalPort();

    PhysicalPort foundPort = physicalPortServiceRepoImpl.find(port.getId());

    assertEquals(port.getId(), foundPort.getId());
    assertEquals(port.getName(), foundPort.getName());

  }

  @Test
  public void testFindEntriesByName() {

    PhysicalPort port = physicalPortDataOnDemand.getRandomPhysicalPort();

    PhysicalPort foundPort = physicalPortServiceRepoImpl.findByName(port.getName());

    assertEquals(port.getId(), foundPort.getId());
    assertEquals(port.getName(), foundPort.getName());

  }

  @Test
  public void testCount() {
    assertEquals(10, physicalPortServiceRepoImpl.count());
  }

  @Test
  public void testDelete() {
    PhysicalPort port = physicalPortDataOnDemand.getRandomPhysicalPort();

    physicalPortServiceRepoImpl.delete(port);

    assertEquals(9, physicalPortServiceRepoImpl.count());

  }

  @Test
  public void testSave() {
    PhysicalPort port = physicalPortDataOnDemand.getNewTransientPhysicalPort(99);

    physicalPortServiceRepoImpl.save(port);

    assertEquals(11, physicalPortServiceRepoImpl.count());
  }

  @Test
  public void testUpdate() {
    PhysicalPort port = physicalPortDataOnDemand.getNewTransientPhysicalPort(99);

    port.setName("JustTesting");
    PhysicalPort updatedPort = physicalPortServiceRepoImpl.update(port);

    assertEquals(port.getId(), updatedPort.getId());
    assertEquals("JustTesting", updatedPort.getName());
    
    assertEquals(11, physicalPortServiceRepoImpl.count());
  }

  @Test
  public void testUpdateExisting() {
    PhysicalPort port = physicalPortDataOnDemand.getRandomPhysicalPort();

    port.setName("JustTesting");
    PhysicalPort updatedPort = physicalPortServiceRepoImpl.update(port);

    assertEquals(port.getId(), updatedPort.getId());
    assertEquals("JustTesting", updatedPort.getName());
  }

}
