/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.web.user;

import static nl.surfnet.bod.util.Orderings.vpUserLabelOrdering;
import static nl.surfnet.bod.util.Orderings.vrgNameOrdering;
import static nl.surfnet.bod.web.WebUtils.*;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

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

import org.joda.time.DateTime;
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

@RequestMapping(ReservationController.PAGE_URL)
@Controller(value = "userReservationController")
public class ReservationController extends AbstractFilteredReservationController {

  static final String PAGE_URL = "reservations";
  static final String MODEL_KEY = "reservation";

  @Resource
  private VirtualResourceGroupService virtualResourceGroupService;

  @Resource
  private MessageSource messageSource;

  private final ReservationValidator reservationValidator = new ReservationValidator();

  @RequestMapping(method = RequestMethod.POST)
  public String create(@Valid Reservation reservation, BindingResult bindingResult, Model model,
      RedirectAttributes redirectAttributes) {

    reservation.setUserCreated(Security.getUserDetails().getNameId());

    reservationValidator.validate(reservation, bindingResult);

    if (bindingResult.hasErrors()) {
      model.addAttribute(MODEL_KEY, reservation);
      model.addAttribute("virtualResourceGroups", findVirtualResourceGroups());
      model.addAttribute("virtualPorts", vpUserLabelOrdering().sortedCopy(
          reservation.getVirtualResourceGroup().getVirtualPorts()));

      return PAGE_URL + CREATE;
    }

    getReservationService().create(reservation);

    WebUtils.addInfoFlashMessage(redirectAttributes, messageSource, "info_reservation_created", reservation.getName(),
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
    model.addAttribute("virtualPorts", vpUserLabelOrdering().sortedCopy(
        defaultReservation.getVirtualResourceGroup().getVirtualPorts()));

    return PAGE_URL + CREATE;
  }

  @Override
  public String listUrl() {
    return PAGE_URL + LIST;
  }

  @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
  public String delete(@RequestParam(ID_KEY) Long id, Model model, RedirectAttributes redirectAttributes) {

    Reservation reservation = getReservationService().find(id);

    getReservationService().cancel(reservation, Security.getUserDetails());

    // Response is ignored, in js related to link
    return "index";
  }


  @RequestMapping(value = EDIT, params = ID_KEY, method = RequestMethod.GET)
  public String copyReservation(@RequestParam(ID_KEY) Long id, Model model) {

    final Reservation originalReservation = getReservationService().find(id);

    final Reservation reservation = new Reservation();
    reservation.setBandwidth(originalReservation.getBandwidth());
    reservation.setDestinationPort(originalReservation.getDestinationPort());
    reservation.setEndDate(originalReservation.getEndDate());
    reservation.setEndDateTime(originalReservation.getEndDateTime());
    reservation.setName(originalReservation.getName());
    reservation.setProtectionType(originalReservation.getProtectionType());
    reservation.setSourcePort(originalReservation.getSourcePort());
    reservation.setStartDate(originalReservation.getStartDate());
    reservation.setStartDateTime(originalReservation.getStartDateTime());
    reservation.setDestinationPort(originalReservation.getDestinationPort());
    reservation.setVirtualResourceGroup(originalReservation.getVirtualResourceGroup());

    model.addAttribute(MODEL_KEY, reservation);
    model.addAttribute("virtualPorts", vpUserLabelOrdering().sortedCopy(
        reservation.getVirtualResourceGroup().getVirtualPorts()));
    model.addAttribute("virtualResourceGroups", findVirtualResourceGroups());
    model.addAttribute("destinationPort", originalReservation.getDestinationPort());
    model.addAttribute("sourcePort", originalReservation.getSourcePort());

    return PAGE_URL + CREATE;
  }

  @Override
  protected List<ReservationView> list(int firstPage, int maxItems, Sort sort, Model model) {
    ReservationFilterView filter = WebUtils.getAttributeFromModel(FILTER_SELECT, model);

    return transformToView(
        getReservationService().findEntriesForUserUsingFilter(Security.getUserDetails(), filter, firstPage, maxItems,
            sort), Security.getUserDetails());
  }

  @Override
  protected long count(Model model) {
    ReservationFilterView filter = WebUtils.getAttributeFromModel(FILTER_SELECT, model);
    return getReservationService().countForFilterAndUser(Security.getUserDetails(), filter);
  }

  @Override
  protected List<Long> getIdsOfAllAllowedEntries(Model model) {
    ReservationFilterView filter = WebUtils.getAttributeFromModel(FILTER_SELECT, model);
    return getReservationService().findIdsForUserUsingFilter(Security.getUserDetails(), filter);
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
    DateTime inFifteenMinutes = DateTime.now().plusMinutes(15);

    Reservation reservation = new Reservation();
    reservation.setStartDateTime(inFifteenMinutes);

    DateTime reservationEnd = reservation.getStartDateTime().plus(WebUtils.DEFAULT_RESERVATON_DURATION);
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
