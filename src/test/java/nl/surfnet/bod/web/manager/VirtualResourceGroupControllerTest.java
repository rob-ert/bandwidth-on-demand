/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.web.manager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.WebUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class VirtualResourceGroupControllerTest {

  @InjectMocks
  private VirtualResourceGroupController subject;

  @Mock
  private VirtualResourceGroupService virtualResourceGroupServiceMock;

  @Test
  public void listShouldFindEntries() {
    ModelStub model = new ModelStub();

    when(virtualResourceGroupServiceMock.findEntries(0, WebUtils.MAX_ITEMS_PER_PAGE)).thenReturn(
        Lists.newArrayList(new VirtualResourceGroupFactory().create()));

    subject.list(1, model);

    assertThat(model.asMap(), hasKey("virtualResourceGroupList"));
    assertThat(model.asMap(), hasKey("maxPages"));

    assertThat((Collection<VirtualResourceGroup>) model.asMap().get("virtualResourceGroupList"), hasSize(1));
  }
}
