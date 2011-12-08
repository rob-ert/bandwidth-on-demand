package nl.surfnet.bod.util;

public class UserContext {

  private final String userName;
  private final String nameId;

  public UserContext(String nameId, String userName) {
    this.userName = userName;
    this.nameId = nameId;
  }

  public String getUserName() {
    return userName;
  }

  public String getNameId() {
    return nameId;
  }
}
