package nl.surfnet.bod.domain;

import java.util.concurrent.atomic.AtomicLong;

import nl.surfnet.bod.web.security.Security.RoleEnum;

import com.google.common.base.Objects;

public class BodRole {

  private static final AtomicLong COUNTER = new AtomicLong();

  private final Long id;
  private final String groupId;
  private final String groupName;
  private final String groupDescription;
  private final RoleEnum role;
  private Long instituteId;
  private String instituteName;

  public BodRole(UserGroup userGroup, String role) {
    this.id = COUNTER.incrementAndGet();
    this.groupId = userGroup.getId();
    this.groupName = userGroup.getName();
    this.groupDescription = userGroup.getDescription();
    this.role = RoleEnum.valueOf(role);
  }

  public BodRole(UserGroup userGroup, String role, Institute institute) {
    this(userGroup, role);
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
    return role.name();
  }

  public RoleEnum getRole() {
    return role;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof BodRole) {
      BodRole bodRole = (BodRole) obj;

      return Objects.equal(this.id, bodRole.id) && Objects.equal(this.groupId, bodRole.groupId)
          && Objects.equal(this.role, bodRole.getRole()) && Objects.equal(this.instituteId, bodRole.getInstituteId());
    }
    else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, groupId, role, instituteId);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("id", id).add("groupId", groupId).add("groupName", groupName)
        .add("groupDescription", groupDescription).add("role", role).add("instituteId", instituteId)
        .add("instituteName", instituteName).toString();
  }

}
