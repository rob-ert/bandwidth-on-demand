/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.BodAccount;
import nl.surfnet.bod.domain.oauth.*;
import nl.surfnet.bod.repo.BodAccountRepo;
import nl.surfnet.bod.util.Environment;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.base.*;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

@Service
public class OAuthServerService {

  private static final String URL_ADMIN_ACCESS_TOKEN = "/admin/accessToken/";

  private final Logger logger = LoggerFactory.getLogger(OAuthServerService.class);

  @Resource(name = "bodEnvironment")
  private Environment env;

  @Resource
  private BodAccountRepo bodAccountRepo;

  private DefaultHttpClient httpClient = new DefaultHttpClient(new PoolingClientConnectionManager());

  public Optional<VerifiedToken> getVerifiedToken(String accessToken) {
    if (Strings.isNullOrEmpty(accessToken)) {
      return Optional.absent();
    }

    HttpGet httpGet = new HttpGet(getVerifyTokenUri(accessToken));
    httpGet.addHeader(getBasicAuthorizationHeader(env.getResourceKey(), env.getResourceSecret()));

    try {
      HttpResponse response = httpClient.execute(httpGet);
      final int statusCode = response.getStatusLine().getStatusCode();
      final String responseEntity = EntityUtils.toString(response.getEntity(), Charsets.UTF_8);

      if (statusCode == HttpStatus.SC_OK) {
        return readJsonToken(responseEntity);
      }
      else {
        logger.warn("Verify token response was {}, {}", statusCode, responseEntity);
        return Optional.absent();
      }
    }
    catch (IOException e) {
      logger.error("Could not verify access token", e);
      return Optional.absent();
    }
    finally {
      httpGet.releaseConnection();
    }
  }

  private Optional<VerifiedToken> readJsonToken(String jsonResponse) throws JsonParseException, JsonMappingException, IOException {
      VerifyTokenResponse token = new ObjectMapper().readValue(jsonResponse, VerifyTokenResponse.class);

      if (token.getPrincipal() != null) {
        Collection<NsiScope> scopes = FluentIterable.from(token.getScopes()).transform(new Function<String, NsiScope>() {
          @Override
          public NsiScope apply(String scope) {
            return NsiScope.valueOf(scope.toUpperCase());
          }
        }).toImmutableList();
        return Optional.of(new VerifiedToken(token.getPrincipal(), scopes));
      } else {
        logger.error("Verify token response gave an error '{}'", token.getError());
        return Optional.absent();
      }
  }

  private String getVerifyTokenUri(String accessToken) {
    try {
      return new URIBuilder(env.getOauthServerUrl().concat("/v1/tokeninfo"))
        .addParameter("access_token", accessToken).build().toASCIIString();
    }
    catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public Collection<AccessToken> getAllAccessTokensForUser(BodAccount account) {
    checkNotNull(account);
    checkArgument(account.getAuthorizationServerAccessToken().isPresent());

    HttpGet httpGet = new HttpGet(env.getOauthServerUrl().concat(URL_ADMIN_ACCESS_TOKEN));
    try {
      httpGet.addHeader(getOauthAuthorizationHeader(account.getAuthorizationServerAccessToken().get()));

      HttpResponse tokensResponse = httpClient.execute(httpGet);
      StatusLine statusLine = tokensResponse.getStatusLine();

      if (statusLine.getStatusCode() == HttpStatus.SC_FORBIDDEN) {
        logger.warn("The access token for the admin interface is not valid");
        account.removeAuthorizationServerAccessToken();
        bodAccountRepo.save(account);
        return Collections.emptyList();
      }
      if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
        logger.warn(String.format(
            "Could not retreive access tokens from the auth server: %s - %s",
            statusLine.getStatusCode(), statusLine.getReasonPhrase()));
        return Collections.emptyList();
      }

      List<AccessToken> tokens = new ObjectMapper().readValue(
          EntityUtils.toString(tokensResponse.getEntity()),
          new TypeReference<List<AccessToken>>() { });

      return Collections2.filter(tokens, new Predicate<AccessToken>() {
        @Override
        public boolean apply(AccessToken token) {
          return token.getClientId().equals(env.getClientClientId());
        }
      });
    }
    catch (IOException e) {
      logger.error("Could not get access tokens for " + account.getNameId(), e);
      return Collections.emptyList();
    }
    finally {
      httpGet.releaseConnection();
    }
  }

  public boolean deleteAccessToken(BodAccount account, String tokenId) {
    checkNotNull(account);

    HttpDelete delete = new HttpDelete(env.getOauthServerUrl().concat(URL_ADMIN_ACCESS_TOKEN).concat(tokenId));
    try {
      delete.addHeader(getOauthAuthorizationHeader(account.getAuthorizationServerAccessToken().get()));
      HttpResponse response = httpClient.execute(delete);

      int statusCode = response.getStatusLine().getStatusCode();
      delete.releaseConnection();

      if (statusCode != HttpStatus.SC_NO_CONTENT) {
        throw new RuntimeException(String.format("Expected %s but was %s", HttpStatus.SC_NO_CONTENT, statusCode));
      }

      return true;
    }
    catch (IOException e) {
      logger.error("Could not delete access token", e);
      return false;
    }
    finally {
      delete.releaseConnection();
    }
  }

  public Optional<AccessTokenResponse> getAdminAccessToken(String code, String redirectUri) {
    return getAccessToken(code, redirectUri, env.getAdminClientId(), env.getAdminSecret());
  }

  public Optional<AccessTokenResponse> getClientAccessToken(String code, String redirectUri) {
    return getAccessToken(code, redirectUri, env.getClientClientId(), env.getClientSecret());
  }

  private Optional<AccessTokenResponse> getAccessToken(String code, String redirectUri, String clientId, String secret) {
    HttpPost post = new HttpPost(env.getOauthServerUrl().concat("/oauth2/token"));
    logger.info("requesting access token to " + post.getURI());

    try {
      List<NameValuePair> formparams = Lists.<NameValuePair>newArrayList(
        new BasicNameValuePair("grant_type", "authorization_code"),
        new BasicNameValuePair("code", code),
        new BasicNameValuePair("redirect_uri", redirectUri));

      UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
      post.addHeader(getBasicAuthorizationHeader(clientId, secret));
      post.setEntity(entity);

      HttpResponse response = httpClient.execute(post);
      String responseJson = EntityUtils.toString(response.getEntity());

      return Optional.of(new ObjectMapper().readValue(responseJson, AccessTokenResponse.class));
    }
    catch (Exception e) {
      post.releaseConnection();
      logger.error("Could not retreive access token", e);
      return Optional.absent();
    }
  }

  protected void setEnvironment(Environment environment) {
    this.env = environment;
  }

  private Header getBasicAuthorizationHeader(String user, String password) {
    return new BasicHeader("Authorization", "Basic ".concat(base64Encoded(user.concat(":").concat(password))));
  }

  private String base64Encoded(String input) {
    return new String(Base64.encodeBase64(input.getBytes()));
  }

  private Header getOauthAuthorizationHeader(String accessToken) {
    return new BasicHeader("Authorization", "bearer ".concat(accessToken));
  }
}
