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

  @Test
  public void gotToAppManagerUri() {
    BodRole appManagerRole = BodRole.createAppManager();
    BodRole userRole = BodRole.createUser();
    RichUserDetails user = new RichUserDetailsFactory().addBodRoles(userRole, appManagerRole).create();

    Security.setUserDetails(user);
    Security.switchToUser();

    subject.verifySelectedRole("/appmanager");

    assertThat(Security.isSelectedAppManagerRole(), is(true));
    assertThat(Security.getSelectedRole(), is(appManagerRole));
  }
}