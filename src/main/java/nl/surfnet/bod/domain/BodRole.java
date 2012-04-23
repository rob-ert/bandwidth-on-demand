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
public final class BodRole {

  private static final AtomicLong COUNTER = new AtomicLong();

  private final Long id;
  private final RoleEnum role;
  private String instituteName;
  private Long physicalResourceGroupId;

  public static BodRole createNewUser() {
    return new BodRole(RoleEnum.NEW_USER);
  }

  public static BodRole createUser() {
    return new BodRole(RoleEnum.USER);
  }

  public static BodRole createNocEngineer() {
    return new BodRole(RoleEnum.NOC_ENGINEER);
  }

  public static BodRole createManager(PhysicalResourceGroup prg) {
    return new BodRole(RoleEnum.ICT_MANAGER, prg);
  }

  private BodRole(Security.RoleEnum role) {
    this.id = COUNTER.incrementAndGet();
    this.role = role;
  }

  private BodRole(Security.RoleEnum role, PhysicalResourceGroup physicalResourceGroup) {
    this(role);
    this.instituteName = physicalResourceGroup.getName();
    this.physicalResourceGroupId = physicalResourceGroup.getId();
  }

  public Long getId() {
    return id;
  }

  public String getInstituteName() {
    return instituteName;
  }

  public Long getPhysicalResourceGroupId() {
    return physicalResourceGroupId;
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
      BodRole other = (BodRole) obj;

      return Objects.equal(this.role, other.getRole())
          && Objects.equal(this.physicalResourceGroupId, other.getPhysicalResourceGroupId());
    }
    else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(role, physicalResourceGroupId);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("id", id).add("role", role)
        .add("instituteName", instituteName).add("physicalResourceGroupId", physicalResourceGroupId).toString();
  }
}
