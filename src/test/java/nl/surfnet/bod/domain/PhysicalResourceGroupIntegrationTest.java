package nl.surfnet.bod.domain;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;

import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import nl.surfnet.bod.service.PhysicalResourceGroupServiceImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/appCtx*.xml")
@Transactional
public class PhysicalResourceGroupIntegrationTest {

    @Autowired
    private PhysicalResourceGroupDataOnDemand dod;

    @Autowired
    private PhysicalResourceGroupServiceImpl physicalResourceGroupService;

    @Autowired
    private PhysicalResourceGroupRepo physicalResourceGroupRepo;

    @Test
    public void countAllPhysicalResourceGroups() {
        assertNotNull(dod.getRandomPhysicalResourceGroup());

        long count = physicalResourceGroupService.countAllPhysicalResourceGroups();

        assertThat(count, greaterThan(0L));
    }

    @Test
    public void findPhysicalResourceGroup() {
        PhysicalResourceGroup randomGroup = dod.getRandomPhysicalResourceGroup();

        PhysicalResourceGroup freshLoadedGroup = physicalResourceGroupService.findPhysicalResourceGroup(randomGroup
                .getId());

        assertThat(randomGroup, is(freshLoadedGroup));
    }

    @Test
    public void findAllPhysicalResourceGroups() {
        dod.getRandomPhysicalResourceGroup();

        List<PhysicalResourceGroup> result = physicalResourceGroupService.findAllPhysicalResourceGroups();

        assertThat(result, hasSize(greaterThan(0)));
    }

    @Test
    public void findPhysicalResourceGroupEntries() {
        dod.getRandomPhysicalResourceGroup();

        long count = physicalResourceGroupService.countAllPhysicalResourceGroups();

        int maxResults = count > 20 ? 20 : (int) count;

        List<PhysicalResourceGroup> result = physicalResourceGroupService.findPhysicalResourceGroupEntries(0,
                maxResults);

        assertThat(result, hasSize((int) count));
    }

    @Test
    public void testUpdatePhysicalResourceGroupUpdate() {
        PhysicalResourceGroup obj = dod.getRandomPhysicalResourceGroup();

        Integer initialVersion = obj.getVersion();

        obj.setName("New name");

        PhysicalResourceGroup merged = physicalResourceGroupService.updatePhysicalResourceGroup(obj);

        physicalResourceGroupRepo.flush();

        assertThat(merged.getId(), is(obj.getId()));
        assertThat(merged.getVersion(), greaterThan(initialVersion));
    }

    @Test
    public void savePhysicalResourceGroup() {
        PhysicalResourceGroup obj = dod.getNewTransientPhysicalResourceGroup(Integer.MAX_VALUE);

        physicalResourceGroupService.savePhysicalResourceGroup(obj);

        physicalResourceGroupRepo.flush();

        assertThat(obj.getId(), greaterThan(0L));
    }

    @Test
    public void testDeletePhysicalResourceGroup() {
        PhysicalResourceGroup obj = dod.getRandomPhysicalResourceGroup();

        physicalResourceGroupService.deletePhysicalResourceGroup(obj);

        physicalResourceGroupRepo.flush();

        assertThat(physicalResourceGroupService.findPhysicalResourceGroup(obj.getId()), nullValue());
    }
}
