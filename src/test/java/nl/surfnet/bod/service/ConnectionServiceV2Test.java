package nl.surfnet.bod.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.nsi.v2.ConnectionServiceRequesterV2;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.util.Environment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceV2Test {

  @InjectMocks
  private ConnectionServiceV2 subject = new ConnectionServiceV2();

  @Mock private Environment bodEnvironment;
  @Mock private ConnectionV2Repo connectionRepo;
  @Mock private ReservationService reservationService;
  @Mock private VirtualPortService virtualPortService;
  @Mock private ConnectionServiceRequesterV2 connectionServiceRequester;

  @Test
  public void querySummarySync_should_return_an_empty_list_when_no_connection_objects_where_found(){

    final String nonExistingConnectionId = "1";

    List<String> connectionIds = Arrays.asList(nonExistingConnectionId);

    when(connectionRepo.findByConnectionId(nonExistingConnectionId)).thenReturn(null);

    List<ConnectionV2> connections = subject.querySummarySync(connectionIds, new ArrayList<String>(), "foo");
    assertTrue("There may be no null element(s) in the list", connections.size() == 0);

  }
}
