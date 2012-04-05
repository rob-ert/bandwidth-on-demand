package nl.surfnet.bod.web;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SwitchRoleController {
  private final static String MODEL_KEY = "roleId";

  @RequestMapping(method = RequestMethod.POST)
  public void switchRole(final BindingResult bindingResult, final Model uiModel) {
    RichUserDetails userDetails = Security.getUserDetails();
    Long bodRoleId = WebUtils.getAttributeFromModel(MODEL_KEY, uiModel);

    BodRole bodRole = userDetails.findBodRole(bodRoleId);
    userDetails.switchRoleTo(bodRole);
  }
}
