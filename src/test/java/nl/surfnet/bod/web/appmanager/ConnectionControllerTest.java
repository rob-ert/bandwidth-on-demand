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