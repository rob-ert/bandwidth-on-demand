package nl.surfnet.bod.web;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/switchrole")
public class SwitchRoleController {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private Environment environment;

  @RequestMapping(method = RequestMethod.POST)
  public String switchRole(final String roleId, final Model uiModel) {
    RichUserDetails userDetails = Security.getUserDetails();

    if (StringUtils.hasText(roleId)) {
      userDetails.switchRoleById(Long.valueOf(roleId));
    }

    return userDetails.getSelectedRole().getRole().getViewName();
  }

  @RequestMapping(value = "logout", method = RequestMethod.GET)
  public String logout(HttpServletRequest request) {
    logger.info("Logging out user: {}", Security.getUserDetails().getUsername());
    request.getSession().invalidate();

    return "redirect:" + environment.getShibbolethLogoutUrl();
  }
}
