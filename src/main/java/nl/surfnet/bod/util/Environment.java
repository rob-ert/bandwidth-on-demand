package nl.surfnet.bod.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Environment {

  @Value("${shibboleth.imitate}")
  private boolean imitateShibboleth;

  @Value("${os.url}")
  private String openSocialUrl;

  @Value("${os.oauth-key}")
  private String openSocialOAuthKey;

  @Value("${os.oauth-secret}")
  private String openSocialOAuthSecret;

  @Value("${shibboleth.imitate.nameid}")
  private String mockShibbolethNameId;

  @Value("${shibboleth.imitate.username}")
  private String mockShibbolethUserName;

  public String getMockShibbolethNameId() {
    return mockShibbolethNameId;
  }

  public String getMockShibbolethUserName() {
    return mockShibbolethUserName;
  }

  public String getOpenSocialUrl() {
    return openSocialUrl;
  }

  public String getOpenSocialOAuthKey() {
    return openSocialOAuthKey;
  }

  public String getOpenSocialOAuthSecret() {
    return openSocialOAuthSecret;
  }

  public boolean getImitateShibboleth() {
    return imitateShibboleth;
  }

}
