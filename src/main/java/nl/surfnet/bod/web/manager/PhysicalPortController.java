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
package nl.surfnet.bod.web.manager;

import static nl.surfnet.bod.web.WebUtils.*;

import java.util.Collection;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

@Controller("managerPhysicalPortController")
@RequestMapping("/manager/physicalports")
public class PhysicalPortController {

  @Autowired
  private PhysicalPortService physicalPortService;

  @Autowired
  private VirtualPortService virtualPortService;

  @Autowired
  private InstituteService instituteService;

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {
    Collection<PhysicalPortView> ports = findPortsForUser(page);

    uiModel.addAttribute("physicalPorts", ports);

    uiModel.addAttribute(MAX_PAGES_KEY,
        calculateMaxPages(physicalPortService.countAllocatedForUser(Security.getUserDetails())));

    return "manager/physicalports/list";
  }

  private Collection<PhysicalPortView> findPortsForUser(Integer page) {
    Collection<PhysicalPort> ports = physicalPortService.findAllocatedEntriesForUser(Security.getUserDetails(),
        calculateFirstPage(page), MAX_ITEMS_PER_PAGE);

    return Collections2.transform(ports, new Function<PhysicalPort, PhysicalPortView>() {
      @Override
      public PhysicalPortView apply(PhysicalPort port) {
        instituteService.fillInstituteForPhysicalResourceGroup(port.getPhysicalResourceGroup());
        Collection<VirtualPort> virtualPorts = virtualPortService.findAllForPhysicalPort(port);
        return new PhysicalPortView(port, virtualPorts);
      }
    });
  }

  public class PhysicalPortView {
    private final Long id;
    private final String name;
    private final String displayName;
    private final PhysicalResourceGroup physicalResourceGroup;
    private final Collection<VirtualPort> virtualPorts;

    public PhysicalPortView(PhysicalPort port, Collection<VirtualPort> virtualPorts) {
      this.id = port.getId();
      this.name = port.getName();
      this.displayName = port.getDisplayName();
      this.physicalResourceGroup = port.getPhysicalResourceGroup();
      this.virtualPorts = virtualPorts;
    }

    public Integer getNumberOfVirtualPorts() {
      return virtualPorts.size();
    }

    public String getName() {
      return name;
    }

    public String getDisplayName() {
      return displayName;
    }

    public PhysicalResourceGroup getPhysicalResourceGroup() {
      return physicalResourceGroup;
    }

    public Long getId() {
      return id;
    }

  }
}
