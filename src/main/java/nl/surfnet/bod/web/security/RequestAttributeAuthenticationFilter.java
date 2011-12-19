package nl.surfnet.bod.web.security;

import static com.google.common.base.Strings.nullToEmpty;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.util.ShibbolethConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

public class RequestAttributeAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

  private final Logger logger = LoggerFactory.getLogger(RequestAttributeAuthenticationFilter.class);

  @Autowired
  private Environment env;

  @Override
  protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
    String nameId = getRequestAttributeOrImitate(request, ShibbolethConstants.NAME_ID, "urn:surfguest:donaldduck");
    String displayName = getRequestAttributeOrImitate(request, ShibbolethConstants.DISPLAY_NAME, "Donald Duck");

    logger.debug("Found Shibboleth name-id: {}, displayName: {}", nameId, displayName);

    return new RichPrincipal(nameId, displayName);
  }

  private String getRequestAttributeOrImitate(HttpServletRequest request, String attribute, String imitateValue) {
    String value = nullToEmpty((String) request.getAttribute(attribute));

    return env.getImitateShibboleth() && value.isEmpty() ? imitateValue : value;
  }

  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    return "N/A";
  }

}
