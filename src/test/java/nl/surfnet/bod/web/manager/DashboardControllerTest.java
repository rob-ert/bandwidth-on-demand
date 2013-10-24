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
package nl.surfnet.bod.web.manager;

import java.util.Collection;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPortCreateRequestLink;
import nl.surfnet.bod.service.LogEventService;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortCreateRequestLinkFactory;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DashboardControllerTest {

  @InjectMocks
  private DashboardController subject;

  @Mock private PhysicalResourceGroupService physicalResourceGroupServiceMock;
  @Mock private PhysicalPortService physicalPortServiceMock;
  @Mock private VirtualPortService virtualPortServiceMock;
  @Mock private ReservationService reservationServiceMock;
  @Mock private LogEventService logEventServiceMock;

  @SuppressWarnings("unchecked")
  @Test
  public void shouldAddPrgAndRequestLinkToModel() {
    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();
    VirtualPortCreateRequestLink requestLink = new VirtualPortCreateRequestLinkFactory().create();
    RichUserDetails user = new RichUserDetailsFactory().addManagerRole(physicalResourceGroup).create();

    Security.setUserDetails(user);
    Security.switchToManager(physicalResourceGroup);

    RedirectAttributes model = new ModelStub();

    when(physicalResourceGroupServiceMock.find(physicalResourceGroup.getId())).thenReturn(physicalResourceGroup);
    when(virtualPortServiceMock.findPendingCreateRequests(physicalResourceGroup)).thenReturn(ImmutableList.of(requestLink));

    String page = subject.index(model);

    assertThat((PhysicalResourceGroup) WebUtils.getAttributeFromModel("prg", model), is(physicalResourceGroup));
    assertThat((Collection<VirtualPortCreateRequestLink>) WebUtils.getAttributeFromModel("createRequests", model), contains(requestLink));
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
  public void shouldDetermineStatistics() {
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