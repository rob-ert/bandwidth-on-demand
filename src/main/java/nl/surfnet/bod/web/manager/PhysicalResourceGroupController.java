package nl.surfnet.bod.web.manager;

import static nl.surfnet.bod.web.WebUtils.*;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller("managerPhysicalResourceGroupController")
@RequestMapping("/manager/physicalresourcegroups")
public class PhysicalResourceGroupController {

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {

    RichUserDetails user = Security.getUserDetails();

    uiModel.addAttribute("physicalResourceGroups", physicalResourceGroupService.findEntriesForManager(
        user, calculateFirstPage(page), MAX_ITEMS_PER_PAGE));

    uiModel.addAttribute(MAX_PAGES_KEY, calculateMaxPages(physicalResourceGroupService.countForManager(user)));

    return "manager/physicalresourcegroups/list";
  }
}
