package nl.surfnet.bod.web.view;

import nl.surfnet.bod.domain.PhysicalPort;

public class PhysicalPortJsonView {
  private final Long id;
  private final String managerLabel;
  private final String nocLabel;
  private final String networkElementPk;

  public PhysicalPortJsonView(PhysicalPort port) {
    id = port.getId();
    managerLabel = port.getManagerLabel();
    nocLabel = port.getNocLabel();
    networkElementPk = port.getNetworkElementPk();
  }

  public Long getId() {
    return id;
  }

  public String getManagerLabel() {
    return managerLabel;
  }

  public String getNocLabel() {
    return nocLabel;
  }

  public String getNetworkElementPk() {
    return networkElementPk;
  }
}
