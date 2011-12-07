package nl.surfnet.bod.web;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import static nl.surfnet.bod.web.WebUtils.*;

@RequestMapping("/noc/physicalresourcegroups")
@Controller
public class PhysicalResourceGroupController {

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @RequestMapping(method = RequestMethod.POST)
  public String create(@Valid final PhysicalResourceGroup physicalResourceGroup, final BindingResult bindingResult,
      final Model uiModel, final HttpServletRequest httpServletRequest) {

    if (bindingResult.hasErrors()) {
      uiModel.addAttribute("physicalResourceGroup", physicalResourceGroup);
      return "physicalresourcegroups/"+ CREATE;
    }

    uiModel.asMap().clear();
    physicalResourceGroupService.save(physicalResourceGroup);

    // Do not return to the create instance, but to the list view
    return "redirect:physicalresourcegroups";
  }

  @RequestMapping(value = CREATE, method = RequestMethod.GET)
  public String createForm(final Model uiModel) {
    uiModel.addAttribute("physicalResourceGroup", new PhysicalResourceGroup());

    return "physicalresourcegroups/"+CREATE;
  }

  @RequestMapping(params = "id", method = RequestMethod.GET)
  public String show(@RequestParam("id") final Long id, final Model uiModel) {
    uiModel.addAttribute("physicalresourcegroup", physicalResourceGroupService.find(id));
    
    return "physicalresourcegroups/"+SHOW;
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = "page", required = false) final Integer page, final Model uiModel) {
    uiModel.addAttribute("physicalresourcegroups",
        physicalResourceGroupService.findEntries(calculateFirstPage(page), MAX_ITEMS_PER_PAGE));

    uiModel.addAttribute("maxPages", calculateMaxPages(physicalResourceGroupService.count()));

    return "physicalresourcegroups/"+LIST;
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid final PhysicalResourceGroup physicalResourceGroup, final BindingResult bindingResult,
      final Model uiModel, final HttpServletRequest httpServletRequest) {

    if (bindingResult.hasErrors()) {
      uiModel.addAttribute("physicalResourceGroup", physicalResourceGroup);
      return "physicalresourcegroups/"+UPDATE;
    }
    uiModel.asMap().clear();
    physicalResourceGroupService.update(physicalResourceGroup);

    return "redirect:physicalresourcegroups";
  }

  @RequestMapping(value = EDIT, params = "id", method = RequestMethod.GET)
  public String updateForm(@RequestParam("id") final Long id, final Model uiModel) {
    uiModel.addAttribute("physicalResourceGroup", physicalResourceGroupService.find(id));

    return "physicalresourcegroups/"+UPDATE;
  }

  @RequestMapping(value = DELETE, params = "id", method = RequestMethod.DELETE)
  public String delete(@RequestParam("id") final Long id,
      @RequestParam(value = "page", required = false) final Integer page,
      final Model uiModel) {

    PhysicalResourceGroup physicalResourceGroup = physicalResourceGroupService.find(id);
    physicalResourceGroupService.delete(physicalResourceGroup);

    uiModel.asMap().clear();

    uiModel.addAttribute("page", (page == null) ? "1" : page.toString());

    return "redirect:";
  }

  protected void setPhysicalResourceGroupService(PhysicalResourceGroupService physicalResourceGroupService) {
    this.physicalResourceGroupService = physicalResourceGroupService;
  }
}
