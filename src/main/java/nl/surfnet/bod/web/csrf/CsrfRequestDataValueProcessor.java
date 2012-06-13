package nl.surfnet.bod.web.csrf;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestDataValueProcessor;

import com.google.common.collect.ImmutableMap;

@Component(value = "requestDataValueProcessor")
public class CsrfRequestDataValueProcessor implements RequestDataValueProcessor {

  @Override
  public String processAction(HttpServletRequest request, String action) {
    return action;
  }

  @Override
  public String processFormFieldValue(HttpServletRequest request, String name, String value, String type) {
    return value;
  }

  @Override
  public Map<String, String> getExtraHiddenFields(HttpServletRequest request) {
    return new ImmutableMap.Builder<String, String>().put(
        CsrfTokenManager.CSRF_PARAM_NAME,
        CsrfTokenManager.getTokenForSession(request.getSession())).build();
  }

  @Override
  public String processUrl(HttpServletRequest request, String url) {
    return url;
  }

}
