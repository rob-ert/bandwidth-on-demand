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
package nl.surfnet.bod.support;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import com.google.common.collect.Lists;

import nl.surfnet.bod.web.view.ReservationFilterView;

@RunWith(MockitoJUnitRunner.class)
public class ReservationFilterViewFactoryTest {

  @InjectMocks
  ReservationFilterViewFactory subject = new ReservationFilterViewFactory();

  @Mock
  MessageSource messageSource;

  @Test
  public void testCreateYearBasedOnString() {
    ReservationFilterView filterView = subject.create("2012");

    assertThat(filterView.getLabel(), is("2012"));
  }

  @Test
  public void testCreateYearBasedOnListOfDouble() {
    ReservationFilterView filter2011 = subject.create("2011");
    ReservationFilterView filter2012 = subject.create("2012");

    List<Integer> list = Lists.newArrayList(Integer.valueOf("2011"), Integer.valueOf("2012"));

    List<ReservationFilterView> filterViews = subject.create(list);
    assertThat(filterViews, hasSize(2));
    assertThat(filterViews, containsInAnyOrder(filter2011, filter2012));
  }

  @Test
  public void testCreateCommingPeriodFilter() {
    ReservationFilterView commingPeriodFilter = subject.create(ReservationFilterViewFactory.COMING);

    assertThat(commingPeriodFilter.getId(), is(ReservationFilterViewFactory.COMING));
  }

  @Test
  public void testCreateElapsedPeriodFilter() {
    ReservationFilterView elapsedPeriodFilter = subject.create(ReservationFilterViewFactory.ELAPSED);

    assertThat(elapsedPeriodFilter.getId(), is(ReservationFilterViewFactory.ELAPSED));
  }
}
