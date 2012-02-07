package nl.surfnet.bod.web;

import nl.surfnet.bod.web.security.Security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/")
@Controller
public class DashboardController {

  @RequestMapping(method = RequestMethod.GET)
  public String index() {
    if (Security.hasUserRole()) {
      return "index";
    }
    if (Security.hasIctManagerRole()) {
      return "redirect:manager";
    }
    if (Security.hasNocEngineerRole()) {
      return "redirect:noc";
    }

    return "noUserRole";
  }

}
