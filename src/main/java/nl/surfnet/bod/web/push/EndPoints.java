/**
 * Copyright (c) 2012, 2013 SURFnet BV
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
package nl.surfnet.bod.web.push;

import java.util.Collection;
import java.util.Map;

import javax.servlet.AsyncContext;

import nl.surfnet.bod.web.push.EndPoint.LongPollEndPoint;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

@Component
public class EndPoints {

  private final Logger logger = LoggerFactory.getLogger(EndPoints.class);
  private final Map<String, EndPoint> endPoints = Maps.newConcurrentMap();

  public void broadcast(final PushMessage event) {
    logger.debug("Got a event with message [{}] for groupId: {}", event.getMessage(), event.getGroupId());
    Map<String, EndPoint> clients = Maps.filterValues(endPoints, new Predicate<EndPoint>() {
      @Override
      public boolean apply(EndPoint connection) {
        return connection.getUser().getUserGroupIds().contains(event.getGroupId());
      }
    });

    broadcast(clients.values(), event.getMessage());
  }

  private void broadcast(Collection<EndPoint> clients, String message) {
    logger.debug("Got a broadcast message [{}] for {} clients", message, clients.size());
    for (EndPoint connection : clients) {
      connection.sendMessage(message);
    }
  }

  public void clientRequest(String id, Integer count, AsyncContext asyncContext, RichUserDetails user) {
    logger.debug("New request for client {} with count {}", id, count);

    if (count == 1) {
      addClient(id, new LongPollEndPoint(id, user));
    }

    endPoints.get(id).setAsyncContext(asyncContext);
  }

  public void removeClient(String id) {
    logger.debug("Removing client {}", id);
    endPoints.remove(id);
  }

  public void sendHeartbeat(String id) {
    EndPoint connection = endPoints.get(id);
    connection.send("heartbeat", null);
  }

  protected void addClient(String id, EndPoint endPoint) {
    endPoints.put(id, endPoint);
  }
}
