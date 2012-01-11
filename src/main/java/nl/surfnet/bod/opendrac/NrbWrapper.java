package nl.surfnet.bod.opendrac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.server.nrb.NrbInterface;
import com.nortel.appcore.app.drac.server.requesthandler.RemoteConnectionProxy;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;

public class NrbWrapper {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final RemoteConnectionProxy nrbProxy = new RemoteConnectionProxy();

  /**
   * 
   * @return the current active {@link NrbInterface} or <code>null</code> if an
   *         error occurred
   */
  public NrbInterface getNrbInterface() {
    try {
      return nrbProxy.getNrbInterface();
    }
    catch (RequestHandlerException e) {
      log.error("Error: ", e);
      return null;
    }
  }

}
