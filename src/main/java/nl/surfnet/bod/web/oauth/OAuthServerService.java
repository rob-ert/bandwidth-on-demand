package nl.surfnet.bod.web.oauth;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.annotation.Resource;

import nl.surfnet.bod.util.Environment;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

@Service
public class OAuthServerService {

  private final Logger logger = LoggerFactory.getLogger(OAuthServerService.class);

  @Resource
  private Environment env;

  public Optional<AuthenticatedPrincipal> getAuthenticatedPrincipal(String accessToken) {
    if (Strings.isNullOrEmpty(accessToken)) {
      return Optional.absent();
    }

    try {
      String uri = new URIBuilder(env.getOauthServerUrl().concat("/v1/tokeninfo"))
        .addParameter("access_token", accessToken).build().toASCIIString();

      DefaultHttpClient client = new DefaultHttpClient();
      HttpGet httpGet = new HttpGet(uri);
      httpGet.addHeader(OAuth2Helper.getBasicAuthorizationHeader(env.getResourceKey(), env.getResourceSecret()));

      HttpResponse response = client.execute(httpGet);
      String jsonResponse = EntityUtils.toString(response.getEntity(), Charsets.UTF_8);
      VerifyTokenResponse token = new ObjectMapper().readValue(jsonResponse, VerifyTokenResponse.class);

      return Optional.fromNullable(token.getPrincipal());
    }
    catch (IOException | URISyntaxException e) {
      logger.error("Could not verify access token", e);
      return Optional.absent();
    }
  }

  protected void setEnvironment(Environment env) {
    this.env = env;
  }

}
