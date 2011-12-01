package nl.surfnet.bod.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

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
public class PhysicalPortServiceImplTest {

  @Autowired
  private PhysicalPortServiceImpl physicalPortServiceImpl;

  @Autowired
  private PhysicalPortDataOnDemand physicalPortDataOnDemand;

  @Before
  public void setUp() {
    physicalPortDataOnDemand.init();
  }

  @Test
  public void testFindAll() {

    // Execute
    List<PhysicalPort> ports = physicalPortServiceImpl.findAll();

    // Verify
    assertEquals(260, ports.size());
  }

  @Test
  public void testFindEntriesById() {

    PhysicalPort port = physicalPortDataOnDemand.getRandomPhysicalPort();

    PhysicalPort foundPort = physicalPortServiceImpl.find(port.getId());

    assertEquals(port.getId(), foundPort.getId());
    assertEquals(port.getName(), foundPort.getName());

  }

  @Test
  public void testFindEntriesByName() {

    PhysicalPort port = physicalPortServiceImpl.findAll().get(0);

    PhysicalPort foundPort = physicalPortServiceImpl.findByName(port.getName());

    assertNull(foundPort.getId());
    assertEquals(port.getName(), foundPort.getName());

  }

  @Test
  public void testCount() {
    long count = physicalPortServiceImpl.findAll().size();

    assertEquals(count, physicalPortServiceImpl.count());
  }

  /**
   * Even after a delete, which only deletes the port (e.g. unlink from the
   * PhysicalResourceGroup) in our repository, the amount of ports coming back
   * from the NbiClient should still be the same.
   */
  @Test
  public void testDelete() {
    PhysicalPort port = physicalPortServiceImpl.findAll().get(0);
    
    PhysicalPort foundPort = physicalPortServiceImpl.findByName(port.getName());
    long count = physicalPortServiceImpl.count();

    physicalPortServiceImpl.delete(foundPort);

    assertEquals(count, physicalPortServiceImpl.count());

  }

  /**
   * Even after a save, which only saves the port (e.g. link to a
   * PhysicalResourceGroup) in our repository, the amount of ports coming back
   * from the NbiClient should still be the same
   */
  @Test
  public void testSave() {
    PhysicalPort port = physicalPortDataOnDemand.getNewTransientPhysicalPort(99);
    long count = physicalPortServiceImpl.count();

    physicalPortServiceImpl.save(port);

    assertEquals(count, physicalPortServiceImpl.count());
  }

  @Test
  public void testUpdate() {
    PhysicalPort port = physicalPortDataOnDemand.getNewTransientPhysicalPort(99);
    long count = physicalPortServiceImpl.count();

    port.setName("JustTesting");
    PhysicalPort updatedPort = physicalPortServiceImpl.update(port);

    assertEquals(port.getId(), updatedPort.getId());
    assertEquals("JustTesting", updatedPort.getName());

    assertEquals(count, physicalPortServiceImpl.count());
  }

  @Test
  public void testUpdateExisting() {
    PhysicalPort port = physicalPortDataOnDemand.getRandomPhysicalPort();

    port.setName("JustTesting");
    PhysicalPort updatedPort = physicalPortServiceImpl.update(port);

    assertEquals(port.getId(), updatedPort.getId());
    assertEquals("JustTesting", updatedPort.getName());
  }

}
