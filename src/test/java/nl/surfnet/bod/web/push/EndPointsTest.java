/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
