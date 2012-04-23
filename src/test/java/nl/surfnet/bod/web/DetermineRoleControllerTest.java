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
package nl.surfnet.bod.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Test;
import org.springframework.ui.Model;

public class DetermineRoleControllerTest {

  private DetermineRoleController subject = new DetermineRoleController();

  @Test
  public void aNocEngineerShouldBeRedirectToNocPage() {
    RichUserDetails user = new RichUserDetailsFactory().addNocRole().create();
    Security.setUserDetails(user);

    String page = subject.index();

    assertThat(page, is("redirect:noc"));
  }

  @Test
  public void aIctManagerShouldBeRedirectToManagerPage() {
    RichUserDetails user = new RichUserDetailsFactory().addManagerRole().create();
    Security.setUserDetails(user);

    String page = subject.index();

    assertThat(page, is("redirect:manager"));
  }

  @Test
  public void aUserShouldGoToIndex() {
    RichUserDetails user = new RichUserDetailsFactory().addUserRole().create();
    Security.setUserDetails(user);

    String page = subject.index();

    assertThat(page, is("redirect:user"));
  }

}
