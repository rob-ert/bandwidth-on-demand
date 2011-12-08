package nl.surfnet.bod.util;

import static com.google.common.base.Preconditions.checkNotNull;

public class UserContext {

  private final String userName;
  private final String nameId;

  public UserContext(String nameId, String userName) {
    this.userName = checkNotNull(userName);
    this.nameId = checkNotNull(nameId);
  }

  public String getUserName() {
    return userName;
  }

  public String getNameId() {
    return nameId;
  }
}
