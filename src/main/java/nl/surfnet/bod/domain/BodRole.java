package nl.surfnet.bod.domain;

import java.util.concurrent.atomic.AtomicLong;

import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.security.Security.RoleEnum;

import com.google.common.base.Objects;

/**
 * Represents a role so the user can switch between them. Note that only the
 * {@link #role} and {@link #instituteId} are relevant for the
 * {@link #equals(Object)} and {@link #hashCode()}. This way duplicate
 * {@link Security.RoleEnum#USER} are prevented even if the are related to
 * different groups. Multiple {@link RoleEnum#ICT_MANAGER} roles are allowed, as
 * long as they are related to different {@link #instituteId}s.
 * 
 * @author Franky
 * 
 */
public class BodRole {

  private static final AtomicLong COUNTER = new AtomicLong();

  private final Long id;
  private final String groupId;
  private final String groupName;
  private final String groupDescription;
  private final RoleEnum role;
  private Long instituteId;
  private String instituteName;

  public BodRole(UserGroup userGroup, Security.RoleEnum role) {
    this.id = COUNTER.incrementAndGet();
    this.groupId = userGroup.getId();
    this.groupName = userGroup.getName();
    this.groupDescription = userGroup.getDescription();
    this.role = role;
  }

  public BodRole(UserGroup userGroup, Security.RoleEnum role, Institute institute) {
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

      return Objects.equal(this.role, bodRole.getRole()) && Objects.equal(this.instituteId, bodRole.getInstituteId());
    }
    else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(role, instituteId);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("id", id).add("groupId", groupId).add("groupName", groupName)
        .add("groupDescription", groupDescription).add("role", role).add("instituteId", instituteId)
        .add("instituteName", instituteName).toString();
  }

}
