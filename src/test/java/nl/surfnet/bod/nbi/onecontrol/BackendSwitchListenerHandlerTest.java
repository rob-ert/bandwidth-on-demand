package nl.surfnet.bod.nbi.onecontrol;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BackendSwitchListenerHandlerTest {

  @Mock
  NotificationSubscriber notificationSubscriber;

  @InjectMocks
  private BackendSwitchListenerHandler subject = new BackendSwitchListenerHandler();

  @Test
  public void outoundmessagesAreIgnored() throws Exception {
    fail();
  }

  // TODO hans add more tests

}
