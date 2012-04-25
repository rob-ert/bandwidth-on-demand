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
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.view.ReservationFilterView;
import nl.surfnet.bod.web.view.ReservationView;

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
  private final List<ReservationView> reservationViewsFor2012 = Lists.newArrayList();

  private final Integer page = 0;
  private final Model model = new ModelStub();
  private Integer size;

  @Before
  public void setUp() {

    List<Reservation> reservations = Lists.newArrayList();
    for (int i = 0; i <= WebUtils.MAX_ITEMS_PER_PAGE; i++) {
      Reservation reservation = new ReservationFactory().create();
      reservations.add(reservation);
    }

    reservationsFor2012.addAll(reservations);
    size = new Integer(reservationsFor2012.size());

    reservationViewsFor2012.addAll(Lists.transform(reservationsFor2012, ReservationController.TO_RESERVATION_VIEW));

    when(reservationFilterViewFactoryMock.create(filter2012.getId())).thenReturn(filter2012);

    when(reservationServiceMock.countAllEntriesUsingFilter(filter2012)).thenReturn((long) reservationsFor2012.size());

    when(
        reservationServiceMock.findAllEntriesUsingFilter(any(ReservationFilterView.class), anyInt(), anyInt(),
            any(Sort.class))).thenReturn(reservationsFor2012);
  }

  @Test
  public void shouldHaveMaxPageOnModel() {
    subject.list(page, "id", "asc", filter2012.getId(), model);

    assertThat((Integer) model.asMap().get("maxPages"),
        is(Integer.valueOf(WebUtils.calculateMaxPages(size.longValue()))));
  }

  @Test
  public void defaultListViewShouldRedirectToDefaultFilterView() {
    String viewName = subject.list(page, "id", "asc", model);

    assertThat(viewName, is("redirect:/noc/reservations/filter/" + ReservationFilterViewFactory.COMMING));
  }

  @Test
  public void filteredViewShouldRedirectToListView() {
    String viewName = subject.list(page, "id", "asc", filter2012.getId(), model);

    assertThat(viewName, is(subject.listUrl()));
  }
}
