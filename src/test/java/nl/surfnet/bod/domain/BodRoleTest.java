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
package nl.surfnet.bod.domain;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

public class BodRoleTest {

  @Test
  public void nocShouldBeNoc() {
    BodRole nocEngineer = BodRole.createNocEngineer();

    assertThat(nocEngineer.isNocRole(), is(true));
    assertThat(nocEngineer.isManagerRole(), is(false));
  }

  @Test
  public void managerShouldBeManager() {
    BodRole manager = BodRole.createManager(new PhysicalResourceGroupFactory().create());

    assertThat(manager.isManagerRole(), is(true));
    assertThat(manager.isNocRole(), is(false));
  }

  @Test
  public void userShouldBeUser() {
    BodRole user = BodRole.createUser();

    assertThat(user.isUserRole(), is(true));
  }

  @Test
  public void newUserShouldNotBeUser() {
    BodRole newUser = BodRole.createNewUser();

    assertThat(newUser.isUserRole(), is(false));
  }

}
