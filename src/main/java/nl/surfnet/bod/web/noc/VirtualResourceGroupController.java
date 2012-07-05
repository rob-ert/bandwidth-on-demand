package nl.surfnet.bod.web.noc;

import java.util.List;

import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.web.base.AbstractSortableListController;
import nl.surfnet.bod.web.manager.VirtualResourceGroupController.VirtualResourceGroupView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Controller("nocVirtualResourceGroupController")
@RequestMapping("/noc/teams")
public class VirtualResourceGroupController extends AbstractSortableListController<VirtualResourceGroupView> {

  @Autowired
  private VirtualResourceGroupService virtualResourceGroupService;

  private final Function<VirtualResourceGroup, VirtualResourceGroupView> TO_VIEW =
      new Function<VirtualResourceGroup, VirtualResourceGroupView>() {
        @Override
        public VirtualResourceGroupView apply(VirtualResourceGroup input) {
          return new VirtualResourceGroupView(input, input.getVirtualPortCount());
        }
  };

  @Override
  protected String listUrl() {
    return "noc/teams/list";
  }

  @Override
  protected List<VirtualResourceGroupView> list(int firstPage, int maxItems, Sort sort, Model model) {
    return Lists.transform(virtualResourceGroupService.findEntries(firstPage, maxItems, sort), TO_VIEW);
  }

  @Override
  protected long count() {
    return virtualResourceGroupService.count();
  }

}
