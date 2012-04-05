package nl.surfnet.bod.support;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.web.security.Security;

public class BodRoleFactory {

  private UserGroup userGroup = new UserGroupFactory().create();
  private String role = Security.RoleEnum.ICT_MANAGER.name();
  private Institute institute = new InstituteFactory().create();

  public BodRole create() {

    return new BodRole(userGroup, role, institute);
  }

  public BodRoleFactory setUserGroup(UserGroup userGroup) {
    this.userGroup = userGroup;
    return this;
  }

  public BodRoleFactory setRole(String role) {
    this.role = Security.RoleEnum.valueOf(role).name();
    return this;
  }

  public BodRoleFactory setInstitute(Institute institute) {
    this.institute = institute;
    return this;
  }

}
