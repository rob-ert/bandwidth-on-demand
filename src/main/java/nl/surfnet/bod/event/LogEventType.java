package nl.surfnet.bod.event;

public enum LogEventType {

  CREATE("create.object"), READ("read.object"), UPDATE("update.object"), DELETE("delete.object");

  private String action;

  private LogEventType(String name) {
    this.action = name;
  }

  public String getAction() {
    return action;
  }

}
