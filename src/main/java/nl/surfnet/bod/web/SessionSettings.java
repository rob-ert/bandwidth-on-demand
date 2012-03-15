package nl.surfnet.bod.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which manages settings related to the user session
 * 
 * @author Franky
 * 
 */
public class SessionSettings {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  //@Value("${error.details.show}")
  private boolean showErrorDetails = true;

  public SessionSettings() {
    log.info("SessionSettings init");
  }

  public boolean isShowErrorDetails() {
    log.info("sessionSettings errorDetails");
    return showErrorDetails;
  }

}
