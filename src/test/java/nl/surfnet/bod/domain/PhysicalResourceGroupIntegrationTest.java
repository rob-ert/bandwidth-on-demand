package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.util.List;

import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import nl.surfnet.bod.service.PhysicalResourceGroupServiceImpl;

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
public class PhysicalResourceGroupIntegrationTest {

    @Autowired
    private PhysicalResourceGroupDataOnDemand dod;

    @Autowired
    private PhysicalResourceGroupServiceImpl physicalResourceGroupService;

    @Autowired
    private PhysicalResourceGroupRepo physicalResourceGroupRepo;

    @Test
    public void testCountAllPhysicalResourceGroups() {
        Assert.assertNotNull("Data on demand for 'PhysicalResourceGroup' failed to initialize correctly",
                dod.getRandomPhysicalResourceGroup());

        long count = physicalResourceGroupService.countAllPhysicalResourceGroups();

        assertThat(count, greaterThan(0L));
    }

    @Test
    public void testFindPhysicalResourceGroup() {
        PhysicalResourceGroup obj = dod.getRandomPhysicalResourceGroup();
        Assert.assertNotNull("Data on demand for 'PhysicalResourceGroup' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PhysicalResourceGroup' failed to provide an identifier", id);
        obj = physicalResourceGroupService.findPhysicalResourceGroup(id);
        Assert.assertNotNull("Find method for 'PhysicalResourceGroup' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'PhysicalResourceGroup' returned the incorrect identifier", id,
                obj.getId());
    }

    @Test
    public void testFindAllPhysicalResourceGroups() {
        Assert.assertNotNull("Data on demand for 'PhysicalResourceGroup' failed to initialize correctly",
                dod.getRandomPhysicalResourceGroup());
        long count = physicalResourceGroupService.countAllPhysicalResourceGroups();
        Assert.assertTrue(
                "Too expensive to perform a find all test for 'PhysicalResourceGroup', as there are "
                        + count
                        + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test",
                count < 250);
        List<PhysicalResourceGroup> result = physicalResourceGroupService.findAllPhysicalResourceGroups();
        Assert.assertNotNull("Find all method for 'PhysicalResourceGroup' illegally returned null", result);
        Assert.assertTrue("Find all method for 'PhysicalResourceGroup' failed to return any data", result.size() > 0);
    }

    @Test
    public void testFindPhysicalResourceGroupEntries() {
        Assert.assertNotNull("Data on demand for 'PhysicalResourceGroup' failed to initialize correctly",
                dod.getRandomPhysicalResourceGroup());
        long count = physicalResourceGroupService.countAllPhysicalResourceGroups();
        if (count > 20)
            count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<PhysicalResourceGroup> result = physicalResourceGroupService.findPhysicalResourceGroupEntries(firstResult,
                maxResults);
        Assert.assertNotNull("Find entries method for 'PhysicalResourceGroup' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'PhysicalResourceGroup' returned an incorrect number of entries",
                count, result.size());
    }

    @Test
    public void testFlush() {
        PhysicalResourceGroup obj = dod.getRandomPhysicalResourceGroup();
        Assert.assertNotNull("Data on demand for 'PhysicalResourceGroup' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PhysicalResourceGroup' failed to provide an identifier", id);
        obj = physicalResourceGroupService.findPhysicalResourceGroup(id);
        Assert.assertNotNull("Find method for 'PhysicalResourceGroup' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifyPhysicalResourceGroup(obj);
        Integer currentVersion = obj.getVersion();
        physicalResourceGroupRepo.flush();
        Assert.assertTrue("Version for 'PhysicalResourceGroup' failed to increment on flush directive",
                (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testUpdatePhysicalResourceGroupUpdate() {
        PhysicalResourceGroup obj = dod.getRandomPhysicalResourceGroup();
        Assert.assertNotNull("Data on demand for 'PhysicalResourceGroup' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PhysicalResourceGroup' failed to provide an identifier", id);
        obj = physicalResourceGroupService.findPhysicalResourceGroup(id);
        boolean modified = dod.modifyPhysicalResourceGroup(obj);
        Integer currentVersion = obj.getVersion();
        PhysicalResourceGroup merged = physicalResourceGroupService.updatePhysicalResourceGroup(obj);
        physicalResourceGroupRepo.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object",
                merged.getId(), id);
        Assert.assertTrue("Version for 'PhysicalResourceGroup' failed to increment on merge and flush directive",
                (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSavePhysicalResourceGroup() {
        Assert.assertNotNull("Data on demand for 'PhysicalResourceGroup' failed to initialize correctly",
                dod.getRandomPhysicalResourceGroup());
        PhysicalResourceGroup obj = dod.getNewTransientPhysicalResourceGroup(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'PhysicalResourceGroup' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'PhysicalResourceGroup' identifier to be null", obj.getId());
        physicalResourceGroupService.savePhysicalResourceGroup(obj);
        physicalResourceGroupRepo.flush();
        Assert.assertNotNull("Expected 'PhysicalResourceGroup' identifier to no longer be null", obj.getId());
    }

    @Test
    public void testDeletePhysicalResourceGroup() {
        PhysicalResourceGroup obj = dod.getRandomPhysicalResourceGroup();
        Assert.assertNotNull("Data on demand for 'PhysicalResourceGroup' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PhysicalResourceGroup' failed to provide an identifier", id);
        obj = physicalResourceGroupService.findPhysicalResourceGroup(id);
        physicalResourceGroupService.deletePhysicalResourceGroup(obj);
        physicalResourceGroupRepo.flush();
        Assert.assertNull("Failed to remove 'PhysicalResourceGroup' with identifier '" + id + "'",
                physicalResourceGroupService.findPhysicalResourceGroup(id));
    }
}
