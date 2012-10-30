package nl.surfnet.bod.web.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/advanced")
public class AdvancedController {

  @RequestMapping
  public String index() {
    return "advanced";
  }
}
