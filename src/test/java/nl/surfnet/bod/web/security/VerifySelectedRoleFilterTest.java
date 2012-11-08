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
import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.support.RichUserDetailsFactory;

import org.junit.Test;

public class VerifySelectedRoleFilterTest {

  private VerifySelectedRoleFilter subject = new VerifySelectedRoleFilter();

  @Test
  public void goToNocUriWithoutNocRole() {
    BodRole userRole = BodRole.createUser();
    RichUserDetails user = new RichUserDetailsFactory().addBodRoles(userRole).create();

    Security.setUserDetails(user);
    Security.switchToUser();

    subject.verifySelectedRole("/noc/reservations");

    assertThat(Security.isSelectedUserRole(), is(true));
    assertThat(Security.getSelectedRole(), is(userRole));
  }

  @Test
  public void goToNocUriWithNocRole() {
    BodRole userRole = BodRole.createUser();
    BodRole nocRole = BodRole.createNocEngineer();
    RichUserDetails user = new RichUserDetailsFactory().addBodRoles(userRole, nocRole).create();

    Security.setUserDetails(user);
    Security.switchToUser();

    subject.verifySelectedRole("/noc/reservations");

    assertThat(Security.isSelectedNocRole(), is(true));
    assertThat(Security.getSelectedRole(), is(nocRole));
  }

  @Test
  public void goToUserUri() {
    BodRole nocRole = BodRole.createNocEngineer();
    BodRole userRole = BodRole.createUser();
    RichUserDetails user = new RichUserDetailsFactory().addBodRoles(userRole, nocRole).create();

    Security.setUserDetails(user);
    Security.switchToNocEngineer();

    subject.verifySelectedRole("/reservations");

    assertThat(Security.isSelectedUserRole(), is(true));
    assertThat(Security.getSelectedRole(), is(userRole));
  }
}
