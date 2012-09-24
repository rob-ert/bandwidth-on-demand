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
package nl.surfnet.bod.web.view;

import java.io.IOException;

import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.Security;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class ReservationViewJsonRenderTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  public void jodaTimesShouldBePrettyPrinted() throws IOException {
    Security.setUserDetails(new RichUserDetailsFactory().create());
    DateTime startDateTime = new DateTime(2009, 3, 23, 12, 0);

    ElementActionView actionView = new ElementActionView(false);

    ReservationView reservationView = new ReservationView(new ReservationFactory().setStartDateTime(startDateTime)
        .create(), actionView);
    String json = mapper.writer().writeValueAsString(reservationView);

    assertThat(json, containsString("\"startDateTime\":\"2009-03-23 12:00\""));
  }

}
