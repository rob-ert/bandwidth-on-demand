package nl.surfnet.bod.web.csrf;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public final class CsrfTokenManager {

  public static final String CSRF_PARAM_NAME = "csrf-token";
  private static final String CSRF_SESSION_ATTR_NAME = "csrf-token";

  private CsrfTokenManager() {
  }

  public static String getTokenForSession(HttpSession session) {
    synchronized (session) {
      String token = (String) session.getAttribute(CSRF_SESSION_ATTR_NAME);
      if (token == null) {
        token = UUID.randomUUID().toString();
        session.setAttribute(CSRF_SESSION_ATTR_NAME, token);
      }
      return token;
    }
  }

  public static String getTokenFromRequest(HttpServletRequest request) {
    return request.getParameter(CSRF_PARAM_NAME);
  }
}
