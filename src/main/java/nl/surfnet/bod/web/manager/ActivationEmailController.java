package nl.surfnet.bod.web.manager;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/manager/activate")
@Controller
public class ActivationEmailController {

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;
  @Autowired
  private InstituteService instituteService;

  @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
  public String activateEmail(@PathVariable String uuid, Model uiModel) {
    ActivationEmailLink link = physicalResourceGroupService.findActivationLink(uuid);

    if (link == null) {
      return "index";
    }
    else if (link.getCreationDateTime().plusDays(5).isBefore(LocalDateTime.now())) {
      return "manager/linkNotValid";
    }

    PhysicalResourceGroup group = link.getPhysicalResourceGroup();

    physicalResourceGroupService.activate(group);
    instituteService.fillInstituteForPhysicalResourceGroup(group);

    uiModel.addAttribute("physicalResourceGroup", group);

    return "manager/emailConfirmed";
  }
}
