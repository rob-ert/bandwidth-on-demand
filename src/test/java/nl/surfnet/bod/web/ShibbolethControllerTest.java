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
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;

import java.util.Collection;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.Security;

import org.junit.Test;

public class ShibbolethControllerTest {

  private ShibbolethController subject = new ShibbolethController();

  @SuppressWarnings("unchecked")
  @Test
  public void test() {
    Security.setUserDetails(new RichUserDetailsFactory().addUserGroup("urn:group").create());

    ModelStub model = new ModelStub();

    subject.list(model);

    assertThat(model.asMap(), hasKey("groups"));
    assertThat((Collection<UserGroup>) model.asMap().get("groups"), hasSize(1));
  }

}
