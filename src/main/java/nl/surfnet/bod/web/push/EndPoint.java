package nl.surfnet.bod.web.push;

import java.io.IOException;

import nl.surfnet.bod.web.security.RichUserDetails;

import org.eclipse.jetty.websocket.WebSocket;

public class EndPoint implements WebSocket.OnTextMessage {

  private final EndPoints endPoints;
  private final RichUserDetails user;

  private Connection connection;

  public EndPoint(RichUserDetails user, EndPoints endPoints) {
    this.endPoints = endPoints;
    this.user = user;
  }

  @Override
  public void onOpen(Connection connection) {
    this.connection = connection;
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
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
