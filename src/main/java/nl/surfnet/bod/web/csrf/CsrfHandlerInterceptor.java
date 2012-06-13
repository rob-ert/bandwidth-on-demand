package nl.surfnet.bod.web.csrf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.google.common.collect.ImmutableList;

public class CsrfHandlerInterceptor extends HandlerInterceptorAdapter {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    if (requestCanChangeData(request)) {
      String sessionToken = CsrfTokenManager.getTokenForSession(request.getSession());
      String requestToken = CsrfTokenManager.getTokenFromRequest(request);

      if (!sessionToken.equals(requestToken)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bad or missing CSRF token");
        return false;
      }
    }

    return true;
  }

  private boolean requestCanChangeData(HttpServletRequest request) {
    ImmutableList<String> changingMethods = ImmutableList.of("put", "post", "delete");

    return changingMethods.contains(request.getMethod().toLowerCase());
  }

}
