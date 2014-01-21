package nl.surfnet.bod.nsi.v2;

import static org.mockito.Mockito.verifyZeroInteractions;

import java.net.URI;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ogf.schemas.nsi._2013._12.connection.types.QueryResultResponseType;
import org.ogf.schemas.nsi._2013._12.framework.headers.CommonHeaderType;

import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceRequesterClientTest {

  @Mock
  private NsiV2MessageRepo messageRepo;

  @Mock
  private ConnectionServiceRequesterAsyncClient asyncClient;

  @InjectMocks
  private ConnectionServiceRequesterClient connectionServiceRequesterClient = new ConnectionServiceRequesterClient();

  @Test
  public void aQueryReplyShouldNotBeStoredInDb() throws Exception {
    final CommonHeaderType commonHeaderType = new CommonHeaderType()
            .withProtocolVersion("2.0")
            .withRequesterNSA("urn:ogf:network:nsa:foo")
            .withProviderNSA("blah")
            .withReplyTo("boo")
            .withCorrelationId("dw");
    connectionServiceRequesterClient.replyQueryResultConfirmed(commonHeaderType, new ArrayList<QueryResultResponseType>(), Optional.of(new URI("http://foo")));
    verifyZeroInteractions(messageRepo);
  }
}
