package nl.surfnet.bod.web.manager;

import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller("managerPhysicalPortController")
@RequestMapping("/manager/physicalports")
public class PhysicalPortController {

  @Autowired
  private PhysicalPortService physicalPortService;

  @RequestMapping(method = RequestMethod.GET)
  public String list(final Model uiModel) {
    uiModel.addAttribute("physicalPorts", physicalPortService.findAllocatedForUser(Security.getUserDetails()));

    return "manager/physicalports/list";
  }

}
