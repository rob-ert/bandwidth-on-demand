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
package nl.surfnet.bod.web.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;

public class CssTag extends BodTagSupport {

  private static final String TEMPLATE = "<link  href=\"%s\" rel=\"stylesheet\" type=\"text/css\" media=\"screen\" />";

  private String value;

  @Override
  public void doTag() throws JspException, IOException {
    getJspContext().getOut().write(String.format(TEMPLATE, createUrl()));
  }

  private String createUrl() {
    return Tags.createUrl(value, getRequest());
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
