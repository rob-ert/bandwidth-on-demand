package nl.surfnet.bod.web.oauth;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.BodAccount;
import nl.surfnet.bod.util.Environment;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
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

@Service
public class OAuthServerService {

  private final Logger logger = LoggerFactory.getLogger(OAuthServerService.class);

  private final static String URL_ADMIN_ACCESS_TOKEN = "/admin/accessToken/";

  @Resource
  private Environment env;

  private DefaultHttpClient httpClient = new DefaultHttpClient();

  public Optional<AuthenticatedPrincipal> getAuthenticatedPrincipal(String accessToken) {
    if (Strings.isNullOrEmpty(accessToken)) {
      return Optional.absent();
    }

    try {
      String uri = new URIBuilder(env.getOauthServerUrl().concat("/v1/tokeninfo"))
        .addParameter("access_token", accessToken).build().toASCIIString();

      HttpGet httpGet = new HttpGet(uri);
      httpGet.addHeader(OAuth2Helper.getBasicAuthorizationHeader(env.getResourceKey(), env.getResourceSecret()));

      HttpResponse response = httpClient.execute(httpGet);
      String jsonResponse = EntityUtils.toString(response.getEntity(), Charsets.UTF_8);
      VerifyTokenResponse token = new ObjectMapper().readValue(jsonResponse, VerifyTokenResponse.class);

      return Optional.fromNullable(token.getPrincipal());
    }
    catch (IOException | URISyntaxException e) {
      logger.error("Could not verify access token", e);
      return Optional.absent();
    }
  }

  public Collection<AccessToken> getAllAccessTokensForUser(BodAccount account) {
    checkNotNull(account);

    try {
      HttpGet httpGet = new HttpGet(env.getOauthServerUrl().concat(URL_ADMIN_ACCESS_TOKEN));
      httpGet.addHeader(OAuth2Helper.getOauthAuthorizationHeader(account.getAuthorizationServerAccessToken().get()));

      HttpResponse tokensResponse = httpClient.execute(httpGet);
      StatusLine statusLine = tokensResponse.getStatusLine();

      if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
        logger.warn(String.format("Could not retreive access tokens from the auth server: %s - %s", statusLine.getStatusCode(), statusLine.getReasonPhrase()));
        httpGet.releaseConnection();
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
    }
  }

  public boolean deleteAccessToken(BodAccount account, String tokenId) {
    checkNotNull(account);

    try {
      HttpDelete delete = new HttpDelete(env.getOauthServerUrl().concat(URL_ADMIN_ACCESS_TOKEN).concat(tokenId));
      delete.addHeader(OAuth2Helper.getOauthAuthorizationHeader(account.getAuthorizationServerAccessToken().get()));
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
  }

  protected void setEnvironment(Environment env) {
    this.env = env;
  }
}
