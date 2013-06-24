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
package nl.surfnet.bod.support;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.security.Constraint;
import org.eclipse.jetty.http.security.Credential;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

public class MockHttpServer extends AbstractHandler {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final Server server;
  private final HandlerCollection mainHandlers;

  private Map<String, MockResponse> responseResource = Maps.newHashMap();
  private final LinkedBlockingDeque<String> lastRequests = new LinkedBlockingDeque<>();
  private final List<String> requests = Lists.newArrayList();

  private String username;
  private String password;

  public MockHttpServer(int port) {
    server = new Server(port);
    mainHandlers = new HandlerCollection();
  }

  public void addHandler(Handler handler) {
    mainHandlers.addHandler(handler);
  }

  public void startServer() throws Exception {
    if (username == null && password == null) {
      server.setHandler(this);
    }
    else {
      server.setHandler(getSecurityHandler());
    }
    server.start();
  }

  private SecurityHandler getSecurityHandler() {
    Constraint constraint = new Constraint();
    constraint.setName(Constraint.__BASIC_AUTH);
    constraint.setRoles(new String[] { "admin" });
    constraint.setAuthenticate(true);

    ConstraintMapping cm = new ConstraintMapping();
    cm.setConstraint(constraint);
    cm.setPathSpec("/*");

    HashLoginService loginService = new HashLoginService();
    loginService.putUser(username, Credential.getCredential(password), new String[] { "admin" });

    ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
    securityHandler.setRealmName("please password");
    securityHandler.setAuthenticator(new BasicAuthenticator());
    securityHandler.addConstraintMapping(cm);
    securityHandler.setLoginService(loginService);
    securityHandler.setHandler(this);

    return securityHandler;
  }

  public void stopServer() throws Exception {
    server.stop();
    server.join();
  }

  @Override
  public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    saveRequestBody(request);

    if (responseResource.containsKey(target)) {
      try (ServletOutputStream outputStream = response.getOutputStream()) {
        MockResponse mockResponse = responseResource.get(target);
        response.setStatus(mockResponse.getStatus().value());
        ByteStreams.copy(mockResponse.getBody().getInputStream(), outputStream);
      }
    }
    else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  private void saveRequestBody(final HttpServletRequest request) throws IOException {
    final String currentRequestBody = IOUtils.toString(request.getInputStream());
    lastRequests.addFirst(currentRequestBody);
    requests.add(currentRequestBody);
  }

  public void removeResponse(String path) {
    this.responseResource.remove(prependPathWithSlash(path));
  }

  public void addResponse(String path, HttpStatus status, Resource body) {
    this.responseResource.put(prependPathWithSlash(path), new MockResponse(status, body));
  }

  public void addResponse(String path, Resource body) {
    addResponse(path, HttpStatus.OK, body);
  }

  private String prependPathWithSlash(String path) {
    return path.startsWith("/") ? path : "/" + path;
  }

  public void withBasicAuthentication(String user, String pass) {
    this.username = user;
    this.password = pass;
  }

  public final int getCallCounter() {
    return requests.size();
  }

  public final String awaitRequest(long timeout, TimeUnit unit) {
    try {
      String request = lastRequests.pollLast(timeout, unit);
      if (request == null) {
        throw new AssertionError("Failed to retrieve the request");
      }
      return request;
    }
    catch (InterruptedException e) {
      log.error("Error: ", e);
      throw new RuntimeException(e);
    }
  }

  public final List<String> getRequests() {
    return requests;
  }

  public void clearRequests() {
    lastRequests.clear();
  }

  private static class MockResponse {
    private final HttpStatus status;
    private final Resource body;

    public MockResponse(HttpStatus status, Resource body) {
      this.status = status;
      this.body = body;
    }

    protected HttpStatus getStatus() {
      return status;
    }

    protected Resource getBody() {
      return body;
    }

  }
}
