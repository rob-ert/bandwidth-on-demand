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
import java.util.List;

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
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Controller("managerVirtualPortController")
@RequestMapping(VirtualPortController.PAGE_URL)
public class VirtualPortController {

  public static final String MODEL_KEY = "virtualPort";
  public static final String MODEL_KEY_LIST = MODEL_KEY + LIST_POSTFIX;
  public static final String PAGE_URL = "/manager/virtualports";

  private static final Function<VirtualPort, VirtualPortView> TO_VIRTUAL_PORT_VIEW =
      new Function<VirtualPort, VirtualPortView>() {
        @Override
        public VirtualPortView apply(VirtualPort port) {
          return new VirtualPortView(port);
        }
      };

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

  @RequestMapping(method = RequestMethod.POST)
  public String create(@Valid VirtualPort virtualPort, BindingResult bindingResult, Model model,
      RedirectAttributes redirectAttributes) {
    virtualPortValidator.validate(virtualPort, bindingResult);

    if (bindingResult.hasErrors()) {
      model.addAttribute(MODEL_KEY, virtualPort);
      model.addAttribute("physicalPorts", virtualPort.getPhysicalResourceGroup().getPhysicalPorts());

      return PAGE_URL + CREATE;
    }

    model.asMap().clear();

    WebUtils.addInfoMessage(redirectAttributes, "Virtual Port '%s' was created.", virtualPort.getManagerLabel());

    virtualPortService.save(virtualPort);

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = CREATE, method = RequestMethod.GET)
  public String createForm(@RequestParam(value = "port", required = false) Long physicalPortId,
      @RequestParam(value = "pgroup", required = false) Long pGroupId,
      @RequestParam(value = "vgroup", required = false) Long vGroupId, final Model model) {

    VirtualPort virtualPort = new VirtualPort();

    if (vGroupId != null) {
      VirtualResourceGroup vGroup = virtualResourceGroupService.find(vGroupId);
      if (vGroup != null) {
        virtualPort.setVirtualResourceGroup(vGroup);
      }
    }

    if (pGroupId != null && physicalPortId == null) {
      PhysicalResourceGroup pGroup = physicalResourceGroupService.find(pGroupId);
      if (pGroup != null && Security.isManagerMemberOf(pGroup) && pGroup.getPhysicalPortCount() > 0) {
        virtualPort.setPhysicalPort(Iterables.get(pGroup.getPhysicalPorts(), 0));
        model.addAttribute("physicalPorts", pGroup.getPhysicalPorts());
      }
    }

    if (physicalPortId != null) {
      PhysicalPort port = physicalPortService.find(physicalPortId);
      if (Security.isManagerMemberOf(port.getPhysicalResourceGroup())) {
        virtualPort.setPhysicalPort(port);
        model.addAttribute("physicalPorts", port.getPhysicalResourceGroup().getPhysicalPorts());
      }
    }

    model.addAttribute(MODEL_KEY, virtualPort);

    return PAGE_URL + CREATE;
  }

  @RequestMapping(params = ID_KEY, method = RequestMethod.GET)
  public String show(@RequestParam(ID_KEY) final Long id, final Model model) {
    model.addAttribute(MODEL_KEY, TO_VIRTUAL_PORT_VIEW.apply(virtualPortService.find(id)));

    return PAGE_URL + SHOW;
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model model) {
    RichUserDetails manager = Security.getUserDetails();

    model.addAttribute(MODEL_KEY_LIST, Lists.transform(
        virtualPortService.findEntriesForManager(manager, calculateFirstPage(page), MAX_ITEMS_PER_PAGE),
        TO_VIRTUAL_PORT_VIEW));

    model.addAttribute(MAX_PAGES_KEY, calculateMaxPages(virtualPortService.countForManager(manager)));

    return PAGE_URL + LIST;
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid VirtualPort virtualPort, BindingResult bindingResult, Model model,
      RedirectAttributes redirectAttributes) {
    if (Security.managerMayNotEdit(virtualPort)) {
      return "redirect:" + PAGE_URL;
    }

    virtualPortValidator.validate(virtualPort, bindingResult);
    if (bindingResult.hasErrors()) {
      model.addAttribute(MODEL_KEY, virtualPort);
      return PAGE_URL + UPDATE;
    }

    model.asMap().clear();
    WebUtils.addInfoMessage(redirectAttributes, "The Virtual Port has been updated.");

    virtualPortService.update(virtualPort);

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = EDIT, params = ID_KEY, method = RequestMethod.GET)
  public String updateForm(@RequestParam(ID_KEY) final Long id, final Model model) {
    VirtualPort virtualPort = virtualPortService.find(id);

    if (virtualPort == null || Security.managerMayNotEdit(virtualPort)) {
      return "redirect:" + PAGE_URL;
    }

    model.addAttribute(MODEL_KEY, virtualPort);
    model.addAttribute("physicalPorts", virtualPort.getPhysicalResourceGroup().getPhysicalPorts());

    return PAGE_URL + UPDATE;
  }

  @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
  public String delete(@RequestParam(ID_KEY) Long id, @RequestParam(value = PAGE_KEY, required = false) Integer page,
      RedirectAttributes redirectAttributes) {

    VirtualPort virtualPort = virtualPortService.find(id);

    if (virtualPort == null || Security.managerMayNotEdit(virtualPort)) {
      return "redirect:" + PAGE_URL;
    }

    virtualPortService.delete(virtualPort);

    WebUtils.addInfoMessage(redirectAttributes, "The virtual port was deleted.");

    return "redirect:" + PAGE_URL;
  }

  @ModelAttribute("virtualResourceGroups")
  public Collection<VirtualResourceGroup> populateVirtualResourceGroups() {
    return virtualResourceGroupService.findAll();
  }

  @ModelAttribute
  public void populatePhysicalResourceGroups(Model model) {
    List<PhysicalResourceGroup> groups = Lists.newArrayList(Collections2.filter(
        physicalResourceGroupService.findAllForManager(Security.getUserDetails()),
        new Predicate<PhysicalResourceGroup>() {
          @Override
          public boolean apply(PhysicalResourceGroup group) {
            return group.getPhysicalPortCount() > 0;
          }
        }));

    Collection<PhysicalPort> ports = getFirst(
        transform(groups, new Function<PhysicalResourceGroup, Collection<PhysicalPort>>() {
          @Override
          public Collection<PhysicalPort> apply(PhysicalResourceGroup port) {
            return port.getPhysicalPorts();
          }
        }), Collections.<PhysicalPort> emptyList());

    model.addAttribute("physicalResourceGroups", groups);
    model.addAttribute("physicalPorts", ports);
  }

  public static final class VirtualPortView {
    private final Long id;
    private final String managerLabel;
    private final Integer maxBandwidth;
    private final Integer vlanId;
    private final String virtualResourceGroup;
    private final String physicalResourceGroup;
    private final String physicalPort;
    private final String userLabel;

    public VirtualPortView(VirtualPort port) {
      id = port.getId();
      managerLabel = port.getManagerLabel();
      userLabel = port.getUserLabel();
      maxBandwidth = port.getMaxBandwidth();
      vlanId = port.getVlanId();
      virtualResourceGroup = port.getVirtualResourceGroup().getName();
      physicalResourceGroup = port.getPhysicalResourceGroup().getName();
      physicalPort = port.getPhysicalPort().getManagerLabel();
    }

    public String getManagerLabel() {
      return managerLabel;
    }

    public Integer getMaxBandwidth() {
      return maxBandwidth;
    }

    public Integer getVlanId() {
      return vlanId;
    }

    public String getVirtualResourceGroup() {
      return virtualResourceGroup;
    }

    public String getPhysicalResourceGroup() {
      return physicalResourceGroup;
    }

    public String getPhysicalPort() {
      return physicalPort;
    }

    public Long getId() {
      return id;
    }

    public String getUserLabel() {
      return userLabel;
    }

  }

}
