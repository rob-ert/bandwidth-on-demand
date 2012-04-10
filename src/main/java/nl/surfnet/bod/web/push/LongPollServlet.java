package nl.surfnet.bod.web.push;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.surfnet.bod.web.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

@SuppressWarnings("serial")
public class LongPollServlet extends HttpServlet {

  private static final Pattern SOCKET_ID_PATTERN = Pattern.compile(".*\"socket\":\"([a-z0-9\\-]+).*", Pattern.DOTALL);

  private Logger logger = LoggerFactory.getLogger(LongPollServlet.class);

  @Autowired
  private EndPoints connections;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    String transport = request.getParameter("transport");
    if (transport == null) {
      logger.error("No transport defined for the long polling call");
    }
    else if (transport.equals("longpollajax") || transport.equals("longpollxdr") || transport.equals("longpolljsonp")) {
      doLongPollConnect(request, response, transport);
    }
    else {
      logger.error("Do not understand the transport '{}'", transport);
    }
  }

  private void doLongPollConnect(HttpServletRequest request, HttpServletResponse response, String transport) {
    response.setCharacterEncoding("utf-8");
    // required by longpollxdr
    // (http://msdn.microsoft.com/en-us/library/cc288060%28v=VS.85%29.aspx)
    response.setHeader("Access-Control-Allow-Origin", "*");
    // longpolljsonp requires the content type to be 'text/javascript'
    response.setContentType("text/" + (transport.equals("longpolljsonp") ? "javascript" : "plain"));

    final String id = request.getParameter("id");
    final String count = request.getParameter("count");

    AsyncContext aCtx = request.startAsync(request, response);
    aCtx.addListener(new AsyncListener() {
      @Override
      public void onTimeout(AsyncEvent event) throws IOException {
        cleanup(event);
      }

      @Override
      public void onStartAsync(AsyncEvent event) throws IOException {
      }

      @Override
      public void onError(AsyncEvent event) throws IOException {
        cleanup(event);
      }

      @Override
      public void onComplete(AsyncEvent event) throws IOException {
        cleanup(event);
      }

      private void cleanup(AsyncEvent event) {
        if (!event.getAsyncContext().getResponse().isCommitted()) {
          connections.removeClient(id);
        }
      }
    });

    connections.clientRequest(id, Integer.valueOf(count), aCtx, Security.getUserDetails());
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("utf-8");

    String socketId = extractSocketId(request.getReader().readLine());

    if (socketId != null) {
      connections.sendHeartbeat(socketId);
    }

    response.setHeader("Access-Control-Allow-Origin", "*");
  }

  protected String extractSocketId(String message) {
    // data={"id":"1","socket":"f8eed38c-c0d5-4b15-b9b9-e121a1741df6","type":"heartbeat","data":null,"reply":false}
    Matcher matcher = SOCKET_ID_PATTERN.matcher(message);

    if (matcher.matches()) {
      return matcher.group(1);
    }

    return null;
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
  }

}
