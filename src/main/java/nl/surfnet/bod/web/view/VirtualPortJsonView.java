package nl.surfnet.bod.web.view;

import nl.surfnet.bod.domain.VirtualPort;

public final class VirtualPortJsonView {
  private final String managerLabel;
  private final String userLabel;
  private final Integer maxBandwidth;
  private final Integer vlanId;
  private final String virtualResourceGroupName;
  private Long id;

  public VirtualPortJsonView(VirtualPort port) {
    this.id = port.getId();
    this.managerLabel = port.getManagerLabel();
    this.userLabel = port.getUserLabel();
    this.maxBandwidth = port.getMaxBandwidth();
    this.vlanId = port.getVlanId();
    this.virtualResourceGroupName = port.getVirtualResourceGroup().getName();
  }

  public Integer getMaxBandwidth() {
    return maxBandwidth;
  }

  public Integer getVlanId() {
    return vlanId;
  }

  public String getVirtualResourceGroupName() {
    return virtualResourceGroupName;
  }

  public String getManagerLabel() {
    return managerLabel;
  }

  public String getUserLabel() {
    return userLabel;
  }

  public Long getId() {
    return id;
  }
}