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
    return new EndPoint(user, this);
  }

  public void add(EndPoint endPoint) {
    endPoints.put(endPoint.getUser(), endPoint);
  }

  public void remove(EndPoint endPoint) {
    this.endPoints.remove(endPoint.getUser(), endPoint);
  }

  public void broadcast(final Event event) {
    Collection<EndPoint> clients = Multimaps.filterKeys(endPoints, new Predicate<RichUserDetails>() {
      @Override
      public boolean apply(RichUserDetails user) {
        return user.getUserGroupIds().contains(event.getGroupId());
      }
    }).values();

    broadcast(clients, event.getMessage());
  }

  private void broadcast(Collection<EndPoint> clients, String message) {
    for (EndPoint client : clients) {
      client.sendMessage(message);
    }

    log.debug("Sent message [{}] to [{}] clients", message, clients.size());
  }

}
