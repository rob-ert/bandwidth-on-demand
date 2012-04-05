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

import java.util.List;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.web.AbstractSortableListController;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("managerReservationController")
@RequestMapping("/manager/reservations")
public class ReservationController extends AbstractSortableListController<Reservation> {

  @Autowired
  private ReservationService reservationService;

  @Override
  protected List<Reservation> list(int firstPage, int maxItems, Sort sort, Model model) {
    return reservationService.findEntriesForManager(Security.getUserDetails(), firstPage, maxItems, sort);
  }

  @Override
  public long count() {
    return reservationService.countForManager(Security.getUserDetails());
  }

  @Override
  public String defaultSortProperty() {
    return "name";
  }

  @Override
  public String listUrl() {
    return "manager/reservations/list";
  }

}
