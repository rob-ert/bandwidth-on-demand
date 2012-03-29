package nl.surfnet.bod.web.push;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.AsyncContext;

import nl.surfnet.bod.web.security.RichUserDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface EndPoint {
  void sendMessage(String message);

  void send(String type, String data);

  RichUserDetails getUser();

  void setAsyncContext(AsyncContext asyncContext);

  public class LongPollEndPoint implements EndPoint {
    private final Logger logger = LoggerFactory.getLogger(LongPollEndPoint.class);

    private AsyncContext asyncContext;
    private final String id;
    private final RichUserDetails user;
    private final AtomicInteger eventId = new AtomicInteger(0);

    public LongPollEndPoint(String id, RichUserDetails user) {
      this.id = id;
      this.user = user;
    }

    @Override
    public void setAsyncContext(AsyncContext asyncContext) {
      this.asyncContext = asyncContext;
    }

    @Override
    public RichUserDetails getUser() {
      return user;
    }

    @Override
    public void sendMessage(String message) {
      send("message", message);
    }

    @Override
    public void send(String type, String data) {
      if (asyncContext.getRequest().isAsyncStarted()) {
        eventId.incrementAndGet();
        PrintWriter writer;
        try {
          writer = asyncContext.getResponse().getWriter();
          String template = "{\"type\": \"%s\", \"data\": %s, \"id\": %d}";
          writer.write(String.format(template, type, data, eventId.get()));
          writer.flush();
          asyncContext.complete();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
      else {
        logger.info("Could not send message to {}, {}", id, data);
      }
    }

  }
}