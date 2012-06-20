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
package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.service.Emails.ErrorMail;
import nl.surfnet.bod.support.RichUserDetailsFactory;

import org.junit.Test;

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
