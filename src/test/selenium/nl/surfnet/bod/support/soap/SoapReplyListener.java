/**
 * Copyright (c) 2012, SURFnet BV
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
package nl.surfnet.bod.support.soap;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Stack;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple server that always replies "200 OK" and exposes a stack of response objects,
 * which you obviously should only use pop() on.
 *
 */
public class SoapReplyListener {

  private static final Logger LOG = LoggerFactory.getLogger(SoapReplyListener.class);

  private Stack<String> responses = new Stack<>();

  public String waitForReply(){

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      throw new IllegalStateException("No reply received in time!");
    }

    // TODO perform XML/XSD validation at this point, throw some RuntimeException if it fails
    return responses.pop();
  }


  public SoapReplyListener(Integer port) {

    try {
      Thread t = new RequestListenerThread(port, responses);
      t.setDaemon(false);
      t.start();
    } catch (IOException e) {
      LOG.error("Unable to start soap reply listener... Something else still running on our port? (=" + port + ") ", e);
    }

  }

  static class SoapPostRequestHandler implements HttpRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SoapPostRequestHandler.class);

    private Stack<String> responses;

    SoapPostRequestHandler(Stack<String> responses) {
      this.responses = responses;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
      LOG.debug("Handling request: " + request);

      if (! (request instanceof  HttpEntityEnclosingRequest)){
        throw new IllegalArgumentException("Request did not have a body: " + request);
      }

      HttpEntityEnclosingRequest httpEntityEnclosingRequest = (HttpEntityEnclosingRequest) request;
      responses.push(EntityUtils.toString(httpEntityEnclosingRequest.getEntity(), "UTF-8"));
      response.setStatusCode(200);
    }
  }

  static class RequestListenerThread extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(RequestListenerThread.class);

    private final ServerSocket serversocket;
    private final HttpParams params;
    private final HttpService httpService;

    public RequestListenerThread(int port, Stack<String> responses) throws IOException {
      this.serversocket = new ServerSocket(port);
      this.params = new SyncBasicHttpParams();
      this.params
          .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
          .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
          .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
          .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
          .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");

      // Set up the HTTP protocol processor
      HttpProcessor httpProcessor = new ImmutableHttpProcessor(new HttpResponseInterceptor[] {
          new ResponseDate(),
          new ResponseServer(),
          new ResponseContent(),
          new ResponseConnControl()
      });

      // Set up request handlers
      HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
      reqistry.register("*", new SoapPostRequestHandler(responses));

      // Set up the HTTP service
      this.httpService = new HttpService(
          httpProcessor,
          new DefaultConnectionReuseStrategy(),
          new DefaultHttpResponseFactory(),
          reqistry,
          this.params);
    }

    @Override
    public void run() {
      LOG.debug("Listening on port " + this.serversocket.getLocalPort());
      while (!Thread.interrupted()) {
        try {
          // Set up HTTP connection
          Socket socket = this.serversocket.accept();
          DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
          LOG.debug("Incoming connection from " + socket.getInetAddress());
          conn.bind(socket, this.params);

          // Start worker thread
          Thread t = new WorkerThread(this.httpService, conn);
          t.setDaemon(true);
          t.start();
        } catch (InterruptedIOException ex) {
          LOG.debug("interrupted!", ex);
          break;
        } catch (IOException e) {
          LOG.error("I/O error initialising connection thread: " + e.getMessage(), e);
          break;
        }
      }
    }
  }

  static class WorkerThread extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(RequestListenerThread.class);

    private final HttpService httpservice;
    private final HttpServerConnection conn;

    public WorkerThread(
        final HttpService httpservice,
        final HttpServerConnection conn) {
      super();
      this.httpservice = httpservice;
      this.conn = conn;
    }

    @Override
    public void run() {
      LOG.debug("New connection thread");
      HttpContext context = new BasicHttpContext(null);
      try {
        while (!Thread.interrupted() && this.conn.isOpen()) {
          this.httpservice.handleRequest(this.conn, context);
        }
      } catch (ConnectionClosedException ex) {
        LOG.error("Client closed connection");
      } catch (IOException ex) {
        LOG.error("I/O error: " + ex.getMessage());
      } catch (HttpException ex) {
        LOG.error("Unrecoverable HTTP protocol violation: " + ex.getMessage());
      } finally {
        try {
          this.conn.shutdown();
        } catch (IOException ignore) {}
      }
    }

  }
}
