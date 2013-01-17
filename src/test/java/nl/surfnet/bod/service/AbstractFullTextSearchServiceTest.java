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
package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.List;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.util.TestFullTextSearchService;

import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class AbstractFullTextSearchServiceTest {

  private final TestFullTextSearchService<Reservation> subject = new TestFullTextSearchService<>();

  @Test
  public void intersectWithOneFilterResult() {
    List<Reservation> searchResult = Lists.newArrayList();
    for (int i = 0; i < 3; i++) {
      searchResult.add(new ReservationFactory().setId((long) i).create());
    }

    List<Long> filterResult = Lists.newArrayList(2L);
    List<Reservation> intersectedResult = subject.intersectFullTextResultAndFilterResult(searchResult, filterResult);

    assertThat(intersectedResult, hasSize(1));
    assertThat(Iterables.getOnlyElement(intersectedResult).getId(), is(2L));
  }

  @Test
  public void intersectMoreFilterResultsAndSearchResults() {
    List<Reservation> searchResult = Lists.newArrayList();
    for (int i = 0; i < 4; i++) {
      searchResult.add(new ReservationFactory().setId((long) i).create());
    }

    List<Long> filterResult = Lists.newArrayList(2L, 3L);

    List<Reservation> intersectedResult = subject.intersectFullTextResultAndFilterResult(searchResult, filterResult);

    assertThat(intersectedResult, hasSize(2));
  }

  @Test
  public void intersectLessFilterResultsAndSearchResults() {
    Reservation reservation1 = new ReservationFactory().create();
    Reservation reservation2 = new ReservationFactory().create();

    List<Reservation> searchResult = Lists.newArrayList(reservation1);
    List<Long> filterResult = Lists.newArrayList(reservation1.getId(), reservation2.getId());

    List<Reservation> intersectedResult = subject.intersectFullTextResultAndFilterResult(searchResult, filterResult);

    assertThat(intersectedResult, hasSize(1));
    assertThat(intersectedResult, contains(reservation1));
  }

  @Test
  public void intersectEmptyFilterResultsAndSearchResults() {
    Reservation reservation = new ReservationFactory().create();

    List<Reservation> searchResult = Lists.newArrayList(reservation);
    List<Long> filterResult = Lists.newArrayList();

    List<Reservation> intersectedResult = subject.intersectFullTextResultAndFilterResult(searchResult, filterResult);

    assertThat(intersectedResult, hasSize(0));
  }

  @Test
  public void intersectFilterResultsAndEmptySearchResults() {
    Reservation reservation = new ReservationFactory().create();

    List<Reservation> searchResult = Lists.newArrayList();
    List<Long> filterResult = Lists.newArrayList(reservation.getId());

    List<Reservation> intersectedResult = subject.intersectFullTextResultAndFilterResult(searchResult, filterResult);

    assertThat(intersectedResult, hasSize(0));
  }

  @Test
  public void shouldMaintainOrderWhenIntersectingASC() {
    Reservation reservation1 = new ReservationFactory().create();
    Reservation reservation2 = new ReservationFactory().create();
    Reservation reservation3 = new ReservationFactory().create();

    List<Reservation> searchResult = Lists.newArrayList(reservation3, reservation2, reservation1);
    List<Long> filterResult = Lists.newArrayList(reservation1.getId(), reservation2.getId());

    List<Reservation> intersectedResult = subject.intersectFullTextResultAndFilterResult(searchResult, filterResult);

    assertThat(intersectedResult, hasSize(2));
    assertThat(intersectedResult, contains(reservation1, reservation2));
  }

  @Test
  public void shouldMaintainOrderWhenIntersectingDESC() {
    Reservation reservation1 = new ReservationFactory().create();
    Reservation reservation2 = new ReservationFactory().create();
    Reservation reservation3 = new ReservationFactory().create();

    List<Reservation> searchResult = Lists.newArrayList(reservation1, reservation2, reservation3);
    List<Long> filterResult = Lists.newArrayList(reservation3.getId(), reservation1.getId());

    List<Reservation> intersectedResult = subject.intersectFullTextResultAndFilterResult(searchResult, filterResult);

    assertThat(intersectedResult, hasSize(2));
    assertThat(intersectedResult, contains(reservation3, reservation1));
  }

  @Test
  public void shouldMaintainOrderWhenIntersectingRandom() {
    Reservation reservation1 = new ReservationFactory().create();
    Reservation reservation2 = new ReservationFactory().create();
    Reservation reservation3 = new ReservationFactory().create();

    List<Reservation> searchResult = Lists.newArrayList(reservation2, reservation3, reservation1);
    List<Long> filterResult = Lists.newArrayList(reservation1.getId(), reservation3.getId());

    List<Reservation> intersectedResult = subject.intersectFullTextResultAndFilterResult(searchResult, filterResult);

    assertThat(intersectedResult, hasSize(2));
    assertThat(intersectedResult, contains(reservation1, reservation3));
  }

}
