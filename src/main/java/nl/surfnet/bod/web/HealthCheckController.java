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

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import nl.surfnet.bod.idd.IddClient;
import nl.surfnet.bod.nbi.NbiClient;

@Controller
public class HealthCheckController {

  @Resource
  private IddClient iddClient;

  @Resource
  private NbiClient nbiClient;

  @RequestMapping(value = "/healthcheck")
  public String index(Model model) {

    boolean iddHealth = isServiceHealty(new ServiceCheck() {
      @Override
      public boolean healty() {
        return iddClient.getKlanten().size() > 0;
      }
    });

    boolean nbiHealth = isServiceHealty(new ServiceCheck() {
      @Override
      public boolean healty() {
        return nbiClient.getPhysicalPortsCount() > 0;
      }
    });

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
    boolean healty();
  }
}
