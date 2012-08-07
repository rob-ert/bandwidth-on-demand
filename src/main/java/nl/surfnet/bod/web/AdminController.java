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
package nl.surfnet.bod.web;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.util.BoDInitializer;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("admin")
public class AdminController {

  @PersistenceContext
  private EntityManager entityManager;

  @Resource
  private ReloadableResourceBundleMessageSource messageSource;

  @Resource
  private BoDInitializer boDInitializer;

  @RequestMapping(value = "refreshMessages", method = RequestMethod.GET)
  public String refreshMessageSource(HttpServletRequest request) {
    messageSource.clearCache();

    return "redirect:" + request.getHeader("Referer");
  }

  @RequestMapping(value = "error", method = RequestMethod.GET)
  public String error() {
    throw new RuntimeException("Something went wrong on purpose");
  }

  @RequestMapping(value = "index", method = RequestMethod.GET)
  public String indexData() {
    boDInitializer.indexDatabaseContent();

    return "redirect:/";
  }
}
