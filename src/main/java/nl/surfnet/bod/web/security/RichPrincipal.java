package nl.surfnet.bod.web.security;

public class RichPrincipal {

  private final String nameId;
  private final String displayName;

  public RichPrincipal(String nameId, String displayName) {
    this.nameId = nameId;
    this.displayName = displayName;
  }

  public String getNameId() {
    return nameId;
  }
  public String getDisplayName() {
    return displayName;
  }

}