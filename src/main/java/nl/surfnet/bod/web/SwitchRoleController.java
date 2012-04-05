package nl.surfnet.bod.web;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/switchrole")
public class SwitchRoleController {

  @RequestMapping(method = RequestMethod.POST)
  public String switchRole(final String roleId, final Model uiModel) {
    RichUserDetails userDetails = Security.getUserDetails();

    if (StringUtils.hasText(roleId)) {
      BodRole bodRole = userDetails.findBodRole(Long.valueOf(roleId));
      userDetails.switchRoleTo(bodRole);
    }
    
    return userDetails.getSelectedRole().getRoleName();
  }
}
