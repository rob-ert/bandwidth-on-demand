/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
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
