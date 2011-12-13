package nl.surfnet.bod.web;

import static nl.surfnet.bod.web.WebUtils.CREATE;
import static nl.surfnet.bod.web.WebUtils.DELETE;
import static nl.surfnet.bod.web.WebUtils.EDIT;
import static nl.surfnet.bod.web.WebUtils.ICON_ITEM_KEY;
import static nl.surfnet.bod.web.WebUtils.ID_KEY;
import static nl.surfnet.bod.web.WebUtils.LIST;
import static nl.surfnet.bod.web.WebUtils.LIST_POSTFIX;
import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
import static nl.surfnet.bod.web.WebUtils.MAX_PAGES_KEY;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
import static nl.surfnet.bod.web.WebUtils.SHOW;
import static nl.surfnet.bod.web.WebUtils.UPDATE;
import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;
import static nl.surfnet.bod.web.WebUtils.calculateMaxPages;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.util.UserContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

@SessionAttributes({ "userContext" })
@RequestMapping(VirtualPortController.PAGE_URL_PREFIX + VirtualPortController.PAGE_URL)
@Controller
public class VirtualPortController {
  public static final String PAGE_URL_PREFIX = "/manager/";
  static final String PAGE_URL = "virtualports";

  static final String MODEL_KEY = "virtualPort";
  static final String MODEL_KEY_LIST = MODEL_KEY + LIST_POSTFIX;

  @Autowired
  private VirtualPortService virtualPortService;
  
  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @RequestMapping(method = RequestMethod.POST)
  public String create(@Valid VirtualPort virtualPort, final BindingResult bindingResult, final Model uiModel,
      final HttpServletRequest httpServletRequest) {

    if (bindingResult.hasErrors()) {
      uiModel.addAttribute(MODEL_KEY, virtualPort);
      return PAGE_URL + CREATE;
    }

    uiModel.asMap().clear();
    virtualPortService.save(virtualPort);

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = CREATE, method = RequestMethod.GET)
  public String createForm(final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, new VirtualPort());

    return PAGE_URL + CREATE;
  }

  @RequestMapping(params = ID_KEY, method = RequestMethod.GET)
  public String show(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, virtualPortService.find(id));
    // Needed for the default icons
    uiModel.addAttribute(ICON_ITEM_KEY, id);

    return PAGE_URL + SHOW;
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY_LIST, virtualPortService.findEntries(calculateFirstPage(page), MAX_ITEMS_PER_PAGE));

    uiModel.addAttribute(MAX_PAGES_KEY, calculateMaxPages(virtualPortService.count()));

    return PAGE_URL + LIST;
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid final VirtualPort virtualPort, final BindingResult bindingResult, final Model uiModel,
      final HttpServletRequest httpServletRequest) {

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
    uiModel.addAttribute(MODEL_KEY, virtualPortService.find(id));
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

  /**
   * Puts all {@link PhysicalResourceGroup}s related to the user on the model.
   * NOW just selects the first.
   * 
   * @param userContext
   *          {@link UserContext}
   * 
   * @return {@link PhysicalResourceGroup}
   * 
   * @throws {@link IllegalStateException} in case multiple groups are found.
   */
  @ModelAttribute(PhysicalResourceGroupController.MODEL_KEY)
  public PhysicalResourceGroup populatePhysicalResourceGroups(@ModelAttribute UserContext userContext) {

    Collection<PhysicalResourceGroup> findAllForUser = physicalResourceGroupService.findAllForUser(userContext
        .getNameId());

    //TODO View should be able to handle list
    if (findAllForUser.size() > 1) {
      throw new IllegalStateException("User [" + userContext + "] has more then one PhysicalResourceGroups: "
          + findAllForUser);
    }

    return findAllForUser.iterator().next();
  }

 
  /**
   * Setter to enable depedency injection from testcases.
   * 
   * @param virtualPortService
   *          {@link VirtualPortService}
   */
  protected void setVirtualPortService(VirtualPortService virtualPortService) {
    this.virtualPortService = virtualPortService;
  }
}
