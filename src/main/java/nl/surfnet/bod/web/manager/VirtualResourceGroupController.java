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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.domain.validator.VirtualResourceGroupValidator;
import nl.surfnet.bod.service.VirtualResourceGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller("managerVirtualResourceGroupController")
@RequestMapping("/manager/" + VirtualResourceGroupController.PAGE_URL)
public class VirtualResourceGroupController {
  public static final String PAGE_URL = "virtualresourcegroups";

  public static final String MODEL_KEY = "virtualResourceGroup";
  public static final String MODEL_KEY_LIST = MODEL_KEY + LIST_POSTFIX;

  @Autowired
  private VirtualResourceGroupService virtualResourceGroupService;

  @Autowired
  private VirtualResourceGroupValidator virtualResourceGroupValidator;

  /**
   * Creates a {@link VirtualResourceGroup}, validates according to
   * {@link VirtualResourceGroupValidator}
   *
   * @param virtualResourceGroup
   *          {@link VirtualResourceGroup}
   * @param bindingResult
   *          {@link BindingResult}
   * @param uiModel
   *          model
   * @param httpServletRequest
   *          Request
   * @return String viewName
   */
  @RequestMapping(method = RequestMethod.POST)
  public String create(@Valid VirtualResourceGroup virtualResourceGroup, final BindingResult bindingResult,
      final Model uiModel, final HttpServletRequest httpServletRequest) {

    virtualResourceGroupValidator.validate(virtualResourceGroup, bindingResult);
    if (bindingResult.hasErrors()) {
      uiModel.addAttribute(MODEL_KEY, virtualResourceGroup);
      return PAGE_URL + CREATE;
    }

    uiModel.asMap().clear();
    virtualResourceGroupService.save(virtualResourceGroup);

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = CREATE, method = RequestMethod.GET)
  public String createForm(final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, new VirtualResourceGroup());

    return PAGE_URL + CREATE;
  }

  @RequestMapping(params = ID_KEY, method = RequestMethod.GET)
  public String show(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, virtualResourceGroupService.find(id));

    return PAGE_URL + SHOW;
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY_LIST,
        virtualResourceGroupService.findEntries(calculateFirstPage(page), MAX_ITEMS_PER_PAGE));

    uiModel.addAttribute(MAX_PAGES_KEY, calculateMaxPages(virtualResourceGroupService.count()));

    return PAGE_URL + LIST;
  }

  /**
   * Updates an existing {@link VirtualResourceGroup}, validates according to
   * {@link VirtualResourceGroupValidator}
   *
   * @param virtualResourceGroup
   *          {@link VirtualResourceGroup}
   * @param bindingResult
   *          {@link BindingResult}
   * @param uiModel
   *          Model
   * @param httpServletRequest
   *          Request
   * @return String viewName
   */
  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid final VirtualResourceGroup virtualResourceGroup, final BindingResult bindingResult,
      final Model uiModel, final HttpServletRequest httpServletRequest) {

    virtualResourceGroupValidator.validate(virtualResourceGroup, bindingResult);
    if (bindingResult.hasErrors()) {
      uiModel.addAttribute(MODEL_KEY, virtualResourceGroup);
      return PAGE_URL + UPDATE;
    }

    uiModel.asMap().clear();
    virtualResourceGroupService.update(virtualResourceGroup);

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = EDIT, params = ID_KEY, method = RequestMethod.GET)
  public String updateForm(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, virtualResourceGroupService.find(id));
    return PAGE_URL + UPDATE;
  }

  @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
  public String delete(@RequestParam(ID_KEY) final Long id,
      @RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {

    VirtualResourceGroup virtualResourceGroup = virtualResourceGroupService.find(id);
    virtualResourceGroupService.delete(virtualResourceGroup);

    uiModel.asMap().clear();
    uiModel.addAttribute(PAGE_KEY, (page == null) ? "1" : page.toString());

    return "redirect:";
  }

  /**
   * Puts all {@link VirtualResourceGroup}s on the model
   *
   * @return Collection<PhysicalResourceGroup>
   */
  @ModelAttribute(MODEL_KEY_LIST)
  public Collection<VirtualResourceGroup> populatevirtualResourceGroups() {
    return virtualResourceGroupService.findAll();
  }

}
