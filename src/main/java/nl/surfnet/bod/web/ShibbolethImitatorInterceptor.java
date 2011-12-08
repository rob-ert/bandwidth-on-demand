package nl.surfnet.bod.web;

import static nl.surfnet.bod.util.ShibbolethConstants.COMMON_NAME;
import static nl.surfnet.bod.util.ShibbolethConstants.NAME_ID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.surfnet.bod.util.Environment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class ShibbolethImitatorInterceptor extends HandlerInterceptorAdapter {

  @Autowired
  private Environment env;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if (env.getImitateShibboleth()) {
      imitateShibboleth(request);
    }

    return super.preHandle(request, response, handler);
  }

  private void imitateShibboleth(HttpServletRequest request) {
    request.setAttribute(COMMON_NAME, env.getMockShibbolethUserName());
    request.setAttribute(NAME_ID, env.getMockShibbolethNameId());
  }

}
