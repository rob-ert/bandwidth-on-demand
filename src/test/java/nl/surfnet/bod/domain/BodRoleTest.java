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
package nl.surfnet.bod.domain;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.junit.Test;

public class BodRoleTest {

  @Test
  public void nocShouldBeNoc() {
    BodRole nocEngineer = BodRole.createNocEngineer();

    assertThat(nocEngineer.isNocRole(), is(true));

    assertThat(nocEngineer.isAppManagerRole(), is(false));
    assertThat(nocEngineer.isManagerRole(), is(false));
    assertThat(nocEngineer.isUserRole(), is(false));
  }

  @Test
  public void managerShouldBeManager() {
    BodRole manager = BodRole.createManager(new PhysicalResourceGroupFactory().create());

    assertThat(manager.isManagerRole(), is(true));

    assertThat(manager.isNocRole(), is(false));
    assertThat(manager.isAppManagerRole(), is(false));
    assertThat(manager.isUserRole(), is(false));
  }

  @Test
  public void userShouldBeUser() {
    BodRole user = BodRole.createUser();

    assertThat(user.isUserRole(), is(true));

    assertThat(user.isAppManagerRole(), is(false));
    assertThat(user.isNocRole(), is(false));
    assertThat(user.isManagerRole(), is(false));
  }

  @Test
  public void newUserShouldNotBeUser() {
    BodRole newUser = BodRole.createNewUser();

    assertThat(newUser.isAppManagerRole(), is(false));
    assertThat(newUser.isNocRole(), is(false));
    assertThat(newUser.isManagerRole(), is(false));
    assertThat(newUser.isUserRole(), is(false));
  }

  @Test
  public void appManagerShouldBeAppManager() {
    BodRole appManager = BodRole.createAppManager();

    assertThat(appManager.isAppManagerRole(), is(true));

    assertThat(appManager.isNocRole(), is(false));
    assertThat(appManager.isManagerRole(), is(false));
    assertThat(appManager.isUserRole(), is(false));
  }
}