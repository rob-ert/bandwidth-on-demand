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
package nl.surfnet.bod.web.noc;

import static nl.surfnet.bod.web.WebUtils.CREATE;
import static nl.surfnet.bod.web.WebUtils.DELETE;
import static nl.surfnet.bod.web.WebUtils.EDIT;
import static nl.surfnet.bod.web.WebUtils.ID_KEY;
import static nl.surfnet.bod.web.WebUtils.LIST;
import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
import static nl.surfnet.bod.web.WebUtils.MAX_PAGES_KEY;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
import static nl.surfnet.bod.web.WebUtils.SHOW;
import static nl.surfnet.bod.web.WebUtils.UPDATE;
import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;
import static nl.surfnet.bod.web.WebUtils.calculateMaxPages;

import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.validator.PhysicalResourceGroupValidator;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("nocPhysicalResourceGroupController")
@RequestMapping("/noc/" + PhysicalResourceGroupController.PAGE_URL)
public class PhysicalResourceGroupController {

  static final String PAGE_URL = "physicalresourcegroups";
  static final String MODEL_KEY = "physicalResourceGroup";
  static final String MODEL_KEY_LIST = MODEL_KEY + WebUtils.LIST_POSTFIX;

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Autowired
  private PhysicalResourceGroupValidator physicalResourceGroupValidator;

  @RequestMapping(method = RequestMethod.POST)
  public String create(@Valid final PhysicalResourceGroup physicalResourceGroup, final BindingResult bindingResult,
      final Model uiModel, final HttpServletRequest httpServletRequest) {

    physicalResourceGroupValidator.validate(physicalResourceGroup, bindingResult);
    if (bindingResult.hasErrors()) {
      uiModel.addAttribute(MODEL_KEY, physicalResourceGroup);
      return PAGE_URL + CREATE;
    }

    uiModel.asMap().clear();
    physicalResourceGroupService.save(physicalResourceGroup);
    ActivationEmailLink<PhysicalResourceGroup> activationLink = physicalResourceGroupService
        .sendAndPersistActivationRequest(physicalResourceGroup, Security.getUserDetails().getEmail());

    // TODO send email with activation Link...

    // Do not return to the create instance, but to the list view
    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = CREATE, method = RequestMethod.GET)
  public String createForm(final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, new PhysicalResourceGroup());

    return PAGE_URL + CREATE;
  }

  @RequestMapping(params = ID_KEY, method = RequestMethod.GET)
  public String show(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, physicalResourceGroupService.find(id));

    return PAGE_URL + SHOW;
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY_LIST,
        physicalResourceGroupService.findEntries(calculateFirstPage(page), MAX_ITEMS_PER_PAGE));

    uiModel.addAttribute(MAX_PAGES_KEY, calculateMaxPages(physicalResourceGroupService.count()));

    return PAGE_URL + LIST;
  }

  @RequestMapping(value = "/{id}/ports", method = RequestMethod.GET, headers = "accept=application/json")
  @ResponseBody
  public Collection<PhysicalPort> listPortsJson(@PathVariable Long id) {
    PhysicalResourceGroup group = physicalResourceGroupService.find(id);

    if (group == null) {
      return Collections.emptyList();
    }

    return group.getPhysicalPorts();
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid final PhysicalResourceGroup physicalResourceGroup, final BindingResult bindingResult,
      final Model uiModel, final HttpServletRequest httpServletRequest) {

    physicalResourceGroupValidator.validate(physicalResourceGroup, bindingResult);
    if (bindingResult.hasErrors()) {
      uiModel.addAttribute(MODEL_KEY, physicalResourceGroup);
      return PAGE_URL + UPDATE;
    }
    uiModel.asMap().clear();
    physicalResourceGroupService.update(physicalResourceGroup);

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = EDIT, params = ID_KEY, method = RequestMethod.GET)
  public String updateForm(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, physicalResourceGroupService.find(id));

    return PAGE_URL + UPDATE;
  }

  @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
  public String delete(@RequestParam(ID_KEY) final Long id,
      @RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {

    PhysicalResourceGroup physicalResourceGroup = physicalResourceGroupService.find(id);
    physicalResourceGroupService.delete(physicalResourceGroup);

    uiModel.asMap().clear();

    uiModel.addAttribute(PAGE_KEY, (page == null) ? "1" : page.toString());

    return "redirect:";
  }
}
