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
package nl.surfnet.bod.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import com.google.common.base.Optional;
import nl.surfnet.bod.domain.BodAccount;
import nl.surfnet.bod.domain.oauth.AccessToken;
import nl.surfnet.bod.domain.oauth.AccessTokenResponse;
import nl.surfnet.bod.domain.oauth.AuthenticatedPrincipal;
import nl.surfnet.bod.domain.oauth.NsiScope;
import nl.surfnet.bod.domain.oauth.VerifiedToken;
import nl.surfnet.bod.service.OAuthServerService;
import nl.surfnet.bod.util.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Always returns the same predefined token
 */
public class MockOAuthServerService extends OAuthServerService {

  private VerifiedToken verifiedToken;
  private static final Logger LOG = LoggerFactory.getLogger(MockAuthBeanFactoryPostProcessor.class);

  public MockOAuthServerService() {
    LOG.info("Instantiating mocked OAuthServerService");
    AuthenticatedPrincipal authenticatedPrincipal = new AuthenticatedPrincipal();
    authenticatedPrincipal.setName("urn:collab:person:surfguest.nl:selenium-user");

    HashMap<String, String> attributes = new HashMap<>();
    attributes.put("email", "foo@bar.com");
    attributes.put("displayName", "John T Smith");
    authenticatedPrincipal.setAttributes(attributes);

    authenticatedPrincipal.setRoles(Collections.<String>emptyList());

    verifiedToken = new VerifiedToken(authenticatedPrincipal, Arrays.asList(NsiScope.values()));

  }

  @Override
  public Optional<VerifiedToken> getVerifiedToken(String accessToken) {
    return Optional.of(verifiedToken);
  }

  @Override
  public Collection<AccessToken> getAllAccessTokensForUser(BodAccount account) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public boolean deleteAccessToken(BodAccount account, String tokenId) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public Optional<AccessTokenResponse> getAdminAccessToken(String code, String redirectUri) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public Optional<AccessTokenResponse> getClientAccessToken(String code, String redirectUri) {
    throw new RuntimeException("not implemented");
  }

  @Override
  protected void setEnvironment(Environment environment) {
    throw new RuntimeException("not implemented");
  }
}
