package nl.surfnet.bod.domain;

import static junit.framework.Assert.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import javax.validation.ConstraintViolationException;

import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import nl.surfnet.bod.service.PhysicalResourceGroupServiceImpl;
import nl.surfnet.bod.support.PhysicalResourceGroupDataOnDemand;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/appCtx*.xml")
@Transactional
public class PhysicalResourceGroupDbTest {

  @Autowired
  private PhysicalResourceGroupDataOnDemand dod;

  @Autowired
  private PhysicalResourceGroupServiceImpl physicalResourceGroupService;

  @Autowired
  private PhysicalResourceGroupRepo physicalResourceGroupRepo;

  @Test
  public void countAllPhysicalResourceGroups() {
    assertNotNull(dod.getRandomPhysicalResourceGroup());

    long count = physicalResourceGroupService.count();

    assertThat(count, greaterThan(0L));
  }

  @Test
  public void findPhysicalResourceGroup() {
    PhysicalResourceGroup randomGroup = dod.getRandomPhysicalResourceGroup();

    PhysicalResourceGroup freshLoadedGroup = physicalResourceGroupService.find(randomGroup.getId());

    assertThat(randomGroup, is(freshLoadedGroup));
  }

  @Test
  public void findAllPhysicalResourceGroups() {
    dod.getRandomPhysicalResourceGroup();

    List<PhysicalResourceGroup> result = physicalResourceGroupService.findAll();

    assertThat(result, hasSize(greaterThan(0)));
  }

  @Test
  public void findPhysicalResourceGroupEntries() {
    dod.getRandomPhysicalResourceGroup();

    long count = physicalResourceGroupService.count();

    int maxResults = count > 20 ? 20 : (int) count;

    List<PhysicalResourceGroup> result = physicalResourceGroupService.findEntries(0, maxResults);

    assertThat(result, hasSize((int) count));
  }

  @Test
  public void testUpdatePhysicalResourceGroupUpdate() {
    PhysicalResourceGroup obj = dod.getRandomPhysicalResourceGroup();

    Integer initialVersion = obj.getVersion();

    obj.setName("New name");

    PhysicalResourceGroup merged = physicalResourceGroupService.update(obj);

    physicalResourceGroupRepo.flush();

    assertThat(merged.getId(), is(obj.getId()));
    assertThat(merged.getVersion(), greaterThan(initialVersion));
  }

  @Test
  public void savePhysicalResourceGroup() {
    PhysicalResourceGroup obj = dod.getNewTransientPhysicalResourceGroup(Integer.MAX_VALUE);

    physicalResourceGroupService.save(obj);

    physicalResourceGroupRepo.flush();

    assertThat(obj.getId(), greaterThan(0L));
  }

  @Test
  public void deletePhysicalResourceGroup() {
    PhysicalResourceGroup obj = dod.getRandomPhysicalResourceGroup();

    physicalResourceGroupService.delete(obj);

    physicalResourceGroupRepo.flush();

    assertThat(physicalResourceGroupService.find(obj.getId()), nullValue());
  }

  @Test(expected = ConstraintViolationException.class)
  public void physicalResourceGroupWithoutANameShouldNotSave() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setName(null).create();

    physicalResourceGroupService.save(group);
  }

  @Test(expected = ConstraintViolationException.class)
  public void physicalResourceGroupWithAnEmptyNameShouldNotSave() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setName("").create();

    physicalResourceGroupService.save(group);
  }

}
