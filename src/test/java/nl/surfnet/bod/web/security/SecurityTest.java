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
package nl.surfnet.bod.web.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.support.RichUserDetailsFactory;

import org.junit.Test;

public class SecurityTest {

  @Test
  public void userIsNotMemberOf() {
    RichUserDetails user = new RichUserDetailsFactory().create();
    Security.setUserDetails(user);

    assertThat(Security.isUserMemberOf("urn:group"), is(false));
    assertThat(Security.isUserNotMemberOf("urn:group"), is(true));
  }

  @Test
  public void userIsMemberOf() {
    RichUserDetails user = new RichUserDetailsFactory().addUserGroup("urn:group").create();
    Security.setUserDetails(user);

    assertThat(Security.isUserMemberOf("urn:group"), is(true));
    assertThat(Security.isUserNotMemberOf("urn:group"), is(false));
  }

}
