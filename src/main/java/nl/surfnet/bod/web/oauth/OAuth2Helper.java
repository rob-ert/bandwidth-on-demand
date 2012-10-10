package nl.surfnet.bod.web.oauth;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public final class OAuth2Helper {

  private OAuth2Helper() {
  }

  public static Header getBasicAuthorizationHeader(String user, String password) {
    return new BasicHeader("Authorization", "Basic ".concat(base64Encoded(user.concat(":").concat(password))));
  }

  private static String base64Encoded(String input) {
    return new String(Base64.encodeBase64(input.getBytes()));
  }

  public static Header getOauthAuthorizationHeader(String accessToken) {
    return new BasicHeader("Authorization", "bearer ".concat(accessToken));
  }
}
