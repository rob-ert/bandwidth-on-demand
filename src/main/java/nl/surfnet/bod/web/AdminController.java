package nl.surfnet.bod.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("admin")
public class AdminController {

  @Autowired
  private ReloadableResourceBundleMessageSource messageSource;

  @RequestMapping(value = "refreshMessages", method = RequestMethod.GET)
  public String refreshMessageSource() {
    messageSource.clearCache();

    return "redirect:/";
  }
}
