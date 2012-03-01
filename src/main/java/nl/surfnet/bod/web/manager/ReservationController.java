package nl.surfnet.bod.web.manager;

import static nl.surfnet.bod.web.WebUtils.ID_KEY;

import java.util.List;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.web.AbstractSortableListController;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ReservationView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.ImmutableList;

@Controller("managerReservationController")
@RequestMapping("/manager/reservations")
public class ReservationController extends AbstractSortableListController<Reservation> {

  @Autowired
  private ReservationService reservationService;

  @Override
  protected List<Reservation> list(int firstPage, int maxItems, Sort sort) {
    return reservationService.findEntriesForManager(Security.getUserDetails(), firstPage, maxItems, sort);
  }

  @Override
  public long count() {
    return reservationService.countForManager(Security.getUserDetails());
  }

  @Override
  public String defaultSortProperty() {
    return "startDateTime";
  }

  @Override
  public String listUrl() {
    return "manager/reservations/list";
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

  @RequestMapping(params = ID_KEY, method = RequestMethod.GET)
  public String show(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
    uiModel.addAttribute("reservation", new ReservationView(reservationService.find(id)));

    return "manager/reservations/show";
  }

}
