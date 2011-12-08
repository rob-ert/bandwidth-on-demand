package nl.surfnet.bod.web;

import static nl.surfnet.bod.web.WebUtils.CREATE;
import static nl.surfnet.bod.web.WebUtils.DELETE;
import static nl.surfnet.bod.web.WebUtils.EDIT;
import static nl.surfnet.bod.web.WebUtils.LIST;
import static nl.surfnet.bod.web.WebUtils.LIST_POSTFIX;
import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
import static nl.surfnet.bod.web.WebUtils.SHOW;
import static nl.surfnet.bod.web.WebUtils.UPDATE;
import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;
import static nl.surfnet.bod.web.WebUtils.calculateMaxPages;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.VirtualResourceGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/manager/"+VirtualResourceGroupController.PAGE_URL)
@Controller
public class VirtualResourceGroupController {

  static final String PAGE_URL = "virtualresourcegroups";
  
  private static final String MODEL_KEY = "virtualResourceGroup";
  private static final String MODEL_KEY_LIST = MODEL_KEY+LIST_POSTFIX;


  @Autowired
  private VirtualResourceGroupService virtualResourceGroupService;

  @RequestMapping(method = RequestMethod.POST)
  public String create(@Valid VirtualResourceGroup virtualResourceGroup, final BindingResult bindingResult,
      final Model uiModel, final HttpServletRequest httpServletRequest) {

    if (bindingResult.hasErrors()) {
      uiModel.addAttribute(MODEL_KEY, virtualResourceGroup);
      return PAGE_URL + CREATE;
    }

    uiModel.asMap().clear();
    virtualResourceGroupService.save(virtualResourceGroup);

    return "redirect:"+PAGE_URL;
  }

  @RequestMapping(value = CREATE, method = RequestMethod.GET)
  public String createForm(final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, new VirtualResourceGroup());

    return PAGE_URL + CREATE;
  }

  @RequestMapping(params = "id", method = RequestMethod.GET)
  public String show(@RequestParam("id") final Long id, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, virtualResourceGroupService.find(id));
    // Needed for the default icons
    uiModel.addAttribute("itemId", id);

    return PAGE_URL + SHOW;
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = "page", required = false) final Integer page, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY_LIST,
        virtualResourceGroupService.findEntries(calculateFirstPage(page), MAX_ITEMS_PER_PAGE));

    uiModel.addAttribute("maxPages", calculateMaxPages(virtualResourceGroupService.count()));

    return PAGE_URL + LIST;
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid final VirtualResourceGroup virtualResourceGroup, final BindingResult bindingResult,
      final Model uiModel, final HttpServletRequest httpServletRequest) {

    if (bindingResult.hasErrors()) {
      uiModel.addAttribute(MODEL_KEY, virtualResourceGroup);
      return PAGE_URL + UPDATE;
    }

    uiModel.asMap().clear();
    virtualResourceGroupService.update(virtualResourceGroup);

    return "redirect:"+PAGE_URL;
  }

  @RequestMapping(value = EDIT, params = "id", method = RequestMethod.GET)
  public String updateForm(@RequestParam("id") final Long id, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, virtualResourceGroupService.find(id));
    return PAGE_URL + UPDATE;
  }

  @RequestMapping(value = DELETE, params = "id", method = RequestMethod.DELETE)
  public String delete(@RequestParam("id") final Long id,
      @RequestParam(value = "page", required = false) final Integer page, final Model uiModel) {

    VirtualResourceGroup virtualResourceGroup = virtualResourceGroupService.find(id);
    virtualResourceGroupService.delete(virtualResourceGroup);

    uiModel.asMap().clear();
    uiModel.addAttribute("page", (page == null) ? "1" : page.toString());

    return "redirect:";
  }

  /**
   * Puts all {@link VirtualResourceGroup}s on the model, needed to relate a
   * group to a {@link VirtualPort}.
   * 
   * @return Collection<PhysicalResourceGroup>
   */
  @ModelAttribute(MODEL_KEY_LIST)
  public Collection<VirtualResourceGroup> populatevirtualResourceGroups() {
    return virtualResourceGroupService.findAll();
  }

  protected void setPhysicalPortService(VirtualResourceGroupService virtualResourceGroupService) {
    this.virtualResourceGroupService = virtualResourceGroupService;
  }
}
