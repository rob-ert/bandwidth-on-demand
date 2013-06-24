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

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ReservationFilterView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;

import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReservationControllerTest {

  @InjectMocks
  private ReservationController subject;

  @Mock
  private ReservationService reservationServiceMock;

  @Mock
  private ReservationFilterViewFactory reservationFilterViewFactoryMock;

  private final RichUserDetails manager = new RichUserDetailsFactory().create();

  private final ReservationFilterView filter = new ReservationFilterViewFactory()
      .create(ReservationFilterViewFactory.COMING);

  @Before
  public void login() {
    Security.setUserDetails(manager);
  }

  @Test
  public void listReservationsForManager() {
    ModelStub model = new ModelStub();

    Reservation reservation = new ReservationFactory().create();

    when(reservationFilterViewFactoryMock.create(filter.getId())).thenReturn(filter);

    when(
        reservationServiceMock.findEntriesForManagerUsingFilter(eq(manager), eq(filter), eq(0),
            eq(WebUtils.MAX_ITEMS_PER_PAGE), any(Sort.class))).thenReturn(Lists.newArrayList(reservation));

    subject.list(0, "id", "asc", model);

    assertThat(model.asMap(), hasKey("list"));
  }
}
