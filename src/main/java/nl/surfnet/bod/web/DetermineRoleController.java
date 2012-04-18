package nl.surfnet.bod.web;

import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.security.Security.RoleEnum;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/")
@Controller
public class DetermineRoleController {

  @RequestMapping(method = RequestMethod.GET)
  public String index() {

    if (Security.isSelectedNocRole()) {
      return RoleEnum.NOC_ENGINEER.getViewName();
    }

    if (Security.isSelectedManagerRole()) {
      return RoleEnum.ICT_MANAGER.getViewName();
    }

    return RoleEnum.USER.getViewName();
  }
}
