package nl.surfnet.bod.web.manager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class ReservationControllerTest {

  @InjectMocks
  private ReservationController subject;

  @Mock
  private ReservationService reservationServiceMock;

  private final RichUserDetails manager = new RichUserDetailsFactory().create();

  @Before
  public void login() {
    Security.setUserDetails(manager);
  }

  @Test
  public void listReservationsForManager() {
    ModelStub model = new ModelStub();

    Reservation reservation = new ReservationFactory().create();

    when(
        reservationServiceMock.findEntriesForManager(eq(manager), eq(0), eq(WebUtils.MAX_ITEMS_PER_PAGE),
            any(Sort.class))).thenReturn(Lists.newArrayList(reservation));

    subject.list(null, null, null, model);

    assertThat(model.asMap(), hasKey("list"));

    assertThat(((List<Reservation>) model.asMap().get("list")), contains(reservation));
  }

  @Test
  public void listReservationsSortPropertyShouldBeMappedToTwoProperties() {
    ModelStub model = new ModelStub();

    Reservation reservation = new ReservationFactory().create();
    Sort sort = new Sort("startDate", "startTime");

    when(
        reservationServiceMock.findEntriesForManager(eq(manager), eq(0), eq(WebUtils.MAX_ITEMS_PER_PAGE),
            eq(sort))).thenReturn(Lists.newArrayList(reservation));

    subject.list(null, "startDateTime", null, model);

    assertThat(model.asMap(), hasKey("list"));

    assertThat(((List<Reservation>) model.asMap().get("list")), contains(reservation));
  }

}
