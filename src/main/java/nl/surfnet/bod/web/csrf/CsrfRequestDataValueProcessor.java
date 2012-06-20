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
package nl.surfnet.bod.web.csrf;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestDataValueProcessor;

import com.google.common.collect.ImmutableMap;

@Component(value = "requestDataValueProcessor")
public class CsrfRequestDataValueProcessor implements RequestDataValueProcessor {

  @Override
  public String processAction(HttpServletRequest request, String action) {
    return action;
  }

  @Override
  public String processFormFieldValue(HttpServletRequest request, String name, String value, String type) {
    return value;
  }

  @Override
  public Map<String, String> getExtraHiddenFields(HttpServletRequest request) {
    return new ImmutableMap.Builder<String, String>().put(
        CsrfTokenManager.CSRF_PARAM_NAME,
        CsrfTokenManager.getTokenForSession(request.getSession())).build();
  }

  @Override
  public String processUrl(HttpServletRequest request, String url) {
    return url;
  }

}
