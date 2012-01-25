package nl.surfnet.bod.web.push;

public final class Events {

  public static Event createSimpleEvent(String groupId, String message) {
    return new SimpleEvent(groupId, message);
  }

  public static final class SimpleEvent implements Event {
    private String groupId;
    private String message;

    public SimpleEvent(String groupId, String message) {
      this.groupId = groupId;
      this.message = message;
    }

    @Override
    public String getGroupId() {
      return groupId;
    }

    @Override
    public String getMessage() {
      return message;
    }

  }
}
