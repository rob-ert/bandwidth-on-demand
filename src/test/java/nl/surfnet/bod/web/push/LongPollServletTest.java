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
package nl.surfnet.bod.web.push;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    when(requestMock.getParameter("lastEventId")).thenReturn("442");
    when(requestMock.startAsync(requestMock, responseMock)).thenReturn(asyncContext);

    subject.doGet(requestMock, responseMock);

    verify(endPointsMock).clientRequest("f8eed38c-c0d5-4b15-b9b9-e121a1741df6", 1, 442, asyncContext, user);
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
