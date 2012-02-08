package nl.surfnet.bod.web.manager;

import java.util.Collection;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Lists;

@Controller("managerDashboardController")
@RequestMapping("/manager")
public class DashboardController {

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @RequestMapping(method = RequestMethod.GET)
  public String index(RedirectAttributes redirectAttributes) {
    Collection<PhysicalResourceGroup> groups = physicalResourceGroupService
        .findAllForManager(Security.getUserDetails());

    for (PhysicalResourceGroup group : groups) {
      if (!group.isActive()) {
        redirectAttributes.addFlashAttribute("infoMessages",
            Lists.newArrayList("Your Physical Resource group is not activated yet, please do so now."));

        return "redirect:manager/physicalresourcegroups/edit?id=" + group.getId();
      }
    }

    return "index";
  }
}
