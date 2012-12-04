/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.web.push;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.*;
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
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setContentType("text/" + (transport.equals("longpolljsonp") ? "javascript" : "plain"));

    final String id = request.getParameter("id");
    final String count = request.getParameter("count");
    final boolean first = "1".equals(request.getParameter("count"));

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
        if (!first && !event.getAsyncContext().getResponse().isCommitted()) {
          connections.removeClient(id);
        }
      }
    });

    connections.clientRequest(id, Integer.valueOf(count), aCtx, Security.getUserDetails());

    if (first) {
      aCtx.complete();
    }
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
