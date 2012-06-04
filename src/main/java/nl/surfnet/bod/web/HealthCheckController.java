package nl.surfnet.bod.web;

import nl.surfnet.bod.idd.IddClient;
import nl.surfnet.bod.nbi.NbiClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HealthCheckController {

  @Autowired
  private IddClient iddClient;

  @Autowired
  private NbiClient nbiClient;

  @RequestMapping(value = "/healthcheck")
  public String index(Model model) {

    model.addAttribute("iddHealth", iddClient.getKlanten().size() > 0);
    model.addAttribute("nbiHealth", nbiClient.getPhysicalPortsCount() > 0);

    return "healthcheck";
  }
}
