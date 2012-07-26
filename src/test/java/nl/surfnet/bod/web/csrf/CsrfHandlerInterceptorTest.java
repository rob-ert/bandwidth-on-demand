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
package nl.surfnet.bod.web.csrf;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Test;

public class CsrfHandlerInterceptorTest {

  private CsrfHandlerInterceptor subject = new CsrfHandlerInterceptor();

  @Test
  public void getReqeustsShouldBeIgnored() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getMethod()).thenReturn("GET", "get");

    boolean result = subject.preHandle(request, response, new Object());
    assertThat(result, is(true));

    result = subject.preHandle(request, response, new Object());
    assertThat(result, is(true));
  }

  @Test
  public void changingRequestsShouldChecked() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    HttpSession session = mock(HttpSession.class);

    String[] methods = { "post", "DELETE", "delete", "PUT", "put" };

    when(request.getMethod()).thenReturn("POST", methods);
    when(request.getSession()).thenReturn(session);

    for (int i = 0; i < methods.length + 1; i++) {
      boolean result = subject.preHandle(request, response, new Object());
      assertThat(result, is(false));
    }
  }

}
