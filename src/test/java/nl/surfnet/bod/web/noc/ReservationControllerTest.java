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
package nl.surfnet.bod.web.noc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ReservationFilterView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class ReservationControllerTest {

  @Mock
  private ReservationService reservationServiceMock;

  @Mock
  private ReservationFilterViewFactory reservationFilterViewFactoryMock;

  @InjectMocks
  private ReservationController subject;

  private final ReservationFilterView filter2012 = new ReservationFilterViewFactory().create("2012");

  private final List<Reservation> reservationsFor2012 = Lists.newArrayList();

  private final Integer page = 0;
  private final Model model = new ModelStub();
  private Integer size;

  @Before
  public void setUp() {
    Security.setUserDetails(new RichUserDetailsFactory().create());

    List<Reservation> reservations = Lists.newArrayList();
    for (int i = 0; i <= WebUtils.MAX_ITEMS_PER_PAGE; i++) {
      Reservation reservation = new ReservationFactory().create();
      reservations.add(reservation);
    }

    reservationsFor2012.addAll(reservations);
    size = new Integer(reservationsFor2012.size());

    when(reservationFilterViewFactoryMock.create(filter2012.getId())).thenReturn(filter2012);

    when(reservationServiceMock.countAllEntriesUsingFilter(filter2012)).thenReturn((long) reservationsFor2012.size());

    when(
        reservationServiceMock.findAllEntriesUsingFilter(any(ReservationFilterView.class), anyInt(), anyInt(),
            any(Sort.class))).thenReturn(reservationsFor2012);
  }

  @Test
  public void shouldHaveMaxPageOnModel() {
    subject.filter(page, "id", "asc",  filter2012.getId(), model);

    assertThat((Integer) model.asMap().get(WebUtils.MAX_PAGES_KEY),
        is(Integer.valueOf(WebUtils.calculateMaxPages(size.longValue()))));
  }

  @Test
  public void defaultListViewShouldRedirectToDefaultFilterView() {
    String viewName = subject.list(page, "id", "asc", model);

    assertThat(viewName, is(subject.listUrl()));
  }

  @Test
  public void filteredViewShouldRedirectToListView() {
    String viewName = subject.filter(page, "id", "asc", filter2012.getId(), model);

    assertThat(viewName, is(subject.listUrl()));
  }
}
