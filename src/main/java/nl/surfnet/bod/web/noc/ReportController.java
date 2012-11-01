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
package nl.surfnet.bod.web.noc;

import javax.annotation.Resource;

import nl.surfnet.bod.service.LogEventService;
import nl.surfnet.bod.util.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller("nocReportController")
@RequestMapping(ReportController.PAGE_URL)
public class ReportController {
  public static final String PAGE_URL = "noc/report";

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Resource
  private Environment environment;

  @Resource
  private LogEventService logEventService;

  @RequestMapping(method = RequestMethod.GET)
  public String index(Model model) {

    return PAGE_URL;
  }

}
