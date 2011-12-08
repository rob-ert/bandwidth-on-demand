package nl.surfnet.bod.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Environment {

  @Value("${shibboleth.imitate}")
  private boolean imitateShibboleth;

  public boolean isImitateShibboleth() {
    return imitateShibboleth;
  }

  public void setImitateShibboleth(boolean imitateShibboleth) {
    this.imitateShibboleth = imitateShibboleth;
  }

}
