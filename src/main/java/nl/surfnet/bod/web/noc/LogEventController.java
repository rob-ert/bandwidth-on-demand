package nl.surfnet.bod.web.noc;

import java.util.List;

import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.service.LogEventService;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Lists;

@Controller
@RequestMapping(value = "/noc/" + LogEventController.PAGE_URL)
public class LogEventController {
  public static final String PAGE_URL = "logevents";
  static final String MODEL_KEY = "list";

  @Autowired
  LogEventService logEventService;

  @RequestMapping(method = RequestMethod.GET)
  public String list(Model model) {
    List<LogEvent> events = Lists.newArrayList();

    if (Security.isSelectedNocRole()) {
      events = logEventService.findAllOrderedByCreatedAndUserId();
    }

    model.addAttribute(MODEL_KEY, events);
    return "noc/logevents";
  }
}
