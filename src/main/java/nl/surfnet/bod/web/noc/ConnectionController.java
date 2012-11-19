package nl.surfnet.bod.web.noc;

import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.ConnectionService;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/noc/connections")
public class ConnectionController extends AbstractSearchableSortableListController<Connection, Connection> {

  @Resource
  private ConnectionService connectionService;

  @RequestMapping("/illegal")
  public String listIllegal(Model model) {
    model.addAttribute("list", connectionService.findWithIllegalState());
    return listUrl();
  }

  @Override
  protected List<Connection> transformToView(List<Connection> entities, RichUserDetails user) {
    return entities;
  }

  @Override
  protected String listUrl() {
    return "noc/connections/list";
  }

  @Override
  protected List<Connection> list(int firstPage, int maxItems, Sort sort, Model model) {
    return connectionService.findEntries(firstPage, maxItems, sort);
  }

  @Override
  protected long count(Model model) {
    return connectionService.count();
  }

  @Override
  protected List<Long> getIdsOfAllAllowedEntries(Model model) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected AbstractFullTextSearchService<Connection> getFullTextSearchableService() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected String getDefaultSortProperty() {
    return "reservation.startDateTime";
  }
}
