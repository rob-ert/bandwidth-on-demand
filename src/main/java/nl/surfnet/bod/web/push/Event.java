package nl.surfnet.bod.web.push;

/**
 * An event that can be pushed to all the clients that are member of the group id.
 */
public interface Event {

  String getGroupId();

  String getMessage();
}
