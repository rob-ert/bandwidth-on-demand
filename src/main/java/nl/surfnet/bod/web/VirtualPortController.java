package nl.surfnet.bod.web;

import static nl.surfnet.bod.web.WebUtils.*;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/virtualports")
public class VirtualPortController {

  @Autowired
  private VirtualPortService virtualPortService;

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {

    uiModel.addAttribute(
        "virtualPorts",
        virtualPortService.findAllEntriesForUser(Security.getUserDetails(), calculateFirstPage(page), MAX_ITEMS_PER_PAGE));
    uiModel.addAttribute(MAX_PAGES_KEY, calculateMaxPages(virtualPortService.count()));

    return "virtualports/list";
  }

}
