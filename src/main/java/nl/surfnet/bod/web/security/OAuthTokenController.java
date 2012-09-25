package nl.surfnet.bod.web.security;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.domain.BodAccount;
import nl.surfnet.bod.repo.BodAccountRepo;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Controller
@RequestMapping("/oauth2")
public class OAuthTokenController {

  private final Logger logger = LoggerFactory.getLogger(OAuthTokenController.class);

  @Resource
  private BodAccountRepo bodAccountRepo;

  private final HttpClient httpClient = new DefaultHttpClient();

  private final String authServerClientId = "bod-authorization-server-client";
  private final String authServerClientSecret = "BodSecret10";
  private final String authServerRedirectUrl = "http://localhost:8082/bod/oauth2/authredirect";

  private final String clientId = "osilient";
  private final String secret = "ce6bca65-717e-4c8f-a974-1088b278e08d";
  private final String redirectUrl = "http://localhost:8082/bod/oauth2/redirect";

  private final String resourceSecret = "87f8bc46-9f58-4a2e-b439-8d6d99bd3ab3";
  private final String resourceKey = "86e05d28-ea62-4b82-8090-250198facca3";

  @RequestMapping("/tokens")
  public String index(Model model) throws ClientProtocolException, IOException, URISyntaxException {
    final BodAccount account = bodAccountRepo.findByNameId(Security.getNameId());

    if (!account.getAuthorizationServerAccessToken().isPresent()) {
      return retreiveAnAuthorizationServerAccessToken();
    }

    HttpGet httpGet = new HttpGet("http://localhost:8080/admin/accessToken/");
    httpGet.addHeader("Authorization", "bearer ".concat(account.getAuthorizationServerAccessToken().get()));

    HttpResponse tokensResponse = httpClient.execute(httpGet);
    String json = EntityUtils.toString(tokensResponse.getEntity());

    List<AccessToken> tokens = new ObjectMapper().readValue(json, new TypeReference<List<AccessToken>>() { });

    AccessToken accessToken = Iterables.find(tokens, new Predicate<AccessToken>() {
      @Override
      public boolean apply(AccessToken token) {
        if (!account.getAccessToken().isPresent()) {
          return false;
        }
        return token.getToken().equals(account.getAccessToken().get());
      }
    }, null);

    model.addAttribute("accessToken", accessToken);

    return "oauthResult";
  }

  private String retreiveAnAuthorizationServerAccessToken() throws URISyntaxException {
    URI uri = new URIBuilder().setScheme("http").setHost("localhost").setPort(8080).setPath("/oauth2/authorize")
      .setParameter("response_type", "code")
      .setParameter("client_id", authServerClientId)
      .setParameter("redirect_uri", authServerRedirectUrl)
      .setParameter("scope", "read,write")
      .build();

    return "redirect:".concat(uri.toString());
  }

  @Transactional
  @RequestMapping(value = "/token/delete",  method = RequestMethod.DELETE)
  public String deleteAccessToken(@RequestParam String tokenId) throws ClientProtocolException, IOException {
    BodAccount account = bodAccountRepo.findByNameId(Security.getNameId());

    HttpDelete delete = new HttpDelete("http://localhost:8080/admin/accessToken/".concat(tokenId));
    delete.addHeader("Authorization", "bearer ".concat(account.getAuthorizationServerAccessToken().get()));

    HttpResponse response = httpClient.execute(delete);
    int statusCode = response.getStatusLine().getStatusCode();
    delete.releaseConnection();

    if (statusCode != 204) {
      throw new RuntimeException("Expected 204 but was " + statusCode);
    }

    account.removeAccessToken();
    bodAccountRepo.save(account);

    return "oauthResult";
  }


  @RequestMapping("/token")
  public String retreiveToken(Model model) throws URISyntaxException {
    BodAccount account = bodAccountRepo.findByNameId(Security.getNameId());

    if (account.getAccessToken().isPresent()) {
      return "oauthResult";
    }

    URI uri = new URIBuilder("http://localhost:8080/oauth2/authorize")
      .addParameter("response_type", "code")
      .addParameter("client_id", clientId)
      .addParameter("redirect_uri", redirectUrl)
      .addParameter("scope", "reserve,provision,query,release").build();

    return "redirect:".concat(uri.toASCIIString());
  }

  @RequestMapping("/authredirect")
  public String authRedirect(HttpServletRequest request, Model model) throws ClientProtocolException, IOException {
    String code = request.getParameter("code");

    AccessTokenResponse tokenResponse = getAccessToken(authServerClientId, authServerClientSecret, code, authServerRedirectUrl).get();

    BodAccount account = bodAccountRepo.findByNameId(Security.getUserDetails().getNameId());
    account.setAuthorizationServerAccessToken(tokenResponse.getAccessToken());
    bodAccountRepo.save(account);

    return "redirect:/oauth2/tokens";
  }

  @RequestMapping("/redirect")
  public String redirect(HttpServletRequest request, Model model) {
    String code = request.getParameter("code");

    AccessTokenResponse accessToken = getAccessToken(clientId, secret, code, redirectUrl).get();

    BodAccount account = bodAccountRepo.findByNameId(Security.getNameId());
    account.setAccessToken(accessToken.getAccessToken());
    bodAccountRepo.save(account);

    return "redirect:/oauth2/tokens";
  }

  private Optional<AccessTokenResponse> getAccessToken(String clientId, String secret, String code, String redirectUri) {
    try {
      List<NameValuePair> formparams = Lists.<NameValuePair>newArrayList(
        new BasicNameValuePair("grant_type", "authorization_code"),
        new BasicNameValuePair("code", code),
        new BasicNameValuePair("redirect_uri", redirectUri));
      UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");

      HttpPost post = new HttpPost("http://localhost:8080/oauth2/token");
      post.addHeader("Authorization", "Basic " + base64EncodedAuthorizationString(clientId, secret));
      post.setEntity(entity);

      HttpResponse response = httpClient.execute(post);
      String responseJson = EntityUtils.toString(response.getEntity());

      return Optional.of(new ObjectMapper().readValue(responseJson, AccessTokenResponse.class));
    } catch(Exception e) {
      logger.error("Could not retreive access token", e);
      return Optional.absent();
    }
  }

  private String base64EncodedAuthorizationString(String key, String secret) {
    return new String(Base64.encodeBase64(key.concat(":").concat(secret).getBytes()));
  }


}
