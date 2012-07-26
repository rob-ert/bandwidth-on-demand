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

import static nl.surfnet.bod.web.WebUtils.*;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.AbstractFilteredReservationController;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ReservationFilterView;
import nl.surfnet.bod.web.view.ReservationView;

@RequestMapping(ReservationController.PAGE_URL)
@Controller(value = "nocReservationController")
public class ReservationController extends AbstractFilteredReservationController {
  public static final String PAGE_URL = "noc/reservations";
  public static final String ELAPSED_URL = PAGE_URL + "/" + FILTER_URL + ReservationFilterViewFactory.ELAPSED;
  public static final String ACTIVE_URL = PAGE_URL + "/" + FILTER_URL + ReservationFilterViewFactory.ACTIVE;
  public static final String COMING_URL = PAGE_URL + "/" + FILTER_URL + ReservationFilterViewFactory.COMING;

  @Override
  public String listUrl() {
    return PAGE_URL + LIST;
  }

  @Override
  protected List<ReservationView> list(int firstPage, int maxItems, Sort sort, Model model) {
    ReservationFilterView filter = WebUtils.getAttributeFromModel(FILTER_SELECT, model);

    model.addAttribute("maxPages",
        WebUtils.calculateMaxPages(getReservationService().countAllEntriesUsingFilter((filter))));

    return transformReservationToReservationView(getReservationService().//
        findAllEntriesUsingFilter(filter, firstPage, maxItems, sort), Security.getUserDetails());
  }

}
