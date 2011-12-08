package nl.surfnet.bod.web;

import static com.google.common.base.Strings.nullToEmpty;
import static nl.surfnet.bod.util.ShibbolethConstants.COMMON_NAME;
import static nl.surfnet.bod.util.ShibbolethConstants.NAME_ID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.util.UserContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class ShibbolethImitatorInterceptor extends HandlerInterceptorAdapter {

  @Autowired
  private Environment env;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    return env.getImitateShibboleth()
        ? imitateShibbolethLogin(request, response, handler)
        : super.preHandle(request, response, handler);
  }

  private boolean imitateShibbolethLogin(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String requestUserName = nullToEmpty(request.getParameter("user-name"));
    String requestNameId = nullToEmpty(request.getParameter("name-id"));

    if (!requestUserName.isEmpty() && !requestNameId.isEmpty()) {
      request.getSession().removeAttribute("userContext");
      setShibbolethRequestAttributes(request, requestUserName, requestNameId);
    }
    else {
      UserContext userContext = (UserContext) request.getSession().getAttribute("userContext");
      if (userContext != null) {
        setShibbolethRequestAttributes(request, userContext.getUserName(), userContext.getNameId());
      }
      else if (!request.getRequestURI().endsWith("login")) {
        response.sendRedirect(request.getContextPath() + "/shibboleth/login");
        return false;
      }
    }

    return super.preHandle(request, response, handler);
  }

  private void setShibbolethRequestAttributes(HttpServletRequest request, String userName, String nameId) {
    request.setAttribute(COMMON_NAME, userName);
    request.setAttribute(NAME_ID, nameId);
  }

}
