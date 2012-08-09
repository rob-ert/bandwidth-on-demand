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
package nl.surfnet.bod.web.user;

import static nl.surfnet.bod.util.Orderings.*;
import static nl.surfnet.bod.web.WebUtils.*;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.joda.time.Hours;
import org.joda.time.LocalDateTime;
import org.joda.time.ReadablePeriod;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.domain.validator.ReservationValidator;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.AbstractFilteredReservationController;
import nl.surfnet.bod.web.base.MessageView;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ReservationFilterView;
import nl.surfnet.bod.web.view.ReservationView;

@RequestMapping(ReservationController.PAGE_URL)
@Controller(value = "userReservationController")
public class ReservationController extends AbstractFilteredReservationController {

  public static final ReadablePeriod DEFAULT_RESERVATON_DURATION = Hours.FOUR;

  static final String PAGE_URL = "reservations";

  static final String MODEL_KEY = "reservation";

  @Resource
  private VirtualResourceGroupService virtualResourceGroupService;

  @Resource
  private MessageSource messageSource;

  private ReservationValidator reservationValidator = new ReservationValidator();

  @RequestMapping(method = RequestMethod.POST)
  public String create(@Valid Reservation reservation, BindingResult bindingResult, Model model,
      RedirectAttributes redirectAttributes) {

    reservation.setUserCreated(Security.getUserDetails().getNameId());

    reservationValidator.validate(reservation, bindingResult);

    if (bindingResult.hasErrors()) {
      model.addAttribute(MODEL_KEY, reservation);
      model.addAttribute("virtualResourceGroups", findVirtualResourceGroups());
      model.addAttribute("virtualPorts",
          vpUserLabelOrdering().sortedCopy(reservation.getVirtualResourceGroup().getVirtualPorts()));

      return PAGE_URL + CREATE;
    }

    getReservationService().create(reservation);

    WebUtils.addInfoMessage(redirectAttributes, messageSource, "info_reservation_created", reservation.getName(),
        reservation.getVirtualResourceGroup().getName());

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = CREATE, method = RequestMethod.GET)
  public String createForm(@RequestParam(value = "vrg", required = false) Long virtualResourceGroupId, Model model) {

    Collection<VirtualResourceGroup> vrgs = findVirtualResourceGroups();

    if (vrgs.isEmpty()) {
      MessageView message = MessageView.createInfoMessage(messageSource,
          "info_reservation_need_two_virtual_ports_title", "info_reservation_need_two_virtual_ports_message");
      message.addButton(WebUtils.getMessage(messageSource, "menu_overview_label"), "/user");

      model.addAttribute(MessageView.MODEL_KEY, message);

      return MessageView.PAGE_URL;
    }

    Reservation defaultReservation = null;

    if (virtualResourceGroupId != null) {
      VirtualResourceGroup vrg = virtualResourceGroupService.find(virtualResourceGroupId);
      if (vrg != null && Security.isUserMemberOf(vrg)) {
        defaultReservation = createDefaultReservation(vrg);
      }
    }

    if (defaultReservation == null) {
      defaultReservation = createDefaultReservation(Iterables.get(vrgs, 0));
    }

    model.addAttribute(MODEL_KEY, defaultReservation);
    model.addAttribute("virtualResourceGroups", vrgs);
    model.addAttribute("virtualPorts",
        vpUserLabelOrdering().sortedCopy(defaultReservation.getVirtualResourceGroup().getVirtualPorts()));

    return PAGE_URL + CREATE;
  }

  @Override
  public String listUrl() {
    return PAGE_URL + LIST;
  }

  @Override
  public String getDefaultSortProperty() {
    return "name";
  }

  @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
  public String delete(@RequestParam(ID_KEY) Long id, Model model, RedirectAttributes redirectAttributes) {

    Reservation reservation = getReservationService().find(id);

    getReservationService().cancel(reservation, Security.getUserDetails());

    // Response is ignored, in js related to link
    return "index";
  }

  @Override
  protected Class<Reservation> getEntityClass() {
    return Reservation.class;
  }

  @Override
  protected List<ReservationView> list(int firstPage, int maxItems, Sort sort, Model model) {
    ReservationFilterView filter = WebUtils.getAttributeFromModel(FILTER_SELECT, model);

    model.addAttribute("maxPages",
        WebUtils.calculateMaxPages(getReservationService().countForFilterAndUser(Security.getUserDetails(), filter)));

    return transformReservationToReservationView(
        getReservationService().findEntriesForUserUsingFilter(Security.getUserDetails(), filter, firstPage, maxItems,
            sort), Security.getUserDetails());
  }

  private List<VirtualResourceGroup> findVirtualResourceGroups() {
    RichUserDetails user = Security.getUserDetails();

    return vrgNameOrdering().sortedCopy(
        Collections2.filter(virtualResourceGroupService.findAllForUser(user), new Predicate<VirtualResourceGroup>() {
          @Override
          public boolean apply(VirtualResourceGroup group) {
            return group.getVirtualPorts().size() > 1;
          }
        }));
  }

  private Reservation createDefaultReservation(VirtualResourceGroup vrg) {
    LocalDateTime inFifteenMinutes = LocalDateTime.now().plusMinutes(15);

    Reservation reservation = new Reservation();
    reservation.setStartDateTime(inFifteenMinutes);

    LocalDateTime reservationEnd = reservation.getStartDateTime().plus(DEFAULT_RESERVATON_DURATION);
    reservation.setEndDateTime(reservationEnd);

    VirtualPort sourcePort = Iterables.get(vrg.getVirtualPorts(), 0, null);
    VirtualPort destPort = Iterables.get(vrg.getVirtualPorts(), 1, null);

    reservation.setSourcePort(sourcePort);
    reservation.setDestinationPort(destPort);

    if (destPort != null && sourcePort != null) {
      reservation.setBandwidth(Math.min(sourcePort.getMaxBandwidth(), destPort.getMaxBandwidth()) / 2);
    }

    return reservation;
  }

}
