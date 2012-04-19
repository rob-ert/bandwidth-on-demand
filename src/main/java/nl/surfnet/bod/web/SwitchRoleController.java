package nl.surfnet.bod.web;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.manager.ActivationEmailController;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/switchrole")
public class SwitchRoleController {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Autowired
  private Environment environment;

  @Autowired
  private MessageSource messageSource;

  @RequestMapping(method = RequestMethod.POST)
  public String switchRole(final String roleId, final Model uiModel, final RedirectAttributes redirectAttribs) {
    RichUserDetails userDetails = Security.getUserDetails();

    if (StringUtils.hasText(roleId)) {
      userDetails.switchRoleById(Long.valueOf(roleId));
    }

    return determineViewNameAndAddAttributes(userDetails.getSelectedRole(), redirectAttribs);
  }

  @RequestMapping(value = "logout", method = RequestMethod.GET)
  public String logout(HttpServletRequest request) {
    logger.info("Logging out user: {}", Security.getUserDetails().getUsername());
    request.getSession().invalidate();

    return "redirect:" + environment.getShibbolethLogoutUrl();
  }

  private String determineViewNameAndAddAttributes(BodRole selectedRole, RedirectAttributes redirectAttribs) {
    Long groupId = WebUtils.getSelectedPhysicalResourceGroupId();

    if (groupId != null) {
      PhysicalResourceGroup group = physicalResourceGroupService.find(groupId);

      if (!group.isActive()) {
        String successMessage = WebUtils.getMessage(messageSource, "info_activation_request_send", group.getName(),
            group.getManagerEmail());

        WebUtils.addInfoMessage(redirectAttribs,
            createNewActivationLinkForm(new Object[] {
                environment.getExternalBodUrl() + ActivationEmailController.ACTIVATION_MANAGER_PATH,
                group.getId().toString(), successMessage }));

        return "redirect:manager/physicalresourcegroups/edit?id=" + group.getId();
      }
    }
    return selectedRole.getRole().getViewName();
  }

  String createNewActivationLinkForm(Object... args) {
    return String.format(WebUtils.getMessage(messageSource, "info_physicalresourcegroup_not_activated")
        + "<a href=\"%s?id=%s\" class=\"btn btn-primary\" data-form=\"true\" data-success=\"%s\">Resend email</a>",
        args);
  }
}
