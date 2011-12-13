package nl.surfnet.bod.support;

import nl.surfnet.bod.domain.UserGroup;

public class UserGroupFactory {

  private String id = "urn:emtpy";
  private String title = "";
  private String description = "";
  
  public UserGroup create() {
    UserGroup group = new UserGroup(id, title, description);
    
    return group;
  }
  
  public UserGroupFactory setId(String id) {
    this.id = id;
    return this;
  }
  
  public UserGroupFactory setDescription(String description) {
    this.description = description;
    return this;
  }
  
  public UserGroupFactory setTitle(String title) {
    this.title = title;
    return this;
  }

}
