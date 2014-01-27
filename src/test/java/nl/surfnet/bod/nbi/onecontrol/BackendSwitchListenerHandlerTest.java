package nl.surfnet.bod.nbi.onecontrol;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.junit.Before;
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

  @Mock
  private SOAPMessageContext soapMessageContext;

  private Map<String, List<String>> headers;

  @Before
  public void before(){
    when(soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
    // default to primary
    headers = new HashMap<>();
    headers.put(BackendSwitchListenerHandler.BACKEND_SERVER_ID_HEADER, Arrays.asList("primary"));
    when(soapMessageContext.get(MessageContext.HTTP_RESPONSE_HEADERS)).thenReturn(headers);
  }

  @Test
  public void outboundMessagesHaveNoEffect() throws Exception {
    when(soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(true);
    subject.handleMessage(soapMessageContext);
    verifyZeroInteractions(notificationSubscriber);
  }

  @Test
  public void firstInvocationHasNoEffectOnSubscription(){
    subject.handleMessage(soapMessageContext);
    verifyZeroInteractions(notificationSubscriber);
  }

  @Test
  public void changeInConfiguredHeaderCausesSwitch(){
    subject.handleMessage(soapMessageContext);
    verifyZeroInteractions(notificationSubscriber);
    headers.put(BackendSwitchListenerHandler.BACKEND_SERVER_ID_HEADER, Arrays.asList("secondary"));
    subject.handleMessage(soapMessageContext);
    verify(notificationSubscriber).unsubscribe();
    verify(notificationSubscriber).subscribe();
  }

}
