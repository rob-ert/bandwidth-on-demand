package nl.surfnet.bod.service;

import java.util.Set;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.collect.Sets;

import static com.google.common.base.Preconditions.checkArgument;

@Service
@Transactional(readOnly = true)
public class ManagerService {

  @Resource
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Resource
  private VirtualResourceGroupService virtualResourceGroupService;

  @Resource
  private PhysicalPortService physicalPortService;

  /**
   * 
   * @param bodRole
   *          Role to search the adminGroups for
   * @return Set<String> set of all different admin groups from the role and the
   *         {@link VirtualResourceGroup}s which are related to the given role.
   */
  public Set<String> findAllAdminGroupsForManager(final BodRole bodRole) {
    checkArgument(bodRole.isManagerRole(), "Given role is not a manager: %s", bodRole);

    Set<String> adminGroups = Sets.newHashSet(bodRole.getAdminGroup().get());

    for (VirtualResourceGroup vrg : virtualResourceGroupService.findEntriesForManager(bodRole)) {
      adminGroups.add(vrg.getAdminGroup());

      for (VirtualPort vp : vrg.getVirtualPorts()) {
        adminGroups.add(vp.getPhysicalPort().getPhysicalResourceGroup().getAdminGroup());
      }
    }

    System.err.println(String.format("Manager [%s] has groups [%s]", bodRole.getInstituteName(), StringUtils
        .collectionToCommaDelimitedString(adminGroups)));
    return adminGroups;
  }
}
