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

import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.transform;
import static nl.surfnet.bod.web.WebUtils.*;

import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.domain.validator.VirtualPortValidator;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Function;
import com.google.common.base.Strings;

@Controller("managerVirtualPortController")
@RequestMapping("/manager/" + VirtualPortController.PAGE_URL)
public class VirtualPortController {
  static final String PAGE_URL = "virtualports";

  public static final String MODEL_KEY = "virtualPort";
  public static final String MODEL_KEY_LIST = MODEL_KEY + LIST_POSTFIX;

  @Autowired
  private VirtualPortService virtualPortService;

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Autowired
  private VirtualResourceGroupService virtualResourceGroupService;

  @Autowired
  private PhysicalPortService physicalPortService;

  @Autowired
  private VirtualPortValidator virtualPortValidator;

  @ModelAttribute("virtualResourceGroups")
  public Collection<VirtualResourceGroup> populateVirtualResourceGroups() {
    return virtualResourceGroupService.findAll();
  }

  @ModelAttribute("physicalResourceGroups")
  public Collection<PhysicalResourceGroup> populatePhysicalResourceGroups() {
    return physicalResourceGroupService.findAllForUser(Security.getUserDetails());
  }

  @ModelAttribute("physicalPorts")
  public Collection<PhysicalPort> populatePhysicalPorts(HttpServletRequest request) {
    String physicalResourceGroup = Strings.nullToEmpty(request.getParameter("physicalResourceGroup"));
    if (physicalResourceGroup.isEmpty()) {
      Collection<PhysicalResourceGroup> groups = physicalResourceGroupService.findAllForUser(Security.getUserDetails());

      return getFirst(transform(groups, new Function<PhysicalResourceGroup, Collection<PhysicalPort>>() {
        @Override
        public Collection<PhysicalPort> apply(PhysicalResourceGroup port) {
          return port.getPhysicalPorts();
        }
      }), Collections.<PhysicalPort> emptyList());
    }
    else {
      return physicalResourceGroupService.find(Long.valueOf(physicalResourceGroup)).getPhysicalPorts();
    }
  }

  @RequestMapping(method = RequestMethod.POST)
  public String create(@Valid VirtualPort virtualPort, final BindingResult bindingResult, final Model uiModel,
      final HttpServletRequest httpServletRequest) {

    virtualPortValidator.validate(virtualPort, bindingResult);

    if (bindingResult.hasErrors()) {
      uiModel.addAttribute(MODEL_KEY, virtualPort);

      return PAGE_URL + CREATE;
    }

    uiModel.asMap().clear();
    virtualPortService.save(virtualPort);

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = CREATE, method = RequestMethod.GET)
  public String createForm(@RequestParam(value = "port", required = false) final Long physicalPortId,
      final Model uiModel) {

    VirtualPort virtualPort = new VirtualPort();
    if (physicalPortId != null) {
      PhysicalPort port = physicalPortService.find(physicalPortId);
      virtualPort.setPhysicalPort(port);
    }

    uiModel.addAttribute(MODEL_KEY, virtualPort);

    return PAGE_URL + CREATE;
  }

  @RequestMapping(params = ID_KEY, method = RequestMethod.GET)
  public String show(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, virtualPortService.find(id));

    return PAGE_URL + SHOW;
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY_LIST, virtualPortService.findEntries(calculateFirstPage(page), MAX_ITEMS_PER_PAGE));

    uiModel.addAttribute(MAX_PAGES_KEY, calculateMaxPages(virtualPortService.count()));

    return "manager/virtualports/list";
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid final VirtualPort virtualPort, final BindingResult bindingResult, final Model uiModel,
      final HttpServletRequest httpServletRequest) {

    virtualPortValidator.validate(virtualPort, bindingResult);
    if (bindingResult.hasErrors()) {
      uiModel.addAttribute(MODEL_KEY, virtualPort);
      return PAGE_URL + UPDATE;
    }

    uiModel.asMap().clear();
    virtualPortService.update(virtualPort);

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = EDIT, params = ID_KEY, method = RequestMethod.GET)
  public String updateForm(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
    VirtualPort virtualPort = virtualPortService.find(id);

    uiModel.addAttribute(MODEL_KEY, virtualPort);
    uiModel.addAttribute("physicalPorts", virtualPort.getPhysicalResourceGroup().getPhysicalPorts());

    return PAGE_URL + UPDATE;
  }

  @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
  public String delete(@RequestParam(ID_KEY) final Long id,
      @RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {

    VirtualPort virtualPort = virtualPortService.find(id);
    virtualPortService.delete(virtualPort);

    uiModel.asMap().clear();
    uiModel.addAttribute(PAGE_KEY, (page == null) ? "1" : page.toString());

    return "redirect:";
  }
}
