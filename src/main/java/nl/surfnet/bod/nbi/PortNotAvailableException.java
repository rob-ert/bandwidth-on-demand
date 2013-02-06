package nl.surfnet.bod.nbi;

public class PortNotAvailableException extends Exception {
  private static final long serialVersionUID = 1L;

  private final String nmsPortId;

  public PortNotAvailableException(String nmsPortId) {
    this.nmsPortId = nmsPortId;
  }

  public String getNmsPortId() {
    return nmsPortId;
  }

}
