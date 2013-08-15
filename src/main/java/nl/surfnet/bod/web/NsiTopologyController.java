package nl.surfnet.bod.web;

import javax.annotation.Resource;

import nl.surfnet.bod.nsi.NsiConstants;
import nl.surfnet.bod.util.Environment;
import org.joda.time.DateTime;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/nsi-topology")
public class NsiTopologyController {

  @Resource(name = "bodEnvironment") private Environment environment;

  @RequestMapping(method = RequestMethod.GET)
  public String renderTopology(final Model model){

    model.addAttribute("nsiId", NsiConstants.URN_PROVIDER_NSA);
    model.addAttribute("networkName", NsiConstants.NETWORK_ID);
    model.addAttribute("version", DateTime.now().toString());
    model.addAttribute("nsi2ConnectionProviderUrl", getNsi2ConnectionProviderUrl());
    model.addAttribute("nsiTopologyContact", environment.getNsiTopologyContact());
    return "nsi-topology";
  }

  /**
   *
   * @return the URL of the soap-service that we run
   */
  private String getNsi2ConnectionProviderUrl() {
    return environment.getExternalBodUrl() + environment.getNsiV2ServiceUrl();
  }

}
