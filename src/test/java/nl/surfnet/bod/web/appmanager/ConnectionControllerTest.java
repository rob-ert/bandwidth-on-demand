package nl.surfnet.bod.web.appmanager;

import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.service.ConnectionService;
import nl.surfnet.bod.support.ConnectionFactory;
import nl.surfnet.bod.util.FullTextSearchResult;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.web.servlet.MockMvc;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionControllerTest {

  @InjectMocks
  private ConnectionController subject;

  @Mock
  private ConnectionService connectionServiceMock;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
  }

  @Test
  public void listConnectionsShouldAddConnectionToModel() throws Exception {

    when(connectionServiceMock.findEntries(0, MAX_ITEMS_PER_PAGE, new Sort(Direction.ASC, subject.getDefaultSortProperty())))
      .thenReturn(Lists.newArrayList(new ConnectionFactory().create(), new ConnectionFactory().create()));

    mockMvc.perform(get("/appmanager/connections"))
      .andExpect(model().attribute("list", hasSize(2)))
      .andExpect(status().isOk());
  }

  @Test
  public void listConnectionsSortedByReservationStatus() throws Exception {

    when(connectionServiceMock.findEntries(0, MAX_ITEMS_PER_PAGE, new Sort(Direction.ASC, "reservation.status")))
      .thenReturn(Lists.newArrayList(new ConnectionFactory().create(), new ConnectionFactory().create()));

    mockMvc.perform(get("/appmanager/connections").param("sort", "reservationStatus"))
      .andExpect(model().attribute("list", hasSize(2)))
      .andExpect(status().isOk());
  }

  @Test
  public void listConnectionsSearchForStatus() throws Exception {

    ImmutableList<Long> filterList = ImmutableList.of(2L);
    when(connectionServiceMock.findIds(Optional.of(new Sort(Direction.ASC, subject.getDefaultSortProperty()))))
      .thenReturn(filterList);
    when(
      connectionServiceMock.searchForInFilteredList(
        Connection.class, "TERMINATED", 0, MAX_ITEMS_PER_PAGE, Security.getUserDetails(), filterList)
      ).thenReturn(
        new FullTextSearchResult<Connection>(1, Lists.newArrayList(new ConnectionFactory().create())));

    mockMvc.perform(get("/appmanager/connections/search").param("search", "TERMINATED"))
      .andExpect(model().attribute("list", hasSize(1)))
      .andExpect(model().attribute("search", "TERMINATED"))
      .andExpect(status().isOk());
  }

  @Test
  public void listIllegalConnectionsShouldAddConnectionToModel() throws Exception {

    when(connectionServiceMock.findWithIllegalState(0, MAX_ITEMS_PER_PAGE, new Sort(Direction.ASC, subject.getDefaultSortProperty())))
      .thenReturn(Lists.newArrayList(new ConnectionFactory().create(), new ConnectionFactory().create()));

    mockMvc.perform(get("/appmanager/connections/illegal"))
      .andExpect(model().attribute("list", hasSize(2)))
      .andExpect(model().attributeExists("sortProperty", "sortDirection"))
      .andExpect(status().isOk());
  }

}