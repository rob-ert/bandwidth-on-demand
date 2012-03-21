package nl.surfnet.bod.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;

import nl.surfnet.bod.web.view.ReservationFilterView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import com.google.common.collect.Lists;

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

    ArrayList<Double> list = Lists.newArrayList(new Double("2011"), new Double("2012"));

    List<ReservationFilterView> filterViews = subject.create(list);
    assertThat(filterViews, hasSize(2));
    assertThat(filterViews, containsInAnyOrder(filter2011, filter2012));
  }

  @Test
  public void testCreateCommingPeriodFilter() {
    ReservationFilterView commingPeriodFilter = subject.create(ReservationFilterViewFactory.COMMING);

    assertThat(commingPeriodFilter.getId(), is(ReservationFilterViewFactory.COMMING));
  }

  @Test
  public void testCreateElapsedPeriodFilter() {
    ReservationFilterView elapsedPeriodFilter = subject.create(ReservationFilterViewFactory.ELAPSED);

    assertThat(elapsedPeriodFilter.getId(), is(ReservationFilterViewFactory.ELAPSED));
  }
}
