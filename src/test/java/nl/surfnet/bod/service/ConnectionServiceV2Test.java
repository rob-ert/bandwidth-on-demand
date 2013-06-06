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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.support.ConnectionV2Factory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceV2Test {

  @InjectMocks
  private ConnectionServiceV2 subject = new ConnectionServiceV2();

  @Mock private ConnectionV2Repo connectionRepo;

  @Test
  public void querySummarySync_should_return_an_empty_list_when_no_connection_objects_where_found() {
    final String nonExistingConnectionId = "1";

    when(connectionRepo.findByConnectionId(nonExistingConnectionId)).thenReturn(null);

    List<ConnectionV2> connections = subject.querySummarySync(ImmutableList.of(nonExistingConnectionId), Collections.<String>emptyList(), "requesterNsa");

    assertThat(connections, hasSize(0));
  }

  @Test
  public void querySummarySync_should_query_by_connection_id() {
    ConnectionV2 connection = new ConnectionV2Factory().create();
    when(connectionRepo.findByConnectionId("connectionId")).thenReturn(connection);

    List<ConnectionV2> connections = subject.querySummarySync(ImmutableList.of("connectionId"), Collections.<String>emptyList(), "requesterNsa");

    assertThat(connections, contains(connection));
  }

  @Test
  public void querySummarySync_should_query_by_global_reservation_id() {
    ConnectionV2 connection = new ConnectionV2Factory().create();
    when(connectionRepo.findByGlobalReservationId("globalReservationId")).thenReturn(connection);

    List<ConnectionV2> connections = subject.querySummarySync(Collections.<String>emptyList(), ImmutableList.of("globalReservationId"), "requesterNsa");

    assertThat(connections, contains(connection));
  }

  @Test
  public void querySummarySync_should_query_by_requester_nsa_when_both_global_reservation_id_and_connection_id_are_empty() {
    ConnectionV2 connection = new ConnectionV2Factory().create();
    when(connectionRepo.findByRequesterNsa("requesterNsa")).thenReturn(ImmutableList.of(connection));

    List<ConnectionV2> connections = subject.querySummarySync(Collections.<String>emptyList(), Collections.<String>emptyList(), "requesterNsa");

    assertThat(connections, contains(connection));
  }
}
