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

import static nl.surfnet.bod.web.WebUtils.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.domain.validator.ReservationValidator;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(ReservationController.PAGE_URL)
@Controller
public class ReservationController {
  static final String PAGE_URL = "reservations";

  static final String MODEL_KEY = "reservation";
  static final String MODEL_KEY_LIST = MODEL_KEY + LIST_POSTFIX;

  @Autowired
  private ReservationService reservationService;

  @Autowired
  private VirtualResourceGroupService virtualResourceGroupService;

  private ReservationValidator reservationValidator = new ReservationValidator();

  @RequestMapping(method = RequestMethod.POST)
  public String create(@Valid Reservation reservation, final BindingResult bindingResult, final Model uiModel,
      final HttpServletRequest httpServletRequest) {

    // First add meta information, then validate
    RichUserDetails user = (RichUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    reservation.setUser(user.getDisplayName());

    reservationValidator.validate(reservation, bindingResult);
    if (bindingResult.hasErrors()) {
      uiModel.addAttribute(MODEL_KEY, reservation);
      return PAGE_URL + CREATE;
    }

    uiModel.asMap().clear();
    reservationService.save(reservation);

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = CREATE, method = RequestMethod.GET)
  public String createForm(final Model uiModel) {
    RichUserDetails user = (RichUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    Reservation reservation = createDefaultReservation();
    uiModel.addAttribute(MODEL_KEY, reservation);

    Collection<VirtualResourceGroup> virtualResourceGroups = virtualResourceGroupService.findAllForUser(user);
    uiModel.addAttribute("virtualResourceGroups", virtualResourceGroups);

    if (virtualResourceGroups.isEmpty()) {
      uiModel.addAttribute("virtualPorts", Collections.emptyList());
    }
    else {
      uiModel.addAttribute("virtualPorts", virtualResourceGroups.iterator().next().getVirtualPorts());
    }

    return PAGE_URL + CREATE;
  }

  @RequestMapping(params = ID_KEY, method = RequestMethod.GET)
  public String show(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, reservationService.find(id));
    // Needed for the default icons
    uiModel.addAttribute(ICON_ITEM_KEY, id);

    return PAGE_URL + SHOW;
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY_LIST, reservationService.findEntries(calculateFirstPage(page), MAX_ITEMS_PER_PAGE));

    uiModel.addAttribute(MAX_PAGES_KEY, calculateMaxPages(reservationService.count()));

    return PAGE_URL + LIST;
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid final Reservation reservation, final BindingResult bindingResult, final Model uiModel,
      final HttpServletRequest httpServletRequest) {

    // First add meta information, then validate
    RichUserDetails user = (RichUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    reservation.setUser(user.getDisplayName());

    reservationValidator.validate(reservation, bindingResult);
    if (bindingResult.hasErrors()) {
      uiModel.addAttribute(MODEL_KEY, reservation);
      return PAGE_URL + UPDATE;
    }

    uiModel.asMap().clear();
    reservationService.update(reservation);

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = EDIT, params = ID_KEY, method = RequestMethod.GET)
  public String updateForm(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, reservationService.find(id));
    return PAGE_URL + UPDATE;
  }

  @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
  public String delete(@RequestParam(ID_KEY) final Long id,
      @RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {

    Reservation reservation = reservationService.find(id);
    reservationService.delete(reservation);

    uiModel.asMap().clear();
    uiModel.addAttribute(PAGE_KEY, (page == null) ? "1" : page.toString());

    return "redirect:";
  }

  protected void setVirtualResourceGroupService(VirtualResourceGroupService virtualResourceGroupService) {
    this.virtualResourceGroupService = virtualResourceGroupService;
  }

  /**
   * Create {@link Reservation} with intelligent default values
   *
   * @return {@link Reservation} new instance
   */
  private Reservation createDefaultReservation() {
    Reservation reservation = new Reservation();
    reservation.setStartDate(new Date());
    reservation.setStartTime(new Date());
    reservation.setEndDate(reservation.getStartDate());
    reservation.setEndTime(new DateTime(reservation.getEndDate()).plusHours(4).toDate());
    return reservation;
  }

}