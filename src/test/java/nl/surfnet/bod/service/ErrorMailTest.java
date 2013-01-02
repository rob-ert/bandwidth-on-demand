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
package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import nl.surfnet.bod.service.Emails.ErrorMail;
import nl.surfnet.bod.support.RichUserDetailsFactory;

public class ErrorMailTest {

  @Test
  public void errorMailShouldContainUserAndError() {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080"));

    String bodyText = ErrorMail.body(new RichUserDetailsFactory().setDisplayname("Truus").setEmail("truus@henk.nl")
        .create(), new RuntimeException("Something went wrong"), request);

    assertThat(bodyText, containsString("Something went wrong"));
    assertThat(bodyText, containsString("User: Truus (truus@henk.nl)"));
  }

  @Test
  public void errorMailWithoutALoggedInUser() {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080"));

    String bodyText = ErrorMail.body(null, new RuntimeException("Something went wrong"), request);

    assertThat(bodyText, containsString("User: Unknown"));
    assertThat(bodyText, containsString("Username: Unknown"));
    assertThat(bodyText, containsString("Something went wrong"));
  }
}
