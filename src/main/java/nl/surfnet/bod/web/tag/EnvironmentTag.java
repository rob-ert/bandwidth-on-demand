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

import nl.surfnet.bod.util.Environment;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class EnvironmentTag extends BodTagSupport {

  private String property;
  private String var;

  @Override
  public void doTag() throws JspException, IOException {
    Environment env = getEnvironment();

    try {
      Object value = PropertyUtils.getSimpleProperty(env, property);
      getPageContext().setAttribute(var, value);
    } catch (Exception e) {
      throw new JspException(e);
    }
  }

  private Environment getEnvironment() {
    WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getPageContext()
        .getServletContext());
    return applicationContext.getBean(Environment.class);
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  public String getVar() {
    return var;
  }

  public void setVar(String var) {
    this.var = var;
  }

}
