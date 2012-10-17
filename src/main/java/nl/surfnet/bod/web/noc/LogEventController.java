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

import java.util.Collections;
import java.util.List;

import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.web.base.AbstractLogEventController;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("nocEventController")
@RequestMapping(value = "/noc/" + LogEventController.PAGE_URL)
public class LogEventController extends AbstractLogEventController {

  @Override
  protected List<LogEvent> list(int firstPage, int maxItems, Sort sort, Model model) {
    return logEventService.findAll(firstPage, maxItems, sort);
  }

  @Override
  protected long count(Model model) {
    return logEventService.count();
  }

  @Override
  protected String listUrl() {
    return "noc/logevents";
  }

  @Override
  public List<Long> handleListFromController(Model model) {
    // TODO Auto-generated method stub
    return Collections.emptyList();
  }

}
