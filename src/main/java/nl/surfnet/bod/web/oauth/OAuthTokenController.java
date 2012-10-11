package nl.surfnet.bod.web.oauth;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.domain.BodAccount;
import nl.surfnet.bod.repo.BodAccountRepo;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.Security;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Controller
@RequestMapping("/oauth2")
public class OAuthTokenController {

  private static final String CLIENT_REDIRECT = "/redirect";
  private static final String ADMIN_REDIRECT = "/authredirect";

  private final Logger logger = LoggerFactory.getLogger(OAuthTokenController.class);

  @Resource
  private BodAccountRepo bodAccountRepo;

  @Resource
  private Environment env;

  @Resource
  private OAuthServerService oAuthServerService;

  private final HttpClient httpClient = new DefaultHttpClient();

  @RequestMapping("/tokens")
  public String index(Model model) throws ClientProtocolException, IOException, URISyntaxException {
    final BodAccount account = bodAccountRepo.findByNameId(Security.getNameId());

    if (!account.getAuthorizationServerAccessToken().isPresent()) {
      return retreiveAuthorizationServerAccessToken();
    }

    Collection<AccessToken> tokens = oAuthServerService.getAllAccessTokensForUser(account);

    model.addAttribute("accessToken", Iterables.getFirst(tokens, null));

    return "oauthResult";
  }

  private String retreiveAuthorizationServerAccessToken() throws URISyntaxException {
    String uri = buildAuthorizeUri(
        env.getAdminClientId(),
        adminRedirectUri(),
        Lists.newArrayList("read", "write"));

    return "redirect:".concat(uri);
  }

  @RequestMapping(value = "/token/delete",  method = RequestMethod.DELETE)
  public String deleteAccessToken(@RequestParam String tokenId, Model model) {
    BodAccount account = bodAccountRepo.findByNameId(Security.getNameId());

    boolean deleted = oAuthServerService.deleteAccessToken(account, tokenId);

    if (deleted) {
      WebUtils.addInfoMessage(model, null, "Your Access Token has been revoked");
    }

    return "oauthResult";
  }

  @RequestMapping("/token")
  public String retreiveToken(Model model) throws URISyntaxException {
    String uri = buildAuthorizeUri(
        env.getClientClientId(),
        redirectUri(),
        Lists.newArrayList("reserve", "provision", "query", "release", "terminate"));

    return "redirect:".concat(uri);
  }

  private String buildAuthorizeUri(String clientId, String redirectUri, Collection<String> scopes) throws URISyntaxException {
    return new URIBuilder(env.getOauthServerUrl().concat("/oauth2/authorize"))
      .addParameter("response_type", "code")
      .addParameter("client_id", clientId)
      .addParameter("redirect_uri", redirectUri)
      .addParameter("scope", Joiner.on(',').join(scopes)).build().toASCIIString();
  }

  @RequestMapping(ADMIN_REDIRECT)
  public String authRedirect(HttpServletRequest request, Model model) throws ClientProtocolException, IOException {
    AccessTokenResponse tokenResponse = getAccessToken(
        env.getAdminClientId(),
        env.getAdminSecret(),
        request.getParameter("code"),
        adminRedirectUri()).get();

    BodAccount account = bodAccountRepo.findByNameId(Security.getNameId());
    account.setAuthorizationServerAccessToken(tokenResponse.getAccessToken());
    bodAccountRepo.save(account);

    return "redirect:/oauth2/tokens";
  }

  @RequestMapping(CLIENT_REDIRECT)
  public String redirect(HttpServletRequest request, Model model) {
    getAccessToken(
        env.getClientClientId(),
        env.getClientSecret(),
        request.getParameter("code"),
        redirectUri()).get();

    return "redirect:/oauth2/tokens";
  }

  private Optional<AccessTokenResponse> getAccessToken(String clientId, String secret, String code, String redirectUri) {
    try {
      List<NameValuePair> formparams = Lists.<NameValuePair>newArrayList(
        new BasicNameValuePair("grant_type", "authorization_code"),
        new BasicNameValuePair("code", code),
        new BasicNameValuePair("redirect_uri", redirectUri));
      UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");

      HttpPost post = new HttpPost(env.getOauthServerUrl().concat("/oauth2/token"));
      post.addHeader(OAuth2Helper.getBasicAuthorizationHeader(clientId, secret));
      post.setEntity(entity);

      logger.info("requesting access token to " + post.getURI());

      HttpResponse response = httpClient.execute(post);
      String responseJson = EntityUtils.toString(response.getEntity());

      return Optional.of(new ObjectMapper().readValue(responseJson, AccessTokenResponse.class));
    } catch(Exception e) {
      logger.error("Could not retreive access token", e);
      return Optional.absent();
    }
  }

  private String adminRedirectUri() {
   return env.getExternalBodUrl().concat("/oauth2" + ADMIN_REDIRECT);
  }

  private String redirectUri() {
   return env.getExternalBodUrl().concat("/oauth2" + CLIENT_REDIRECT);
  }

}
