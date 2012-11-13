/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
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
