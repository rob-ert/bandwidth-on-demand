package nl.surfnet.bod.web.push;

import java.io.IOException;

import nl.surfnet.bod.web.security.RichUserDetails;

import org.eclipse.jetty.websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndPoint implements WebSocket.OnTextMessage {

  private final Logger logger = LoggerFactory.getLogger(EndPoint.class);

  private final EndPoints endPoints;
  private final RichUserDetails user;

  private Connection connection;

  public EndPoint(RichUserDetails user, EndPoints endPoints) {
    this.endPoints = endPoints;
    this.user = user;
  }

  @Override
  public void onOpen(Connection con) {
    this.connection = con;
    endPoints.add(this);
  }

  @Override
  public void onClose(int closeCode, String message) {
    endPoints.remove(this);
  }

  @Override
  public void onMessage(String data) {
  }

  public RichUserDetails getUser() {
    return user;
  }

  public void sendMessage(String message) {
    try {
      connection.sendMessage(message);
    }
    catch (IOException e) {
      logger.warn("Could not send a message over websocket to client", e);
    }
  }

}
