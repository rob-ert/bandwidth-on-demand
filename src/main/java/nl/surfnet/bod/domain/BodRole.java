package nl.surfnet.bod.domain;

import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Objects;

public class BodRole {

  private final Long id;
  private final String groupId;
  private final String groupName;
  private final String groupDescription;
  private final String roleName;
  private final Long instituteId;
  private final String instituteName;

  public BodRole(UserGroup userGroup, String role, Institute institute) {

    this.id = new AtomicLong().incrementAndGet();
    this.groupId = userGroup.getId();
    this.groupName = userGroup.getName();
    this.groupDescription = userGroup.getDescription();
    this.roleName = role;
    this.instituteId = institute.getId();
    this.instituteName = institute.getName();
  }

  public Long getId() {
    return id;
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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof BodRole) {
      BodRole bodRole = (BodRole) obj;

      return Objects.equal(this.id, bodRole.id) && Objects.equal(this.groupId, bodRole.groupId)
          && Objects.equal(this.roleName, bodRole.getRoleName())
          && Objects.equal(this.instituteId, bodRole.getInstituteId());
    }
    else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, groupId, roleName, instituteId);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("id", id).add("groupId", groupId).add("groupName", groupName)
        .add("groupDescription", groupDescription).add("roleName", roleName).add("instituteId", instituteId)
        .add("instituteName", instituteName).toString();
  }

}
