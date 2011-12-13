package nl.surfnet.bod.domain;

public class UserGroup {

  private final String id;
  private final String title;
  private final String description;
  
  public UserGroup(String id, String title, String description) {
    this.id = id;
    this.title = title;
    this.description = description;
  }
  
  public String getId() {
    return id;
  }
  
  public String getTitle() {
    return title;
  }
  
  public String getDescription() {
    return description;
  }
  
}
