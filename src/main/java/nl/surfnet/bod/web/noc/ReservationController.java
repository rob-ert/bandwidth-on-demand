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

import static nl.surfnet.bod.web.WebUtils.FILTER_SELECT;
import static nl.surfnet.bod.web.WebUtils.LIST;

import java.util.List;

import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.AbstractFilteredReservationController;
import nl.surfnet.bod.web.view.ReservationFilterView;
import nl.surfnet.bod.web.view.ReservationView;

import org.joda.time.Hours;
import org.joda.time.ReadablePeriod;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.Lists;

@RequestMapping(ReservationController.PAGE_URL)
@Controller(value = "nocReservationController")
public class ReservationController extends AbstractFilteredReservationController {

  public static final ReadablePeriod DEFAULT_RESERVATON_DURATION = Hours.FOUR;
  public static final String FILTER_COMMING_PERIOD = "comming";
  public static final String FILTER_ELAPSED_PERIOD = "elapsed";

  static final String PAGE_URL = "noc/reservations";

  @Override
  public String listUrl() {
    return PAGE_URL + LIST;
  }

  @Override
  protected List<ReservationView> list(int firstPage, int maxItems, Sort sort, Model model) {
    ReservationFilterView filter = WebUtils.getAttributeFromModel(FILTER_SELECT, model);

    model.addAttribute("maxPages", WebUtils.calculateMaxPages(reservationService.countAllEntriesUsingFilter((filter))));

    return Lists.transform(reservationService.findAllEntriesUsingFilter(filter, firstPage, maxItems, sort),
        TO_RESERVATION_VIEW);
  }

}
