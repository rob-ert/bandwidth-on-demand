package nl.surfnet.bod.web.noc;

import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.web.view.NocStatisticsView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller("nocDashboardController")
@RequestMapping("/noc")
public class DashboardController {

  @Autowired
  private PhysicalPortService physicalPortService;

  @Autowired
  private ReservationService reservationService;

  @RequestMapping(method = RequestMethod.GET)
  public String index(Model model) {
    
    model.addAttribute("stats", determineStatistics());

    return "noc/index";
  }

  NocStatisticsView determineStatistics() {
    ReservationFilterViewFactory reservationFilterViewFactory = new ReservationFilterViewFactory();

    long countPhysicalPorts = physicalPortService.countAllocated();

    long countElapsedReservations = reservationService.countAllEntriesUsingFilter(reservationFilterViewFactory
        .create(ReservationFilterViewFactory.ELAPSED));

    long countActiveReservations = reservationService.countAllEntriesUsingFilter(reservationFilterViewFactory
        .create(ReservationFilterViewFactory.ACTIVE));

    long countComingReservations = reservationService.countAllEntriesUsingFilter(reservationFilterViewFactory
        .create(ReservationFilterViewFactory.COMING));

    return new NocStatisticsView(countPhysicalPorts, countElapsedReservations, countActiveReservations,
        countComingReservations);
  }
}
