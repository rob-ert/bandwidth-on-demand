package nl.surfnet.bod.web;

import static nl.surfnet.bod.util.ShibbolethConstants.COMMON_NAME;
import static nl.surfnet.bod.util.ShibbolethConstants.NAME_ID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.surfnet.bod.util.UserContext;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class LoginInterceptor extends HandlerInterceptorAdapter {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    HttpSession session = request.getSession();

    if (session.getAttribute("userContext") == null) {
      session.setAttribute("userContext", createUserContext(request));
    }

    return super.preHandle(request, response, handler);
  }

  private UserContext createUserContext(HttpServletRequest request) {
    String nameId = (String) request.getAttribute(NAME_ID);
    String userName = (String) request.getAttribute(COMMON_NAME);

    return new UserContext(nameId, userName);
  }
}
