package nl.surfnet.bod.web.manager;

import java.util.Collection;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller("managerDashboardController")
@RequestMapping("/manager")
public class DashboardController {

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @RequestMapping(method = RequestMethod.GET)
  public String index() {
    Collection<PhysicalResourceGroup> groups = physicalResourceGroupService.findAllForManager(Security.getUserDetails());

    for (PhysicalResourceGroup group : groups) {
      if (!group.isActive()) {
        return "redirect:manager/physicalresourcegroups/edit?id=" + group.getId();
      }
    }

    return "index";
  }
}
