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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.BodAccount;
import nl.surfnet.bod.domain.oauth.AccessToken;
import nl.surfnet.bod.domain.oauth.AccessTokenResponse;
import nl.surfnet.bod.domain.oauth.AuthenticatedPrincipal;
import nl.surfnet.bod.domain.oauth.VerifyTokenResponse;
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
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

@Service
public class OAuthServerService {

  private final Logger logger = LoggerFactory.getLogger(OAuthServerService.class);

  private final static String URL_ADMIN_ACCESS_TOKEN = "/admin/accessToken/";

  @Resource
  private Environment env;

  private DefaultHttpClient httpClient = new DefaultHttpClient(new PoolingClientConnectionManager());

  public Optional<AuthenticatedPrincipal> getAuthenticatedPrincipal(String accessToken) {
    if (Strings.isNullOrEmpty(accessToken)) {
      return Optional.absent();
    }

    HttpGet httpGet = new HttpGet(getVerifyTokenUri(accessToken));
    try {
      httpGet.addHeader(getBasicAuthorizationHeader(env.getResourceKey(), env.getResourceSecret()));

      HttpResponse response = httpClient.execute(httpGet);
      String jsonResponse = EntityUtils.toString(response.getEntity(), Charsets.UTF_8);
      VerifyTokenResponse token = new ObjectMapper().readValue(jsonResponse, VerifyTokenResponse.class);

      return Optional.fromNullable(token.getPrincipal());
    }
    catch (IOException e) {
      logger.error("Could not verify access token", e);
      return Optional.absent();
    } finally {
      httpGet.releaseConnection();
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

    HttpGet httpGet = new HttpGet(env.getOauthServerUrl().concat(URL_ADMIN_ACCESS_TOKEN));
    try {
      httpGet.addHeader(getOauthAuthorizationHeader(account.getAuthorizationServerAccessToken().get()));

      HttpResponse tokensResponse = httpClient.execute(httpGet);
      StatusLine statusLine = tokensResponse.getStatusLine();

      if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
        logger.warn(String.format("Could not retreive access tokens from the auth server: %s - %s", statusLine.getStatusCode(), statusLine.getReasonPhrase()));
        return Collections.emptyList();
      }

      List<AccessToken> tokens = new ObjectMapper().readValue(EntityUtils.toString(tokensResponse.getEntity()), new TypeReference<List<AccessToken>>() { });

      return Collections2.filter(tokens, new Predicate<AccessToken>() {
        @Override
        public boolean apply(AccessToken token) {
          return token.getClient().getClientId().equals(env.getClientClientId());
        }
      });
    } catch (IOException e) {
      logger.error("Could not get access tokens for " + account.getNameId(), e);
      return Collections.emptyList();
    } finally {
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
      delete.releaseConnection();
      logger.error("Could not delete access token", e);
      return false;
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
    } catch(Exception e) {
      post.releaseConnection();
      logger.error("Could not retreive access token", e);
      return Optional.absent();
    }
  }

  protected void setEnvironment(Environment env) {
    this.env = env;
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
