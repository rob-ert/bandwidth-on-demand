/**
 * Copyright (c) 2012, SURFnet BV
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
package nl.surfnet.bod.web.noc;

import nl.surfnet.bod.service.LogEventService;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.NocStatisticsView;
import nl.surfnet.bod.web.view.ReservationFilterView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DashboardControllerTest {

  @InjectMocks
  private DashboardController subject;

  @Mock
  private PhysicalPortService physicalPortServiceMock;

  @Mock
  private ReservationService reservationServiceMock;

  @Mock
  private Environment environment;

  @Mock
  private LogEventService logEventServiceMock;

  @Test
  public void shouldAddNullPrgToModel() {
    RichUserDetails user = new RichUserDetailsFactory().addUserRole().create();

    Security.setUserDetails(user);

    RedirectAttributes model = new ModelStub();

    String page = subject.index(model);

    assertThat(WebUtils.getAttributeFromModel("stats", model), notNullValue());
    assertThat(page, is("noc/index"));
  }

  @Test
  public void shouldAddStatisticsToModel() {
    ReservationFilterView elapsedFilter = new ReservationFilterViewFactory()
        .create(ReservationFilterViewFactory.ELAPSED);
    ReservationFilterView activeFilter = new ReservationFilterViewFactory().create(ReservationFilterViewFactory.ACTIVE);
    ReservationFilterView comingFilter = new ReservationFilterViewFactory().create(ReservationFilterViewFactory.COMING);

    RichUserDetails noc = new RichUserDetailsFactory().addNocRole().create();
    Security.setUserDetails(noc);
    Security.switchToNocEngineer();

    when(physicalPortServiceMock.countAllocated()).thenReturn(2L);
    when(reservationServiceMock.countAllEntriesUsingFilter(elapsedFilter)).thenReturn(3L);
    when(reservationServiceMock.countAllEntriesUsingFilter(activeFilter)).thenReturn(4L);
    when(reservationServiceMock.countAllEntriesUsingFilter(comingFilter)).thenReturn(5L);
    when(physicalPortServiceMock.countUnalignedPhysicalPorts()).thenReturn(6L);

    NocStatisticsView statistics = subject.determineStatistics();
    assertThat(statistics.getPhysicalPortsAmount(), is(2L));
    assertThat(statistics.getElapsedReservationsAmount(), is(3L));
    assertThat(statistics.getActiveReservationsAmount(), is(4L));
    assertThat(statistics.getComingReservationsAmount(), is(5L));
    assertThat(statistics.getUnalignedPhysicalPortsAmount(), is(6L));
  }
}
