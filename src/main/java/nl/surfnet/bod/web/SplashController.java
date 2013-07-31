package nl.surfnet.bod.web;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/splash")
public class SplashController {

  @RequestMapping(method = RequestMethod.GET)
  public String showSplash(final HttpServletRequest request,
                           @CookieValue(value = "skipSplash", required = false) final String skipSplash) throws UnsupportedEncodingException {
    if ("1".equals(skipSplash)){
      String targetUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
      return "redirect:/Shibboleth.sso/Login?target=" + targetUrl;
    }
    return "splash";
  }
}
