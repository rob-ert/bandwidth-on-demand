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
package nl.surfnet.bod.web.push;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

@RunWith(MockitoJUnitRunner.class)
public class LongPollServletTest {

  @InjectMocks
  private LongPollServlet subject;

  @Mock
  private EndPoints endPointsMock;

  private RichUserDetails user = new RichUserDetailsFactory().create();

  @Before
  public void userLogin() {
    Security.setUserDetails(user);
  }

  @Test
  public void withoutATransportParameterShouldDoNothing() {
    HttpServletRequest requestMock = mock(HttpServletRequest.class);
    HttpServletResponse responseMock = mock(HttpServletResponse.class);

    subject.doGet(requestMock, responseMock);

    verifyZeroInteractions(endPointsMock);
  }

  @Test
  public void callWithLongPollTransport() {
    HttpServletRequest requestMock = mock(HttpServletRequest.class);
    HttpServletResponse responseMock = mock(HttpServletResponse.class);
    AsyncContextStub asyncContext = new AsyncContextStub();

    when(requestMock.getParameter("transport")).thenReturn("longpollajax");
    when(requestMock.getParameter("count")).thenReturn("1");
    when(requestMock.getParameter("id")).thenReturn("f8eed38c-c0d5-4b15-b9b9-e121a1741df6");
    when(requestMock.startAsync(requestMock, responseMock)).thenReturn(asyncContext);

    subject.doGet(requestMock, responseMock);

    verify(endPointsMock).clientRequest("f8eed38c-c0d5-4b15-b9b9-e121a1741df6", 1, asyncContext, user);
  }

  @Test
  public void extractSocketIdForAValidMessagE() {
    String message = "data={\"id\":\"1\",\"socket\":\"f8eed38c-c0d5-4b15-b9b9-e121a1741df6\",\"type\":\"heartbeat\",\"reply\":false}";

    String socketId = subject.extractSocketId(message);

    assertThat(socketId, is("f8eed38c-c0d5-4b15-b9b9-e121a1741df6"));
  }

  @Test
  public void extractSocketIdForInvalidMessageShouldGiveNull() {
    String message = "data={\"id\":\"1\",\"type\":\"heartbeat\",\"data\":null,\"reply\":false}";

    String socketId = subject.extractSocketId(message);

    assertThat(socketId, nullValue());
  }

  private static class AsyncContextStub implements AsyncContext {
    @Override
    public ServletRequest getRequest() {
      return null;
    }
    @Override
    public ServletResponse getResponse() {
      return null;
    }
    @Override
    public boolean hasOriginalRequestAndResponse() {
      return false;
    }
    @Override
    public void dispatch() {
    }
    @Override
    public void dispatch(String path) {
    }
    @Override
    public void dispatch(ServletContext context, String path) {
    }
    @Override
    public void complete() {
    }
    @Override
    public void start(Runnable run) {
    }
    @Override
    public void addListener(AsyncListener listener) {
    }
    @Override
    public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse) {
    }
    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
      return null;
    }
    @Override
    public void setTimeout(long timeout) {
    }
    @Override
    public long getTimeout() {
      return 0;
    }
  }

}
