package nl.surfnet.bod.support;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.web.security.Security;

public class BodRoleFactory {

  private UserGroup userGroup = new UserGroupFactory().create();
  private Security.RoleEnum role = Security.RoleEnum.ICT_MANAGER;
  private PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();

  public BodRole create() {

    return new BodRole(userGroup, role, physicalResourceGroup);
  }

  public BodRoleFactory setUserGroup(UserGroup userGroup) {
    this.userGroup = userGroup;
    return this;
  }

  public BodRoleFactory setRole(Security.RoleEnum role) {
    this.role = role;
    return this;
  }

  public BodRoleFactory setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
    this.physicalResourceGroup = physicalResourceGroup;
    return this;
  }
}
