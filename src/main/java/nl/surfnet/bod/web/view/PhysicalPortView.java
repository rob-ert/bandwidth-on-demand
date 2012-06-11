package nl.surfnet.bod.web.view;

import nl.surfnet.bod.domain.PhysicalPort;

public class PhysicalPortView {

  private final Long id;
  private final String managerLabel;
  private final String nocLabel;
  private final String portId;
  private final String physicalResourceGroupName;
  private final String networkElementPk;
  private ElementActionView deleteActionView;
  private Long numberOfVirtualPorts;
  private final boolean vlanRequired;
  private final boolean alignedWithNMS;
  private boolean deleteRender;

  public PhysicalPortView(final PhysicalPort physicalPort, final ElementActionView deleteActionView,
      final long virtualPortSize) {

    this(physicalPort, deleteActionView);
    this.numberOfVirtualPorts = virtualPortSize;
  }

  public PhysicalPortView(final PhysicalPort physicalPort, final ElementActionView deleteActionView) {
    this(physicalPort);

    if (deleteActionView == null) {
      this.deleteRender = false;
    }
    else {
      this.deleteActionView = deleteActionView;
    }
  }

  public PhysicalPortView(final PhysicalPort physicalPort) {
    this.id = physicalPort.getId();
    this.managerLabel = physicalPort.getManagerLabel();
    this.nocLabel = physicalPort.getNocLabel();
    this.portId = physicalPort.getPortId();
    this.physicalResourceGroupName = physicalPort.getPhysicalResourceGroup() == null ? null : physicalPort
        .getPhysicalResourceGroup().getName();
    this.networkElementPk = physicalPort.getNetworkElementPk();
    this.vlanRequired = physicalPort.isVlanRequired();
    this.alignedWithNMS = physicalPort.isAlignedWithNMS();

    this.numberOfVirtualPorts = 0L;
    this.deleteActionView = new ElementActionView(false, "");
    this.deleteRender = true;
  }

  public String getDeleteReasonKey() {
    return deleteActionView.getReasonKey();
  }

  public boolean isDeleteAllowed() {
    return deleteActionView.isAllowed();
  }

  public long getId() {
    return id;
  }

  public String getNocLabel() {
    return nocLabel;
  }

  public String getPortId() {
    return portId;
  }

  public String getPhysicalResourceGroupName() {
    return physicalResourceGroupName;
  }

  public String getNetworkElementPk() {
    return networkElementPk;
  }

  public ElementActionView getDeleteActionView() {
    return deleteActionView;
  }

  public String getManagerLabel() {
    return managerLabel;
  }

  public long getNumberOfVirtualPorts() {
    return numberOfVirtualPorts;
  }

  public boolean isDeleteRender() {
    return deleteRender;
  }

  public boolean isVlanRequired() {
    return vlanRequired;
  }

  public boolean isAlignedWithNMS() {
    return alignedWithNMS;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (alignedWithNMS ? 1231 : 1237);
    result = prime * result + ((deleteActionView == null) ? 0 : deleteActionView.hashCode());
    result = prime * result + (deleteRender ? 1231 : 1237);
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((managerLabel == null) ? 0 : managerLabel.hashCode());
    result = prime * result + ((networkElementPk == null) ? 0 : networkElementPk.hashCode());
    result = prime * result + ((nocLabel == null) ? 0 : nocLabel.hashCode());
    result = prime * result + ((numberOfVirtualPorts == null) ? 0 : numberOfVirtualPorts.hashCode());
    result = prime * result + ((physicalResourceGroupName == null) ? 0 : physicalResourceGroupName.hashCode());
    result = prime * result + ((portId == null) ? 0 : portId.hashCode());
    result = prime * result + (vlanRequired ? 1231 : 1237);
    return result;
  }

  @Override
  public String toString() {
    return "PhysicalPortView [id=" + id + ", managerLabel=" + managerLabel + ", nocLabel=" + nocLabel + ", portId="
        + portId + ", physicalResourceGroupName=" + physicalResourceGroupName + ", networkElementPk="
        + networkElementPk + ", deleteActionView=" + deleteActionView + ", numberOfVirtualPorts="
        + numberOfVirtualPorts + ", vlanRequired=" + vlanRequired + ", alignedWithNMS=" + alignedWithNMS
        + ", deleteRender=" + deleteRender + "]";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PhysicalPortView other = (PhysicalPortView) obj;
    if (alignedWithNMS != other.alignedWithNMS)
      return false;
    if (deleteActionView == null) {
      if (other.deleteActionView != null)
        return false;
    }
    else if (!deleteActionView.equals(other.deleteActionView))
      return false;
    if (deleteRender != other.deleteRender)
      return false;
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
    if (networkElementPk == null) {
      if (other.networkElementPk != null)
        return false;
    }
    else if (!networkElementPk.equals(other.networkElementPk))
      return false;
    if (nocLabel == null) {
      if (other.nocLabel != null)
        return false;
    }
    else if (!nocLabel.equals(other.nocLabel))
      return false;
    if (numberOfVirtualPorts == null) {
      if (other.numberOfVirtualPorts != null)
        return false;
    }
    else if (!numberOfVirtualPorts.equals(other.numberOfVirtualPorts))
      return false;
    if (physicalResourceGroupName == null) {
      if (other.physicalResourceGroupName != null)
        return false;
    }
    else if (!physicalResourceGroupName.equals(other.physicalResourceGroupName))
      return false;
    if (portId == null) {
      if (other.portId != null)
        return false;
    }
    else if (!portId.equals(other.portId))
      return false;
    if (vlanRequired != other.vlanRequired)
      return false;
    return true;
  }

}
