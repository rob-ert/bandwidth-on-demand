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

import static nl.surfnet.bod.util.TestHelper.accProperties;
import static nl.surfnet.bod.util.TestHelper.productionProperties;
import static nl.surfnet.bod.util.TestHelper.testProperties;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import java.util.Collection;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.util.TestHelper.PropertiesEnvironment;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.google.common.collect.Iterables;

public class GroupOpenSocialServiceTestIntegration {

  private static final String NAME_ID = "urn:collab:person:surfguest.nl:alanvdam";

  private GroupOpenSocialService subject = new GroupOpenSocialService();

  @Test
  public void retreiveGroupsFromApiForTest() {
    verifyRetreiveGroups(testEnvironment(), containsString("urn:collab:group:test.surfteams"));
  }

  @Test
  public void retreiveGroupsFromApiForAcc() {
    verifyRetreiveGroups(accEnvironment(), containsString("urn:collab:group:surfteams"));
  }

  @Test
  public void retreiveGroupsFromApiForProd() {
    verifyRetreiveGroups(prodEnvironment(), containsString("urn:collab:group:surfteams"));
  }

  private void verifyRetreiveGroups(Environment env, Matcher<String> matcher) {
    subject.setEnvironment(env);

    Collection<UserGroup> groups = subject.getGroups(NAME_ID);

    assertThat(groups, hasSize(greaterThan(0)));
    assertThat(Iterables.getFirst(groups, null).getId(), matcher);
  }

  private Environment testEnvironment() {
    return environment(testProperties());
  }

  private Environment accEnvironment() {
    return environment(accProperties());
  }

  private Environment prodEnvironment() {
    return environment(productionProperties());
  }

  private Environment environment(PropertiesEnvironment env) {
    Environment environment = new Environment();
    environment.setOpenSocialUrl(env.getProperty("os.url"));
    environment.setOpenSocialOAuthKey(env.getProperty("os.oauth-key"));
    environment.setOpenSocialOAuthSecret(env.getDecryptedProperty("os.oauth-secret"));
    return environment;
  }
}
