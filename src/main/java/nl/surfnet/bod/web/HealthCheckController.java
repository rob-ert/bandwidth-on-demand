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

    boolean iddHealth = isServiceHealty(new ServiceCheck() {
      @Override
      public boolean healty() {
        return iddClient.getKlanten().size() > 0;
      }});

    boolean nbiHealth = isServiceHealty(new ServiceCheck() {
      @Override
      public boolean healty() {
        return nbiClient.getPhysicalPortsCount() > 0;
      }});

    model.addAttribute("iddHealth", iddHealth);
    model.addAttribute("nbiHealth", nbiHealth);

    return "healthcheck";
  }

  public boolean isServiceHealty(ServiceCheck check) {
    try {
      return check.healty();
    }
    catch (Exception e) {
      return false;
    }
  }

  interface ServiceCheck {
    public boolean healty();
  }
}
