package nl.surfnet.bod.web.noc;

import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.VirtualPortView;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.ImmutableList;

@Controller("nocVirtualPortController")
@RequestMapping("/noc/virtualports")
public class VirtualPortController extends AbstractSearchableSortableListController<VirtualPortView, VirtualPort> {

  @Resource
  private VirtualPortService virtualPortService;

  @Override
  protected AbstractFullTextSearchService<VirtualPortView, VirtualPort> getFullTextSearchableService() {
    return virtualPortService;
  }

  @Override
  protected String listUrl() {
    return "/noc/virtualports/list";
  }

  @Override
  protected List<VirtualPortView> list(int firstPage, int maxItems, Sort sort, Model model) {
    List<VirtualPort> entriesForManager = virtualPortService.findEntries(firstPage, maxItems);

    return virtualPortService.transformToView(entriesForManager, Security.getUserDetails());
  }

  @Override
  protected long count() {
    return virtualPortService.count();
  }

  @Override
  protected String getDefaultSortProperty() {
    return "managerLabel";
  }

  @Override
  protected List<String> translateSortProperty(String sortProperty) {
    if (sortProperty.equals("physicalResourceGroup")) {
      return ImmutableList.of("physicalPort.physicalResourceGroup");
    }

    return super.translateSortProperty(sortProperty);
  }
}
