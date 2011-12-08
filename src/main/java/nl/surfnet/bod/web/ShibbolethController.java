package nl.surfnet.bod.web;

import java.util.Collection;

import nl.surfnet.bod.service.GroupService;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.util.UserContext;

import org.opensocial.models.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes({ "userContext" })
@RequestMapping("/shibboleth")
public class ShibbolethController {

  @Autowired
  private GroupService groupService;

  @Autowired
  private Environment env;

  @RequestMapping(value = "/groups", method = RequestMethod.GET)
  public String list(@ModelAttribute("userContext") UserContext userContext, final Model uiModel) {
    Collection<Group> groups = groupService.getGroups(userContext.getNameId());

    uiModel.addAttribute("groups", groups);

    return "shibboleth/groups";
  }

  @RequestMapping("/info")
  public String info() {
    return "shibboleth/info";
  }

  @RequestMapping("/login")
  public String login() {
    return env.getImitateShibboleth() ? "shibboleth/login" : "shibboleth/info";
  }
}
