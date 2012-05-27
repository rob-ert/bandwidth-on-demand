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
package nl.surfnet.bod.support;

import static java.util.concurrent.TimeUnit.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

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
import org.springframework.core.io.Resource;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

public class MockHttpServer extends AbstractHandler {

  private final Server server;
  private final HandlerCollection mainHandlers;

  private Map<String, Resource> responseResource = Maps.newHashMap();
  private final LinkedBlockingDeque<String> requests = new LinkedBlockingDeque<String>();

  private String username;
  private String password;

  private int callCounter = 0;

  public MockHttpServer(int port) {
    server = new Server(port);
    mainHandlers = new HandlerCollection();
  }

  public void addHandler(Handler handler) {
    mainHandlers.addHandler(handler);
  }

  public void startServer() throws Exception {
    if (username != null && password != null) {
      server.setHandler(getSecurityHandler());
    }
    else {
      server.setHandler(this);
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

    callCounter++;
    requests.addLast(IOUtils.toString(request.getInputStream()));
    if (responseResource.containsKey(target)) {
      ServletOutputStream outputStream = response.getOutputStream();
      response.setStatus(HttpServletResponse.SC_OK);
      ByteStreams.copy(responseResource.get(target).getInputStream(), outputStream);
      Closeables.close(outputStream, false);
    }
    else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  public void removeResponse(String path) {
    this.responseResource.remove(prependPathWithSlash(path));
  }

  public void addResponse(String path, Resource resource) {
    this.responseResource.put(prependPathWithSlash(path), resource);
  }

  private String prependPathWithSlash(String path) {
    return path.startsWith("/") ? path : "/" + path;
  }

  public void withBasicAuthentication(String user, String pass) {
    this.username = user;
    this.password = pass;
  }

  public final int getCallCounter() {
    return callCounter;
  }

  public final String waitForLastRequest(final long seconds) {
    try {
      final String lastRequest = requests.poll(seconds, SECONDS);
      // add it back, need this strange construction because there is no peek
      // with wait timeout
      requests.addLast(lastRequest);
      return lastRequest;
    }
    catch (InterruptedException e) {
      return null;
    }
  }

  public final Collection<String> getRequests() {
    return requests;
  }

}
