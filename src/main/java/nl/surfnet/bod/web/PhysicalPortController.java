package nl.surfnet.bod.web;

import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;
import static nl.surfnet.bod.web.WebUtils.calculateMaxPages;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/noc/physicalports")
@Controller
public class PhysicalPortController {

  @Autowired
  private PhysicalPortService physicalPortService;

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @RequestMapping(method = RequestMethod.POST)
  public String create(@Valid PhysicalPort physicalPort, final BindingResult bindingResult, final Model uiModel,
      final HttpServletRequest httpServletRequest) {

    if (bindingResult.hasErrors()) {
      uiModel.addAttribute("physicalPort", physicalPort);
      return "physicalresourcegroups/create";
    }

    PhysicalResourceGroup newPhysicalResourceGroup = physicalPort.getPhysicalResourceGroup();

    // Ignore changes made by user, fetch again and set group
    physicalPort = physicalPortService.findByName(physicalPort.getName());
    physicalPort.setPhysicalResourceGroup(newPhysicalResourceGroup);

    uiModel.asMap().clear();
    physicalPortService.save(physicalPort);

    return "redirect:physicalports";
  }

  @RequestMapping(params = "id", method = RequestMethod.GET)
  public String show(@RequestParam("id") final String name, final Model uiModel) {
    uiModel.addAttribute("physicalPort", physicalPortService.findByName(name));
    uiModel.addAttribute("itemId", name);
    return "physicalports/show";
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = "page", required = false) final Integer page, final Model uiModel) {
    uiModel.addAttribute("physicalports", physicalPortService.findEntries(calculateFirstPage(page), MAX_ITEMS_PER_PAGE));

    uiModel.addAttribute("maxPages", calculateMaxPages(physicalPortService.count()));

    return "physicalports/list";
  }

  @RequestMapping(value = "/free", method = RequestMethod.GET)
  public String listUnallocated(@RequestParam(value = "page", required = false) final Integer page, final Model uiModel) {
    uiModel.addAttribute("physicalports",
        physicalPortService.findUnallocatedEntries(calculateFirstPage(page), MAX_ITEMS_PER_PAGE));

    uiModel.addAttribute("maxPages", calculateMaxPages(physicalPortService.countUnallocated()));

    return "physicalports/listunallocated";
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid final PhysicalPort physicalPort, final BindingResult bindingResult,
      @ModelAttribute("physicalResourceGroup") final PhysicalResourceGroup physicalResourceGroup,
      final BindingResult physicalResourceGroupBindingResult, final Model uiModel,
      final HttpServletRequest httpServletRequest) {

    physicalPortService.update(physicalPort);
    uiModel.asMap().clear();

    return "redirect:physicalports";
  }

  @RequestMapping(value = "/edit", params = "id", method = RequestMethod.GET)
  public String updateForm(@RequestParam("id") final String portId, final Model uiModel) {
    uiModel.addAttribute("physicalPort", physicalPortService.findByName(portId));
    return "physicalports/update";
  }

  @RequestMapping(value = "/delete", params = "id", method = RequestMethod.DELETE)
  public String delete(@RequestParam("id") final String name,
      @RequestParam(value = "page", required = false) final Integer page,
      final Model uiModel) {

    PhysicalPort physicalPort = physicalPortService.findByName(name);
    physicalPortService.delete(physicalPort);

    uiModel.asMap().clear();
    uiModel.addAttribute("page", (page == null) ? "1" : page.toString());

    return "redirect:";
  }

  /**
   * Puts all {@link PhysicalResourceGroup}s on the model, needed to relate a
   * group to a {@link PhysicalPort}.
   *
   * @return Collection<PhysicalResourceGroup>
   */
  @ModelAttribute("physicalresourcegroups")
  public Collection<PhysicalResourceGroup> populatePhysicalResourceGroups() {
    return physicalResourceGroupService.findAll();
  }

  protected void setPhysicalPortService(PhysicalPortService physicalPortService) {
    this.physicalPortService = physicalPortService;
  }
}
