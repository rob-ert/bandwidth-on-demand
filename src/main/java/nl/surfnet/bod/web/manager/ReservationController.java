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
package nl.surfnet.bod.web.manager;

import static nl.surfnet.bod.web.WebUtils.FILTER_SELECT;

import java.util.List;

import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.AbstractFilteredReservationController;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ReservationFilterView;
import nl.surfnet.bod.web.view.ReservationView;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("managerReservationController")
@RequestMapping(ReservationController.PAGE_URL)
public class ReservationController extends AbstractFilteredReservationController {

  public static final String PAGE_URL = "manager/reservations";

  @Override
  protected List<ReservationView> list(int firstPage, int maxItems, Sort sort, Model model) {
    ReservationFilterView filter = WebUtils.getAttributeFromModel(FILTER_SELECT, model);

    model.addAttribute("maxPages",
        WebUtils.calculateMaxPages(reservationService.countForFilterAndManager(Security.getUserDetails(), filter)));

    List<ReservationView> reservationViews = transformReservationToReservationView(reservationService
        .findEntriesForManagerUsingFilter(Security.getUserDetails(), filter, firstPage, maxItems, sort));

    return reservationViews;
  }

  @Override
  public String listUrl() {
    return PAGE_URL + WebUtils.LIST;
  }

}
