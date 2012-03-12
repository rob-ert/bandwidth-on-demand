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

import static nl.surfnet.bod.web.WebUtils.CREATE;
import static nl.surfnet.bod.web.WebUtils.DELETE;
import static nl.surfnet.bod.web.WebUtils.ID_KEY;
import static nl.surfnet.bod.web.WebUtils.LIST;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
import static nl.surfnet.bod.web.WebUtils.SHOW;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.domain.validator.ReservationValidator;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ReservationFilterView;
import nl.surfnet.bod.web.view.ReservationView;

import org.joda.time.Hours;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@RequestMapping(ReservationController.PAGE_URL)
@Controller
public class ReservationController extends AbstractSortableListController<ReservationView> {
  public static final ReadablePeriod DEFAULT_RESERVATON_DURATION = Hours.FOUR;
  public static final ReadablePeriod DEFAULT_FILTER_INTERVAL = Months.FOUR;

  static final String PAGE_URL = "reservations";

  static final String MODEL_KEY = "reservation";

  private static final Function<Reservation, ReservationView> TO_RESERVATION_VIEW = new Function<Reservation, ReservationView>() {
    @Override
    public ReservationView apply(Reservation reservation) {
      return new ReservationView(reservation);
    }
  };

  @Autowired
  private ReservationService reservationService;

  @Autowired
  private VirtualResourceGroupService virtualResourceGroupService;

  private ReservationValidator reservationValidator = new ReservationValidator();

  @Autowired
  private MessageSource messageSource;

  @RequestMapping(method = RequestMethod.POST)
  public String create(@Valid Reservation reservation, BindingResult bindingResult, Model model,
      RedirectAttributes redirectAttributes) {
    reservation.setUserCreated(Security.getUserDetails().getNameId());

    reservationValidator.validate(reservation, bindingResult);

    if (bindingResult.hasErrors()) {
      model.addAttribute(MODEL_KEY, reservation);
      model.addAttribute("virtualPorts", reservation.getVirtualResourceGroup().getVirtualPorts());

      return PAGE_URL + CREATE;
    }

    reservationService.create(reservation);

    WebUtils.addInfoMessage(redirectAttributes, "A new reservation for %s has been requested.", reservation
        .getVirtualResourceGroup().getName());

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = CREATE, method = RequestMethod.GET)
  @SuppressWarnings("unchecked")
  public String createForm(final Model model) {

    Collection<VirtualPort> ports = (Collection<VirtualPort>) model.asMap().get("virtualPorts");
    if (CollectionUtils.isEmpty(ports) || ports.size() == 1) {

      model.addAttribute(MessageView.MODEL_KEY,
          MessageView.createInfoMessage(messageSource, "info_reservation_need_two_virtual_ports"));

      return MessageView.PAGE_URL;
    }

    return PAGE_URL + CREATE;
  }

  @RequestMapping(params = ID_KEY, method = RequestMethod.GET)
  public String show(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, new ReservationView(reservationService.find(id)));

    return PAGE_URL + SHOW;
  }

  @Override
  public String listUrl() {
    return PAGE_URL + LIST;
  }

  @Override
  public List<ReservationView> list(int firstPage, int maxItems, Sort sort) {
    return Lists.transform(reservationService.findEntries(firstPage, maxItems, sort), TO_RESERVATION_VIEW);
  }

  @Override
  public long count() {
    return reservationService.count();
  }

  @Override
  public String defaultSortProperty() {
    return "startDateTime";
  }

  @Override
  protected List<String> translateSortProperty(String sortProperty) {
    List<String> sortProperties;
    if (sortProperty.equals("startDateTime")) {
      sortProperties = ImmutableList.of("startDate", "startTime");
    }
    else if (sortProperty.equals("endDateTime")) {
      sortProperties = ImmutableList.of("endDate", "endTime");
    }
    else {
      sortProperties = ImmutableList.of(sortProperty);
    }

    return sortProperties;
  }

  @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
  public String delete(@RequestParam(ID_KEY) Long id, @RequestParam(value = PAGE_KEY, required = false) Integer page,
      RedirectAttributes redirectAttributes) {

    Reservation reservation = reservationService.find(id);

    boolean result = reservationService.cancel(reservation);

    if (result) {
      WebUtils.addInfoMessage(redirectAttributes, "A reservation for %s has been cancelled.", reservation
          .getVirtualResourceGroup().getName());
    }
    else {
      WebUtils.addInfoMessage(redirectAttributes, "A reservation for %s can NOT be cancelled.", reservation
          .getVirtualResourceGroup().getName());
    }

    return "redirect:/" + PAGE_URL;
  }

  @ModelAttribute
  public void populateVirtualPorts(Model model) {
    RichUserDetails user = Security.getUserDetails();

    Collection<VirtualResourceGroup> groups = Collections2.filter(virtualResourceGroupService.findAllForUser(user),
        new Predicate<VirtualResourceGroup>() {
          @Override
          public boolean apply(VirtualResourceGroup group) {
            return !group.getVirtualPorts().isEmpty();
          }
        });

    Collection<VirtualPort> ports = groups.isEmpty() ? Collections.<VirtualPort> emptyList() : groups.iterator().next()
        .getVirtualPorts();

    model.addAttribute("virtualResourceGroups", groups);
    model.addAttribute("virtualPorts", ports);
    model.addAttribute(MODEL_KEY, createDefaultReservation(ports));

  }

  List<nl.surfnet.bod.web.view.ReservationFilterView> createReservationFilters(Model model) {
    List<ReservationFilterView> filterViews = Lists.newArrayList();
    @SuppressWarnings("unchecked")
    List<Reservation> reservations = (List<Reservation>) model.asMap().get(WebUtils.LIST);

    final LocalDateTime now = LocalDateTime.now();
    // Comming period
    filterViews.add(new ReservationFilterView("reservation.filter.comming", now, now.plus(DEFAULT_FILTER_INTERVAL)));
    // Elapsed period
    filterViews.add(new ReservationFilterView("reservation.filter.elapsed", now.minus(DEFAULT_FILTER_INTERVAL), now));

    // Years with reservations
    for (Integer year : getDistinctReservationYears(reservations)) {
      filterViews.add(new ReservationFilterView(year));
    }

    return filterViews;
  }

  List<Integer> getDistinctReservationYears(List<Reservation> reservations) {
    Set<Integer> uniqueYears = Sets.newTreeSet();

    for (Reservation reservation : reservations) {
      uniqueYears.add(reservation.getStartDate().getYear());
      uniqueYears.add(reservation.getEndDate().getYear());
    }

    return Lists.newArrayList(uniqueYears);
  }

  List<Reservation> getReservationsBetweenBasedOnEndDateOnly(LocalDateTime start, LocalDateTime end,
      List<Reservation> reservations) {

    // Interval is exclusive of the end, so add one minute
    Interval interval = new Interval(start.toDate().getTime(), end.toDate().getTime() + 60000);

    List<Reservation> intervalReservations = Lists.newArrayList();

    for (Reservation reservation : reservations) {
      if (interval.contains(reservation.getEndDateTime().toDate().getTime())) {
        intervalReservations.add(reservation);
      }
    }
    return intervalReservations;
  }

  List<Reservation> getReservationsBetweenBasedOnStartDateOrEndDate(LocalDateTime start, LocalDateTime end,
      List<Reservation> reservations) {

    // Interval is exclusive of the end, so add one minute
    Interval interval = new Interval(start.toDate().getTime(), end.toDate().getTime() + 60000);

    List<Reservation> intervalReservations = Lists.newArrayList();
    Interval reservationInterval;
    for (Reservation reservation : reservations) {
      reservationInterval = new Interval(reservation.getStartDateTime().toDate().getTime(), reservation
          .getEndDateTime().toDate().getTime());

      if (interval.overlaps(reservationInterval)) {
        intervalReservations.add(reservation);
      }
    }
    return intervalReservations;
  }

  private Reservation createDefaultReservation(Collection<VirtualPort> ports) {
    LocalDate today = LocalDate.now();
    LocalTime inFifteenMinutes = LocalTime.now().plusMinutes(15);

    Reservation reservation = new Reservation();
    reservation.setStartDate(today);
    reservation.setStartTime(inFifteenMinutes);

    LocalDateTime reservationEnd = reservation.getStartDateTime().plus(DEFAULT_RESERVATON_DURATION);
    reservation.setEndDate(reservationEnd.toLocalDate());
    reservation.setEndTime(reservationEnd.toLocalTime());

    VirtualPort sourcePort = Iterables.get(ports, 0, null);
    VirtualPort destPort = Iterables.get(ports, 1, null);

    reservation.setSourcePort(sourcePort);
    reservation.setDestinationPort(destPort);

    if (destPort != null && sourcePort != null) {
      reservation.setBandwidth(Math.min(sourcePort.getMaxBandwidth(), destPort.getMaxBandwidth()) / 2);
    }

    return reservation;
  }
}
