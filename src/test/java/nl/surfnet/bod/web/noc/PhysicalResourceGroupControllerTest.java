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
package nl.surfnet.bod.web.noc;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static nl.surfnet.bod.web.WebUtils.MAX_PAGES_KEY;
import static nl.surfnet.bod.web.noc.PhysicalResourceGroupController.MODEL_KEY_LIST;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.web.noc.PhysicalResourceGroupController;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.Model;

import com.google.common.collect.Lists;

public class PhysicalResourceGroupControllerTest {

  private PhysicalResourceGroupController subject;

  private PhysicalResourceGroupService physicalResourceGroupServiceMock;

  @Before
  public void initController() {
    subject = new PhysicalResourceGroupController();
    physicalResourceGroupServiceMock = mock(PhysicalResourceGroupService.class);
    subject.setPhysicalResourceGroupService(physicalResourceGroupServiceMock);
  }

  @Test
  public void listShouldSetGroupsAndMaxPages() {
    Model model = new ModelStub();
    List<PhysicalResourceGroup> groups = Lists.newArrayList(new PhysicalResourceGroupFactory().create());
    when(physicalResourceGroupServiceMock.findEntries(eq(0), anyInt())).thenReturn(groups);

    subject.list(1, model);

    assertThat(model.asMap(), hasEntry(MODEL_KEY_LIST, Object.class.cast(groups)));
    assertThat(model.asMap(), hasEntry(MAX_PAGES_KEY, Object.class.cast(1)));
  }

}
