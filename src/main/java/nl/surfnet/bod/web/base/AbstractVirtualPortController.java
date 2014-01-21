package nl.surfnet.bod.web.base;

import java.util.List;

import javax.annotation.Resource;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import nl.surfnet.bod.domain.NsiVersion;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.view.VirtualPortView;

public abstract class AbstractVirtualPortController extends AbstractSearchableSortableListController<VirtualPortView, VirtualPort> {

  @Resource protected NsiHelper nsiHelper;
  @Resource protected VirtualPortService virtualPortService;

  @Override
  protected List<String> translateSortProperty(String sortProperty) {
    if (sortProperty.equals("physicalResourceGroup")) {
      return ImmutableList.of("physicalPort.physicalResourceGroup");
    }

    return super.translateSortProperty(sortProperty);
  }

  @Override
  protected String searchTranslations(String searchQuery) {
    for (String part : Splitter.on(" ").splitToList(searchQuery)) {
      Optional<String> localId = nsiHelper.parseLocalNsiId(part, NsiVersion.ONE).or(nsiHelper.parseLocalNsiId(part, NsiVersion.TWO));
      if (localId.isPresent()) {
        searchQuery = searchQuery.replace(part, "id:" + localId.get());
      }
    }

    return searchQuery;
  }

  @Override
  protected AbstractFullTextSearchService<VirtualPort> getFullTextSearchableService() {
    return virtualPortService;
  }

}
