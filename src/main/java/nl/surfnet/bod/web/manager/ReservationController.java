package nl.surfnet.bod.web.manager;

import static nl.surfnet.bod.web.WebUtils.*;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ReservationView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller("managerReservationController")
@RequestMapping("/manager/reservations")
public class ReservationController {

  @Autowired
  private ReservationService reservationService;

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model model) {
    RichUserDetails manager = Security.getUserDetails();

    model.addAttribute("reservations",
        reservationService.findEntriesForManager(manager, calculateFirstPage(page), MAX_ITEMS_PER_PAGE));

    model.addAttribute(MAX_PAGES_KEY, calculateMaxPages(reservationService.countForManager(manager)));

    return "manager/reservations/list";
  }

  @RequestMapping(params = ID_KEY, method = RequestMethod.GET)
  public String show(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
    uiModel.addAttribute("reservation", new ReservationView(reservationService.find(id)));

    return "manager/reservations/show";
  }
}
