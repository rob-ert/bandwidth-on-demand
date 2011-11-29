package nl.surfnet.bod.web;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

public final class HttpRequestUtils {

  private HttpRequestUtils() {
  }

  public static String encodeUrlPathSegment(String pathSegment, final HttpServletRequest httpServletRequest) {
    String enc = httpServletRequest.getCharacterEncoding();
    if (enc == null) {
      enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
    }

    try {
      pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
    }
    catch (UnsupportedEncodingException uee) {
    }

    return pathSegment;
  }

}
