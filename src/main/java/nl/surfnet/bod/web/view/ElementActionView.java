package nl.surfnet.bod.web.view;

import org.springframework.context.MessageSource;

/**
 * View responsible for holding state related to an element in the user
 * interface. E.g. if the deletion of a row is enabled. The {@link #isAllowed()}
 * indicates if the action is allowed, the {@link #getReason()} contains the
 * message key for the reason why or why not. The convention is that the
 * reasonKey is retrieved from a {@link MessageSource} and shown as a tooltip.
 * 
 * @author Franky
 * 
 */
public class ElementActionView {

  private final boolean allowed;
  private final String reasonKey;

  public ElementActionView(final boolean actionAllowed) {
    this(true, null);
  }

  public ElementActionView(final boolean actionAllowed, final String actionReasonKey) {
    this.allowed = actionAllowed;
    this.reasonKey = actionReasonKey;
  }

  public boolean isAllowed() {
    return allowed;
  }

  public String getReasonKey() {
    return reasonKey;
  }

}
