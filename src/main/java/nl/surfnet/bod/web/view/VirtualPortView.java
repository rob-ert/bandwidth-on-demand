package nl.surfnet.bod.web.view;

import nl.surfnet.bod.domain.VirtualPort;

import com.google.common.base.Optional;

public class VirtualPortView {
  private final Long id;
  private final String managerLabel;
  private final Integer maxBandwidth;
  private final Integer vlanId;
  private final String virtualResourceGroup;
  private final String physicalResourceGroup;
  private final String physicalPort;
  private final String userLabel;
  private final Optional<Long> reservationCounter;
  private final String nsiStpId;

  public VirtualPortView(VirtualPort port) {
    this(port, Optional.<Long> absent());
  }

  public VirtualPortView(VirtualPort port, final Optional<Long> reservationCounter) {
    id = port.getId();
    managerLabel = port.getManagerLabel();
    userLabel = port.getUserLabel();
    maxBandwidth = port.getMaxBandwidth();
    vlanId = port.getVlanId();
    virtualResourceGroup = port.getVirtualResourceGroup().getName();
    physicalResourceGroup = port.getPhysicalResourceGroup().getName();
    physicalPort = port.getPhysicalPort().getManagerLabel();
    this.reservationCounter = reservationCounter;
    this.nsiStpId = port.getNsiStpId();
  }

  public String getManagerLabel() {
    return managerLabel;
  }

  public Integer getMaxBandwidth() {
    return maxBandwidth;
  }

  public Integer getVlanId() {
    return vlanId;
  }

  public String getVirtualResourceGroup() {
    return virtualResourceGroup;
  }

  public String getPhysicalResourceGroup() {
    return physicalResourceGroup;
  }

  public String getPhysicalPort() {
    return physicalPort;
  }

  public Long getId() {
    return id;
  }

  public String getUserLabel() {
    return userLabel;
  }

  public Long getReservationCounter() {
    return reservationCounter.orNull();
  }

  public String getNsiStpId() {
    return nsiStpId;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("VirtualPortView [id=");
    builder.append(id);
    builder.append(", managerLabel=");
    builder.append(managerLabel);
    builder.append(", maxBandwidth=");
    builder.append(maxBandwidth);
    builder.append(", vlanId=");
    builder.append(vlanId);
    builder.append(", virtualResourceGroup=");
    builder.append(virtualResourceGroup);
    builder.append(", physicalResourceGroup=");
    builder.append(physicalResourceGroup);
    builder.append(", physicalPort=");
    builder.append(physicalPort);
    builder.append(", userLabel=");
    builder.append(userLabel);
    builder.append(", reservationCounter=");
    builder.append(reservationCounter.orNull());
    builder.append(", nsiStpId=");
    builder.append(nsiStpId);
    builder.append("]");
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((managerLabel == null) ? 0 : managerLabel.hashCode());
    result = prime * result + ((maxBandwidth == null) ? 0 : maxBandwidth.hashCode());
    result = prime * result + ((nsiStpId == null) ? 0 : nsiStpId.hashCode());
    result = prime * result + ((physicalPort == null) ? 0 : physicalPort.hashCode());
    result = prime * result + ((physicalResourceGroup == null) ? 0 : physicalResourceGroup.hashCode());
    result = prime * result + ((reservationCounter == null) ? 0 : reservationCounter.hashCode());
    result = prime * result + ((userLabel == null) ? 0 : userLabel.hashCode());
    result = prime * result + ((virtualResourceGroup == null) ? 0 : virtualResourceGroup.hashCode());
    result = prime * result + ((vlanId == null) ? 0 : vlanId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    VirtualPortView other = (VirtualPortView) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    if (managerLabel == null) {
      if (other.managerLabel != null)
        return false;
    }
    else if (!managerLabel.equals(other.managerLabel))
      return false;
    if (maxBandwidth == null) {
      if (other.maxBandwidth != null)
        return false;
    }
    else if (!maxBandwidth.equals(other.maxBandwidth))
      return false;
    if (nsiStpId == null) {
      if (other.nsiStpId != null)
        return false;
    }
    else if (!nsiStpId.equals(other.nsiStpId))
      return false;
    if (physicalPort == null) {
      if (other.physicalPort != null)
        return false;
    }
    else if (!physicalPort.equals(other.physicalPort))
      return false;
    if (physicalResourceGroup == null) {
      if (other.physicalResourceGroup != null)
        return false;
    }
    else if (!physicalResourceGroup.equals(other.physicalResourceGroup))
      return false;
    if (reservationCounter == null) {
      if (other.reservationCounter != null)
        return false;
    }
    else if (!reservationCounter.equals(other.reservationCounter))
      return false;
    if (userLabel == null) {
      if (other.userLabel != null)
        return false;
    }
    else if (!userLabel.equals(other.userLabel))
      return false;
    if (virtualResourceGroup == null) {
      if (other.virtualResourceGroup != null)
        return false;
    }
    else if (!virtualResourceGroup.equals(other.virtualResourceGroup))
      return false;
    if (vlanId == null) {
      if (other.vlanId != null)
        return false;
    }
    else if (!vlanId.equals(other.vlanId))
      return false;
    return true;
  }

}