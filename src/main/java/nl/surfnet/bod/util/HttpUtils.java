package nl.surfnet.bod.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public class HttpUtils {
  public static Header getBasicAuthorizationHeader(String user, String password) {
    return new BasicHeader("Authorization", "Basic ".concat(base64Encoded(user.concat(":").concat(password))));
  }

  public static String base64Encoded(String input) {
    return new String(Base64.encodeBase64(input.getBytes()));
  }
}
