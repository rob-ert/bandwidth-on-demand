package nl.surfnet.bod.web;

import static nl.surfnet.bod.web.WebUtils.CREATE;

import static nl.surfnet.bod.web.WebUtils.*;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.service.VirtualPortService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/manager/" + VirtualPortController.PAGE_URL)
@Controller
public class VirtualPortController {

  static final String PAGE_URL = "virtualports";

  static final String MODEL_KEY = "virtualPort";
  static final String MODEL_KEY_LIST = MODEL_KEY+LIST_POSTFIX;
  

  @Autowired
  private VirtualPortService virtualPortService;

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
    uiModel.addAttribute(MODEL_KEY_LIST,
        virtualPortService.findEntries(calculateFirstPage(page), MAX_ITEMS_PER_PAGE));

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
      @RequestParam(value =PAGE_KEY, required = false) final Integer page, final Model uiModel) {

    VirtualPort virtualPort = virtualPortService.find(id);
    virtualPortService.delete(virtualPort);

    uiModel.asMap().clear();
    uiModel.addAttribute(PAGE_KEY,(page == null) ? "1" : page.toString());

    return "redirect:";
  }

  /**
   * Puts all {@link VirtualPort}s on the model, needed to relate a group to a
   * {@link VirtualPort}.
   * 
   * @return Collection<PhysicalResourceGroup>
   */
  @ModelAttribute(MODEL_KEY_LIST)
  public Collection<VirtualPort> populatevirtualPorts() {
    return virtualPortService.findAll();
  }

  protected void setVirtualPortService(VirtualPortService virtualPortService) {
    this.virtualPortService = virtualPortService;
  }
}
