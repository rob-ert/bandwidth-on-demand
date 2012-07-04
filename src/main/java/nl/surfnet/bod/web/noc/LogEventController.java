package nl.surfnet.bod.web.noc;

import java.util.List;

import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.service.LogEventService;
import nl.surfnet.bod.web.base.AbstractSortableListController;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.Lists;

@Controller
@RequestMapping(value = "/noc/" + LogEventController.PAGE_URL)
public class LogEventController extends AbstractSortableListController<LogEvent> {
  public static final String PAGE_URL = "logevents";
  static final String MODEL_KEY = "list";

  @Autowired
  LogEventService logEventService;

  @Override
  protected String getDefaultSortProperty() {
    return "created";
  }

  @Override
  protected Direction getDefaultSortOrder() {
    return Direction.DESC;
  }

  @Override
  protected String listUrl() {
    return "noc/logevents";
  }

  @Override
  protected List<LogEvent> list(int firstPage, int maxItems, Sort sort, Model model) {
    List<LogEvent> logEvents = Lists.newArrayList();

    if (Security.isSelectedNocRole()) {
      logEvents = logEventService.findAll(firstPage, maxItems, sort);
    }
    return logEvents;
  }

  @Override
  protected long count() {
    return logEventService.count();
  }
}
