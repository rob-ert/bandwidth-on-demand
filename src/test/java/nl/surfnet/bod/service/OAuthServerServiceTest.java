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

import static nl.surfnet.bod.matchers.OptionalMatchers.isAbsent;
import static nl.surfnet.bod.matchers.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import nl.surfnet.bod.domain.BodAccount;
import nl.surfnet.bod.domain.oauth.VerifiedToken;
import nl.surfnet.bod.support.MockHttpServer;
import nl.surfnet.bod.util.Environment;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class OAuthServerServiceTest {

  @InjectMocks
  private OAuthServerService subject;

  private static MockHttpServer mockOAuthServer;
  private static int port = 8088;

  private static String oAuthKey = "bod-client";
  private static String oAuthSecret = "secret";

  @BeforeClass
  public static void initAndStartServer() throws Exception {
    mockOAuthServer = new MockHttpServer(port);
    mockOAuthServer.withBasicAuthentication(oAuthKey, oAuthSecret);
    mockOAuthServer.startServer();
  }

  @AfterClass
  public static void stopServer() throws Exception {
    mockOAuthServer.stopServer();
  }

  @Before
  public void initEnv() {
    subject.setEnvironment(getOAuthEnvironment(oAuthKey, oAuthSecret));
  }

  @Test
  public void shouldBeAbsentForAEmptyAccessToken() {
    Optional<VerifiedToken> principal = subject.getVerifiedToken("");

    assertThat(principal, isAbsent());
  }

  @Test
  public void shouldBePresentForValidAccessToken() {
    String nameId = "urn:nl:surfguest:henk";
    String token = "1234-1234-abc";

    mockAccessTokenResponse(token, nameId);

    Optional<VerifiedToken> principal = subject.getVerifiedToken(token);

    assertThat(principal, isPresent());
  }

  @Test
  public void shouldBeAbsentForInvalidAccessToken() {
    mockAccessTokenResponse(HttpStatus.NOT_FOUND, new ByteArrayResource("".getBytes()));
    Optional<VerifiedToken> principal = subject.getVerifiedToken("1234-1234-abc");

    assertThat(principal, isAbsent());
  }

  @Test
  public void shouldBeAbsentForInvalidSecret() {
    String token = "1234-1234-abc";

    subject.setEnvironment(getOAuthEnvironment(oAuthKey, "WrongSecret"));
    mockAccessTokenResponse(token, "urn:truus");

    Optional<VerifiedToken> principal = subject.getVerifiedToken(token);

    assertThat(principal, isAbsent());
  }

  @Ignore("Prints exception so ignore")
  @Test
  public void shouldBeAbsentForInvalidOAuthServerUrl() {
    String token = "1234-1234-abc";

    subject.setEnvironment(getOauthEnvironmentWrongServerUrl());
    mockAccessTokenResponse(token, "urn:truus");

    Optional<VerifiedToken> principal = subject.getVerifiedToken(token);

    assertThat(principal, isAbsent());
  }

  @Test(expected = IllegalArgumentException.class)
  public void retreiveAllAccessTokensShouldHaveAnAdminAccessToken() {
    BodAccount account = new BodAccount();
    account.setNameId("urn:truus");
    account.setAuthorizationServerAccessToken("");

    subject.getAllAccessTokensForUser(account);
  }

  private Environment getOauthEnvironmentWrongServerUrl() {
    Environment environment = new Environment();
    environment.setOauthServerUrl("http://localhost:23434/wrong");
    environment.setResourceKey(oAuthKey);
    environment.setResourceSecret(oAuthSecret);

    return environment;
  }

  private Environment getOAuthEnvironment(String key, String secret) {
    Environment environment = new Environment();
    environment.setOauthServerUrl("http://localhost:" + port);
    environment.setResourceKey(oAuthKey);
    environment.setResourceSecret(secret);

    return environment;
  }

  private void mockAccessTokenResponse(HttpStatus status, Resource resource) {
    mockOAuthServer.addResponse("/v1/tokeninfo", status, resource);
  }

  private void mockAccessTokenResponse(String token, String nameId) {
    String jsonResponse = getAccessTokenJson(nameId);
    mockAccessTokenResponse(HttpStatus.OK, new ByteArrayResource(jsonResponse.getBytes()));
  }

  private String getAccessTokenJson(String nameId) {
    return "{\"audience\":\"\",\"scopes\":[],\"principal\":{\"name\":\""+nameId+"\",\"roles\":[],\"attributes\":{}},\"expires_in\":0}";
  }

}
