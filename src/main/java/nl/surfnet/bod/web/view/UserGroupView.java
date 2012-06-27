package nl.surfnet.bod.web.view;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup;

public class UserGroupView implements Comparable<UserGroupView> {
  private final String name;
  private final String description;
  private final String surfconextGroupId;
  private final boolean existing;

  public UserGroupView(UserGroup userGroup) {
    this.name = userGroup.getName();
    this.description = userGroup.getDescription();
    this.surfconextGroupId = userGroup.getId();
    this.existing = false;
  }

  public UserGroupView(VirtualResourceGroup vrg) {
    this.name = vrg.getName();
    this.description = vrg.getDescription();
    this.surfconextGroupId = vrg.getSurfconextGroupId();
    this.existing = true;
  }

  @Override
  public int compareTo(UserGroupView other) {
    if (this.equals(other)) {
      return 0;
    }
    else {
      return this.getName().compareTo(other.getName());
    }
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getSurfconextGroupId() {
    return surfconextGroupId;
  }

  public boolean isExisting() {
    return existing;
  }

}
