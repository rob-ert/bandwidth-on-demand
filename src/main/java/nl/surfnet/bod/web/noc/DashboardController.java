package nl.surfnet.bod.web.noc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller("nocDashboardController")
@RequestMapping("/noc")
public class DashboardController {

  @RequestMapping(method = RequestMethod.GET)
  public String index(Model model) {

    return "noc/index";
  }
}
