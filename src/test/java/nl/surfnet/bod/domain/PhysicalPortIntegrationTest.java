package nl.surfnet.bod.domain;

import java.util.List;

import nl.surfnet.bod.repo.PhysicalPortRepo;
import nl.surfnet.bod.service.PhysicalPortServiceImpl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/applicationContext*.xml")
@Transactional
public class PhysicalPortIntegrationTest {

    @Autowired
    private PhysicalPortDataOnDemand dod;

    @Autowired
    private PhysicalPortServiceImpl physicalPortService;

    @Autowired
    private PhysicalPortRepo physicalPortRepo;

    @Test
    public void testCountAllPhysicalPorts() {
        Assert.assertNotNull("Data on demand for 'PhysicalPort' failed to initialize correctly",
                dod.getRandomPhysicalPort());
        long count = physicalPortService.countAllPhysicalPorts();
        Assert.assertTrue("Counter for 'PhysicalPort' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFindPhysicalPort() {
        PhysicalPort obj = dod.getRandomPhysicalPort();
        Assert.assertNotNull("Data on demand for 'PhysicalPort' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PhysicalPort' failed to provide an identifier", id);
        obj = physicalPortService.findPhysicalPort(id);
        Assert.assertNotNull("Find method for 'PhysicalPort' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'PhysicalPort' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testFindPhysicalPortEntries() {
        Assert.assertNotNull("Data on demand for 'PhysicalPort' failed to initialize correctly",
                dod.getRandomPhysicalPort());
        long count = physicalPortService.countAllPhysicalPorts();
        if (count > 20)
            count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<PhysicalPort> result = physicalPortService.findPhysicalPortEntries(firstResult, maxResults);
        Assert.assertNotNull("Find entries method for 'PhysicalPort' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'PhysicalPort' returned an incorrect number of entries", count,
                result.size());
    }

    @Test
    public void testFlush() {
        PhysicalPort obj = dod.getRandomPhysicalPort();
        Assert.assertNotNull("Data on demand for 'PhysicalPort' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PhysicalPort' failed to provide an identifier", id);
        obj = physicalPortService.findPhysicalPort(id);
        Assert.assertNotNull("Find method for 'PhysicalPort' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifyPhysicalPort(obj);
        Integer currentVersion = obj.getVersion();
        physicalPortRepo.flush();
        Assert.assertTrue("Version for 'PhysicalPort' failed to increment on flush directive",
                (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testUpdatePhysicalPortUpdate() {
        PhysicalPort obj = dod.getRandomPhysicalPort();
        Assert.assertNotNull("Data on demand for 'PhysicalPort' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PhysicalPort' failed to provide an identifier", id);
        obj = physicalPortService.findPhysicalPort(id);
        boolean modified = dod.modifyPhysicalPort(obj);
        Integer currentVersion = obj.getVersion();
        PhysicalPort merged = physicalPortService.updatePhysicalPort(obj);
        physicalPortRepo.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object",
                merged.getId(), id);
        Assert.assertTrue("Version for 'PhysicalPort' failed to increment on merge and flush directive",
                (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSavePhysicalPort() {
        Assert.assertNotNull("Data on demand for 'PhysicalPort' failed to initialize correctly",
                dod.getRandomPhysicalPort());
        PhysicalPort obj = dod.getNewTransientPhysicalPort(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'PhysicalPort' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'PhysicalPort' identifier to be null", obj.getId());
        physicalPortService.savePhysicalPort(obj);
        physicalPortRepo.flush();
        Assert.assertNotNull("Expected 'PhysicalPort' identifier to no longer be null", obj.getId());
    }

    @Test
    public void testDeletePhysicalPort() {
        PhysicalPort obj = dod.getRandomPhysicalPort();
        Assert.assertNotNull("Data on demand for 'PhysicalPort' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PhysicalPort' failed to provide an identifier", id);
        obj = physicalPortService.findPhysicalPort(id);
        physicalPortService.deletePhysicalPort(obj);
        physicalPortRepo.flush();
        Assert.assertNull("Failed to remove 'PhysicalPort' with identifier '" + id + "'",
                physicalPortService.findPhysicalPort(id));
    }
}
