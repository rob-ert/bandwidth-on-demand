package nl.surfnet.bod.domain;

import com.google.common.base.Objects;

public class BodRole {

  private final String groupId;
  private final String groupName;
  private final String groupDescription;
  private final String roleName;
  private final Long instituteId;
  private final String instituteName;

  public BodRole(UserGroup userGroup, String role, Institute institute) {
    this.groupId = userGroup.getId();
    this.groupName = userGroup.getName();
    this.groupDescription = userGroup.getDescription();
    this.roleName = role;
    this.instituteId = institute.getId();
    this.instituteName = institute.getName();
  }

  public String getInstituteName() {
    return instituteName;
  }

  public Long getInstituteId() {
    return instituteId;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getGroupName() {
    return groupName;
  }

  public String getGroupDescription() {
    return groupDescription;
  }

  public String getRoleName() {
    return roleName;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("groupId", groupId).add("groupName", groupName)
        .add("groupDescription", groupDescription).add("roleName", roleName).add("instituteId", instituteId)
        .add("instituteName", instituteName).toString();
  }

}
