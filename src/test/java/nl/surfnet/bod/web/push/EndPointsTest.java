package nl.surfnet.bod.web.push;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import javax.servlet.AsyncContext;

import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.junit.Test;

import com.google.common.collect.Lists;

public class EndPointsTest {

  private EndPoints subject = new EndPoints();

  @Test
  public void broadcastMessageShouldFilterOn() {
    RichUserDetails user1 = new RichUserDetailsFactory().addUserGroup("urn:firstGroup").create();
    RichUserDetails user2 = new RichUserDetailsFactory().create();

    DummyEndPoint connection1 = new DummyEndPoint(user1);
    DummyEndPoint connection2 = new DummyEndPoint(user2);

    subject.addClient("1", connection1);
    subject.addClient("2", connection2);

    subject.broadcast(new PushMessage() {
      @Override
      public String getMessage() {
        return "First message";
      }

      @Override
      public String getGroupId() {
        return "urn:firstGroup";
      }
    });

    assertThat(connection1.getMessages(), hasSize(1));
    assertThat(connection2.getMessages(), hasSize(0));
  }

  private static class DummyEndPoint implements EndPoint {

    private RichUserDetails user;
    private List<String> messages = Lists.newArrayList();

    public List<String> getMessages() {
      return messages;
    }

    public DummyEndPoint(RichUserDetails user) {
      this.user = user;
    }

    @Override
    public void sendMessage(String message) {
      send("message", message);
    }

    @Override
    public void send(String type, String data) {
      messages.add(data);
    }

    @Override
    public RichUserDetails getUser() {
      return user;
    }

    @Override
    public void setAsyncContext(AsyncContext asyncContext) {
      throw new UnsupportedOperationException();
    }

  }


}
