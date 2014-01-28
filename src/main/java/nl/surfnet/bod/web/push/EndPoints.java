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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.servlet.AsyncContext;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import nl.surfnet.bod.web.push.EndPoint.LongPollEndPoint;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EndPoints {

  protected static final Object POISON_PILL = new Object();

  private final Logger logger = LoggerFactory.getLogger(EndPoints.class);
  private final Map<String, EndPoint> endPoints = Maps.newConcurrentMap();

  private final AtomicInteger lastEventId = new AtomicInteger(0);
  private final NavigableMap<Integer, PushMessage> recentEvents = new TreeMap<Integer, PushMessage>();
  private final BlockingQueue<Object> eventsToPublish = new ArrayBlockingQueue<>(100);
  private volatile boolean running = false;

  public int getLastEventId() {
    return lastEventId.get();
  }

  public void broadcast(final PushMessage event) {
    eventsToPublish.add(event);
  }

  public void shutdown() {
    eventsToPublish.add(POISON_PILL);
  }

  public boolean isRunning() {
    return running;
  }

  @PostConstruct
  public synchronized void startMessageProcessor() {
    if (running) {
      throw new IllegalStateException("already running");
    }

    Thread processor = new Thread() {
      @Override
      public void run() {
        logger.info("Web AJAX push started");
        running = true;
        while (true) {
          try {
            Object message = eventsToPublish.take();
            if (message == POISON_PILL) {
              running = false;
              logger.info("Web AJAX push stopped");
              return;
            } else if (message instanceof PushMessage) {
              handlePushMessage((PushMessage) message);
            }
          } catch (Exception e) {
            logger.info("Exception ignored while processing event: {}", e, e);
          }
        }
      }
    };
    processor.setName("AJAX push message processor");
    processor.setDaemon(true);
    processor.start();
  }

  private void handlePushMessage(final PushMessage event) {
    logger.debug("Got a event with message [{}] for groupId: {}, sending", event.toJson(), event.getGroupId());

    int eventId = registerEvent(event);
    Collection<EndPoint> authorizedEndPoints = selectAuthorizedEndPoints(event);
    sendEventToEndPoints(authorizedEndPoints, eventId, event);
  }

  private int registerEvent(final PushMessage event) {
    synchronized (recentEvents) {
      int eventId = lastEventId.incrementAndGet();
      recentEvents.put(eventId, event);
      for (Integer oldEventId : recentEvents.headMap(eventId - 100).keySet()) {
        recentEvents.remove(oldEventId);
      }
      return eventId;
    }
  }

  private void sendEventToEndPoints(Collection<EndPoint> endPoints, int eventId, final PushMessage event) {
    String message = event.toJson();
    logger.debug("Got a broadcast message [{}] for {} clients", message, endPoints.size());

    for (EndPoint connection : endPoints) {
      connection.sendMessage(eventId, message);
    }
  }

  private Collection<EndPoint> selectAuthorizedEndPoints(final PushMessage event) {
    Map<String, EndPoint> clients = new HashMap<String, EndPoint>(Maps.filterValues(endPoints, isAuthorized(event)));
    for (String key: clients.keySet()) {
      endPoints.remove(key);
    }
    return clients.values();
  }

  public void clientRequest(String id, int count, int lastEventId, AsyncContext asyncContext, RichUserDetails user) {
    checkNotNull(user, "user is required");
    logger.debug("New request for client {} with count {} and lastEventId {} and user {}", id, count, lastEventId, user);

    EndPoint endPoint = endPoints.remove(id);
    if (endPoint == null) {
      endPoint = new LongPollEndPoint(id, user);
    }
    endPoint.setAsyncContext(asyncContext);

    enqueueClient(id, count, endPoint, lastEventId);
  }

  private Predicate<EndPoint> isAuthorized(final PushMessage event) {
    checkNotNull(event, "event is required");
    return new Predicate<EndPoint>() {
      @Override
      public boolean apply(EndPoint connection) {
        checkNotNull(connection, "connection is required");
        RichUserDetails user = checkNotNull(connection.getUser(), "user is required");
        Collection<String> userGroupIds = checkNotNull(user.getUserGroupIds(), "userGroupIds is required");
        return userGroupIds.contains(event.getGroupId());
      }
    };
  }

  private void enqueueClient(String id, int count, EndPoint endPoint, int lastEventId) {
    Entry<Integer, PushMessage> event;
    synchronized (recentEvents) {
      event = recentEvents.higherEntry(lastEventId);
      if (event != null && !isAuthorized(event.getValue()).apply(endPoint)) {
        event = null;
      }
      if (event == null) {
        // Add client while `recentEvents` is locked to avoid missing an event.
        addClient(id, count, endPoint);
      }
    }
    if (event != null) {
      endPoint.sendMessage(lastEventId + 1, event.getValue().toJson());
    }
  }

  public void removeClient(String id, int count) {
    String key = clientKey(id, count);
    logger.debug("Removing client {}", key);
    endPoints.remove(key);
  }

  protected void addClient(String id, int count, EndPoint endPoint) {
    endPoints.put(clientKey(id, count), endPoint);
  }

  private String clientKey(String id, int count) {
    return id + "#" + count;
  }
}
