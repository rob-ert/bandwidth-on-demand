/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.web.base;

import java.util.List;

import javax.annotation.Resource;

import java.util.Optional;
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
      Optional<String> localId = optionalOrFlat(nsiHelper.parseLocalNsiId(part, NsiVersion.ONE), nsiHelper.parseLocalNsiId(part, NsiVersion.TWO));
      if (localId.isPresent()) {
        searchQuery = searchQuery.replace(part, "id:" + localId.get());
      }
    }

    return searchQuery;
  }

  private static<T> Optional<T> optionalOrFlat(Optional<T> first, Optional<T> second) {
    if (first.isPresent()) {
      return first;
    } else {
      return second;
    }
  }

  @Override
  protected AbstractFullTextSearchService<VirtualPort> getFullTextSearchableService() {
    return virtualPortService;
  }

}
