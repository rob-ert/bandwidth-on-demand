package nl.surfnet.bod.web.push;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.push.Events.SimpleEvent;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.junit.Test;

public class EndPointsTest {

  private EndPoints subject = new EndPoints();

  @Test
  public void broadcastEventWithoutEndtPoins() {
    subject.broadcast(new SimpleEvent("urn:somegroup", "message"));
  }

  @Test
  public void broadcastWithAnEndPointThatShouldGetTheMessage() {
    EndPointStub client = new EndPointStub(new RichUserDetailsFactory().addUserGroup("urn:somegroup").create(), subject);

    subject.add(client);

    subject.broadcast(Events.createSimpleEvent("urn:somegroup", "message"));

    assertThat(client.isMessageSend(), is(true));
  }

  @Test
  public void broadcastWithAnEndPointThatShouldNotGetTheMessage() {
    EndPointStub client = new EndPointStub(new RichUserDetailsFactory().addUserGroup("urn:othergroup").create(),
        subject);

    subject.add(client);

    subject.broadcast(Events.createSimpleEvent("urn:somegroup", "message"));

    assertThat(client.isMessageSend(), is(false));
  }

  @Test
  public void broadcastWithTwoEndPoints() {
    EndPointStub clientShouldGetMessage = new EndPointStub(new RichUserDetailsFactory().addUserGroup("urn:somegroup")
        .create(), subject);
    EndPointStub clientShouldNotGetMessage = new EndPointStub(new RichUserDetailsFactory().addUserGroup(
        "urn:othergroup").create(), subject);

    subject.add(clientShouldGetMessage);
    subject.add(clientShouldNotGetMessage);

    subject.broadcast(Events.createSimpleEvent("urn:somegroup", "message"));

    assertThat(clientShouldGetMessage.isMessageSend(), is(true));
    assertThat(clientShouldNotGetMessage.isMessageSend(), is(false));
  }

  private static final class EndPointStub extends EndPoint {
    private boolean messageSend;

    public EndPointStub(RichUserDetails user, EndPoints endPoints) {
      super(user, endPoints);
    }

    @Override
    public void sendMessage(String message) {
      messageSend = true;
    }

    public boolean isMessageSend() {
      return messageSend;
    }
  }
}
