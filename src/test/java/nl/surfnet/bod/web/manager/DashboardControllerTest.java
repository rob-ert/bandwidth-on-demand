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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.support.*;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ManagerStatisticsView;
import nl.surfnet.bod.web.view.ReservationFilterView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

@RunWith(MockitoJUnitRunner.class)
public class DashboardControllerTest {

  @InjectMocks
  private DashboardController subject;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;

  @Mock
  private PhysicalPortService physicalPortServiceMock;

  @Mock
  private VirtualPortService virtualPortServiceMock;

  @Mock
  private ReservationService reservationServiceMock;

  @Test
  public void shouldAddPrgAndRequestLinkToModel() {
    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();
    VirtualPortRequestLink requestLink = new VirtualPortRequestLinkFactory().create();
    RichUserDetails user = new RichUserDetailsFactory().addManagerRole(physicalResourceGroup).create();

    Security.setUserDetails(user);
    Security.switchToManager(physicalResourceGroup);

    RedirectAttributes model = new ModelStub();

    when(physicalResourceGroupServiceMock.find(physicalResourceGroup.getId())).thenReturn(physicalResourceGroup);
    when(virtualPortServiceMock.findPendingRequests(physicalResourceGroup)).thenReturn(ImmutableList.of(requestLink));

    String page = subject.index(model);

    assertThat((PhysicalResourceGroup) WebUtils.getAttributeFromModel("prg", model), is(physicalResourceGroup));
    assertThat((Collection<VirtualPortRequestLink>) WebUtils.getAttributeFromModel("requests", model),
        contains(requestLink));
    assertThat(WebUtils.getAttributeFromModel("stats", model), notNullValue());
    assertThat(page, is("manager/index"));
  }

  @Test
  public void shouldAddNullPrgToModel() {
    RichUserDetails user = new RichUserDetailsFactory().addUserRole().create();

    Security.setUserDetails(user);

    RedirectAttributes model = new ModelStub();

    String page = subject.index(model);

    assertThat(WebUtils.getAttributeFromModel("prg", model), nullValue());
    assertThat(WebUtils.getAttributeFromModel("stats", model), nullValue());
    assertThat(page, is("redirect:/"));
  }

  @Test
  public void shouldAddStatisticsToModel() {
    ReservationFilterView elapsedFilter = new ReservationFilterViewFactory()
        .create(ReservationFilterViewFactory.ELAPSED);
    ReservationFilterView activeFilter = new ReservationFilterViewFactory().create(ReservationFilterViewFactory.ACTIVE);
    ReservationFilterView comingFilter = new ReservationFilterViewFactory().create(ReservationFilterViewFactory.COMING);

    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();

    RichUserDetails manager = new RichUserDetailsFactory().addManagerRole(physicalResourceGroup).create();
    Security.setUserDetails(manager);
    Security.switchToManager(physicalResourceGroup);

    when(physicalResourceGroupServiceMock.find(physicalResourceGroup.getId())).thenReturn(physicalResourceGroup);
    when(physicalPortServiceMock.countAllocatedForPhysicalResourceGroup(physicalResourceGroup)).thenReturn(1L);
    when(virtualPortServiceMock.countForManager(Iterables.getOnlyElement(manager.getManagerRoles()))).thenReturn(2L);
    when(reservationServiceMock.countForFilterAndManager(manager, elapsedFilter)).thenReturn(3L);
    when(reservationServiceMock.countForFilterAndManager(manager, activeFilter)).thenReturn(4L);
    when(reservationServiceMock.countForFilterAndManager(manager, comingFilter)).thenReturn(5L);

    ManagerStatisticsView statistics = subject.determineStatistics(manager);
    assertThat(statistics.getPhysicalPortsAmount(), is(1L));
    assertThat(statistics.getVirtualPortsAmount(), is(2L));
    assertThat(statistics.getElapsedReservationsAmount(), is(3L));
    assertThat(statistics.getActiveReservationsAmount(), is(4L));
    assertThat(statistics.getComingReservationsAmount(), is(5L));
  }
}