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

import static nl.surfnet.bod.web.manager.VirtualPortController.MODEL_KEY_LIST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.support.*;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class VirtualPortControllerTest {

  @InjectMocks
  private VirtualPortController subject;

  @Mock
  private VirtualPortService virtualPortServiceMock;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;

  @SuppressWarnings("unchecked")
  @Test
  public void listShouldFindEntries() {
    ModelStub model = new ModelStub();

    when(virtualPortServiceMock.findEntries(0, WebUtils.MAX_ITEMS_PER_PAGE)).thenReturn(
        Lists.newArrayList(new VirtualPortFactory().create()));

    subject.list(1, model);

    assertThat(model.asMap(), hasKey(MODEL_KEY_LIST));
    assertThat(model.asMap(), hasKey(WebUtils.MAX_PAGES_KEY));

    assertThat((Collection<VirtualPort>) model.asMap().get(MODEL_KEY_LIST), hasSize(1));
  }

  @Test
  public void populatePhysicalResourceGroupShouldFilterOutEmptyGroups() {
    RichUserDetails user = new RichUserDetailsFactory().create();
    Security.setUserDetails(user);

    PhysicalResourceGroup groupWithPorts = new PhysicalResourceGroupFactory().addPhysicalPort(
        new PhysicalPortFactory().create()).create();
    PhysicalResourceGroup groupWithoutPorts = new PhysicalResourceGroupFactory().create();

    when(physicalResourceGroupServiceMock.findAllForManager(user)).thenReturn(
        Lists.newArrayList(groupWithPorts, groupWithoutPorts));

    Collection<PhysicalResourceGroup> groups = subject.populatePhysicalResourceGroups();

    assertThat(groups, hasSize(1));
    assertThat(groups, contains(groupWithPorts));
  }

}
