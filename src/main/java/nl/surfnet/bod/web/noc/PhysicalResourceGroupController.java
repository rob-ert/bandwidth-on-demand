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

import static nl.surfnet.bod.web.WebUtils.*;

import java.util.Collection;
import java.util.Collections;

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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Lists;

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
      final Model model) {

    physicalResourceGroupValidator.validate(physicalResourceGroup, bindingResult);
    if (bindingResult.hasErrors()) {
      model.addAttribute(MODEL_KEY, physicalResourceGroup);
      return PAGE_URL + CREATE;
    }

    model.asMap().clear();
    physicalResourceGroupService.save(physicalResourceGroup);
    ActivationEmailLink<PhysicalResourceGroup> activationLink = physicalResourceGroupService
        .sendAndPersistActivationRequest(physicalResourceGroup);

    // TODO send email with activation Link...

    // Do not return to the create instance, but to the list view
    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = CREATE, method = RequestMethod.GET)
  public String createForm(final Model model) {
    model.addAttribute(MODEL_KEY, new PhysicalResourceGroup());

    return PAGE_URL + CREATE;
  }

  @RequestMapping(params = ID_KEY, method = RequestMethod.GET)
  public String show(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, physicalResourceGroupService.find(id));

    return PAGE_URL + SHOW;
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model model) {
    model.addAttribute(MODEL_KEY_LIST,
        physicalResourceGroupService.findEntries(calculateFirstPage(page), MAX_ITEMS_PER_PAGE));

    model.addAttribute(MAX_PAGES_KEY, calculateMaxPages(physicalResourceGroupService.count()));

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
  public String update(@Valid final PhysicalResourceGroup physicalResourceGroup, final BindingResult result,
      final RedirectAttributes model) {

    if (!Security.managerMayEdit(physicalResourceGroup)) {
      return "redirect:" + PAGE_URL;
    }

    physicalResourceGroupValidator.validate(physicalResourceGroup, result);

    if (result.hasErrors()) {
      model.addAttribute(MODEL_KEY, physicalResourceGroup);
      return PAGE_URL + UPDATE;
    }

    model.asMap().clear();

    if (managerMailHasChanged(physicalResourceGroup)) {
      physicalResourceGroupService.sendAndPersistActivationRequest(physicalResourceGroup);
      addInfoMessage(model,
          String.format("A new activation email has been sent to '%s'", physicalResourceGroup.getManagerEmail()));
    }

    physicalResourceGroupService.update(physicalResourceGroup);

    return "redirect:" + PAGE_URL;
  }

  private void addInfoMessage(RedirectAttributes model, String message) {
    model.addFlashAttribute("infoMessages", Lists.newArrayList(message));
  }

  private boolean managerMailHasChanged(PhysicalResourceGroup group) {
    PhysicalResourceGroup original = physicalResourceGroupService.find(group.getId());

    return original.getManagerEmail() == null || !original.getManagerEmail().equals(group.getManagerEmail());
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

  protected void setPhysicalResourceGroupValidator(PhysicalResourceGroupValidator physicalResourceGroupValidator) {
    this.physicalResourceGroupValidator = physicalResourceGroupValidator;
  }
}
