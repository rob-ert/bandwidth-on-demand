package nl.surfnet.bod.web;

import static nl.surfnet.bod.web.WebUtils.CREATE;
import static nl.surfnet.bod.web.WebUtils.DELETE;
import static nl.surfnet.bod.web.WebUtils.EDIT;
import static nl.surfnet.bod.web.WebUtils.LIST;
import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
import static nl.surfnet.bod.web.WebUtils.SHOW;
import static nl.surfnet.bod.web.WebUtils.UPDATE;
import static nl.surfnet.bod.web.WebUtils.ICON_ITEM_ID;
import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;
import static nl.surfnet.bod.web.WebUtils.calculateMaxPages;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.VirtualPortService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/manager/" + VirtualPortController.PAGE_URL_PART)
@Controller
public class VirtualPortController {

  static final String PAGE_URL_PART = "virtualports/";
  
  private static final String VIRTUAL_PORT_ATTRIB = "virtualport";
  
  private static final String VIRTUAL_PORT_LIST_ATTRIB = VIRTUAL_PORT_ATTRIB+"List";

  @Autowired
  private VirtualPortService virtualPortService;

  @RequestMapping(method = RequestMethod.POST)
  public String create(@Valid VirtualPort virtualPort, final BindingResult bindingResult, final Model uiModel,
      final HttpServletRequest httpServletRequest) {

    if (bindingResult.hasErrors()) {
      uiModel.addAttribute(VIRTUAL_PORT_ATTRIB, virtualPort);
      return PAGE_URL_PART + CREATE;
    }

    uiModel.asMap().clear();
    virtualPortService.save(virtualPort);

    return "redirect:" + PAGE_URL_PART;
  }

  @RequestMapping(value = CREATE, method = RequestMethod.GET)
  public String createForm(final Model uiModel) {
    uiModel.addAttribute(VIRTUAL_PORT_ATTRIB, new VirtualPort());

    return PAGE_URL_PART + CREATE;
  }

  @RequestMapping(params = "id", method = RequestMethod.GET)
  public String show(@RequestParam("id") final Long id, final Model uiModel) {
    uiModel.addAttribute(VIRTUAL_PORT_ATTRIB, virtualPortService.find(id));
    // Needed for the default icons
    uiModel.addAttribute(ICON_ITEM_ID, id);

    return PAGE_URL_PART + SHOW;
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = "page", required = false) final Integer page, final Model uiModel) {
    uiModel.addAttribute(VIRTUAL_PORT_LIST_ATTRIB,
        virtualPortService.findEntries(calculateFirstPage(page), MAX_ITEMS_PER_PAGE));

    uiModel.addAttribute("maxPages", calculateMaxPages(virtualPortService.count()));

    return PAGE_URL_PART + LIST;
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid final VirtualPort virtualPort, final BindingResult bindingResult, final Model uiModel,
      final HttpServletRequest httpServletRequest) {

    if (bindingResult.hasErrors()) {
      uiModel.addAttribute(VIRTUAL_PORT_ATTRIB, virtualPort);
      return PAGE_URL_PART + UPDATE;
    }

    uiModel.asMap().clear();
    virtualPortService.update(virtualPort);

    return "redirect:" + PAGE_URL_PART;
  }

  @RequestMapping(value = EDIT, params = "id", method = RequestMethod.GET)
  public String updateForm(@RequestParam("id") final Long id, final Model uiModel) {
    uiModel.addAttribute(VIRTUAL_PORT_ATTRIB, virtualPortService.find(id));
    return PAGE_URL_PART + UPDATE;
  }

  @RequestMapping(value = DELETE, params = "id", method = RequestMethod.DELETE)
  public String delete(@RequestParam("id") final Long id,
      @RequestParam(value = "page", required = false) final Integer page, final Model uiModel) {

    VirtualPort virtualPort = virtualPortService.find(id);
    virtualPortService.delete(virtualPort);

    uiModel.asMap().clear();
    uiModel.addAttribute("page", (page == null) ? "1" : page.toString());

    return "redirect:";
  }

  /**
   * Puts all {@link VirtualPort}s on the model, needed to relate a
   * group to a {@link VirtualPort}.
   * 
   * @return Collection<PhysicalResourceGroup>
   */
  @ModelAttribute(VIRTUAL_PORT_LIST_ATTRIB)
  public Collection<VirtualPort> populatevirtualPorts() {
    return virtualPortService.findAll();
  }

  protected void setVirtualPortService(VirtualPortService virtualPortService) {
    this.virtualPortService = virtualPortService;
  }
}
