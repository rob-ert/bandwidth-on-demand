package nl.surfnet.bod.web.push;

import nl.surfnet.bod.web.security.RichUserDetails;

import org.eclipse.jetty.websocket.WebSocket;
import org.springframework.stereotype.Component;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

@Component
public class EndPoints {

  private final Multimap<RichUserDetails, EndPoint> clients = Multimaps.synchronizedMultimap(
      HashMultimap.<RichUserDetails, EndPoint> create());

  public WebSocket createNew(RichUserDetails user) {
    return new EndPoint(user, this);
  }

  public void add(EndPoint endPoint) {
    clients.put(endPoint.getUser(), endPoint);
  }

  public void remove(EndPoint endPoint) {
    this.clients.remove(endPoint.getUser(), endPoint);
  }

  public void broadcast(Event event) {

  }

  private void broadcast(String message) {
    for (EndPoint client : clients.values()) {
      client.sendMessage(message);
    }
  }

}
