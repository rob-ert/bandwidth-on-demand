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

import nl.surfnet.bod.web.security.RichUserDetails;

import org.eclipse.jetty.websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

@Component
public class EndPoints {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final Multimap<RichUserDetails, EndPoint> endPoints = Multimaps.synchronizedMultimap(HashMultimap
      .<RichUserDetails, EndPoint> create());

  public WebSocket createNew(RichUserDetails user) {
    log.debug("Creating a new endpoint for user {}", user);
    return new EndPoint(user, this);
  }

  public void add(EndPoint endPoint) {
    endPoints.put(endPoint.getUser(), endPoint);
  }

  public void remove(EndPoint endPoint) {
    this.endPoints.remove(endPoint.getUser(), endPoint);
  }

  public void broadcast(final PushMessage event) {
    Collection<EndPoint> clients = Multimaps.filterKeys(endPoints, new Predicate<RichUserDetails>() {
      @Override
      public boolean apply(RichUserDetails user) {
        return user.getUserGroupIds().contains(event.getGroupId());
      }
    }).values();

    broadcast(clients, event.getMessage());
  }

  private void broadcast(Collection<EndPoint> clients, String message) {
    log.debug("Sent message '{}' to '{}' clients", message, clients.size());

    for (EndPoint client : clients) {
      client.sendMessage(message);
    }
  }

}
