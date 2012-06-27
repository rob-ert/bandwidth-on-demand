package nl.surfnet.bod.web.view;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup;

public class TeamView implements Comparable<TeamView> {
  private final Long id;
  private final String name;
  private final long numberOfPorts;
  private final long totalReservations;
  private final long scheduledReservations;
  private final long activeReservations;
  private final String surfconextGroupId;
  private final boolean existing;

  public TeamView(UserGroup group) {
    this.id = null;
    this.name = group.getName();
    this.surfconextGroupId = group.getId();
    this.numberOfPorts = 0;
    this.totalReservations = 0;
    this.scheduledReservations = 0;
    this.activeReservations = 0;
    this.existing = false;
  }

  public TeamView(VirtualResourceGroup group, long activeReservations, long scheduledReservations,
      long totalReservations) {
    this.id = group.getId();
    this.name = group.getName();
    this.numberOfPorts = group.getVirtualPortCount();
    this.surfconextGroupId = group.getSurfconextGroupId();
    this.totalReservations = totalReservations;
    this.scheduledReservations = scheduledReservations;
    this.activeReservations = activeReservations;
    this.existing = true;
  }

  public String getName() {
    return name;
  }

  public long getNumberOfPorts() {
    return numberOfPorts;
  }

  public String getSurfconextGroupId() {
    return surfconextGroupId;
  }

  public boolean isExisting() {
    return existing;
  }

  public long getTotalReservations() {
    return totalReservations;
  }

  public long getScheduledReservations() {
    return scheduledReservations;
  }

  public long getActiveReservations() {
    return activeReservations;
  }

  @Override
  public int compareTo(TeamView other) {
    if (this.equals(other)) {
      return 0;
    }
    else {
      return this.getName().compareTo(other.getName());
    }
  }

  public Long getId() {
    return id;
  }
}
