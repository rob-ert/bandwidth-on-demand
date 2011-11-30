package nl.surfnet.bod.nbi;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import nl.surfnet.bod.nbi.generated.PortDetail;
import nl.surfnet.bod.nbi.generated.TerminationPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adventnet.security.authentication.RMIAccessException;

public class NbiOfflineClient implements NbiClient {

  private final Logger log = LoggerFactory.getLogger(getClass());

  public static final String NAME_PREFIX="Name_";
  public static final String DISPLAY_NAME_PREFIX = "DisplayName_";
  public static final String PORTID_PREFIX = "PortId_";
  
  
  @SuppressWarnings("unused")
  @PostConstruct
  private void init() throws RemoteException, RMIAccessException, MalformedURLException, NotBoundException,
      JAXBException {
    log.info("USING OFFLINE NBI CLIENT!");

  }

  /**
   *
   * @param name
   *          The name of the port
   * @return A {@link TerminationPoint} or <code>null</code> if nothing was
   *         found.
   */
  @Override
  public TerminationPoint findPortsByName(String name) {
    return findAllPorts().get(1);
  }

  @Override
  public List<TerminationPoint> findAllPorts() {
    ArrayList<TerminationPoint> terminationPoints = new ArrayList<TerminationPoint>();
    TerminationPoint tp = null;
    for (int index = 0; index < 10; index++) {
      tp = new TerminationPoint();
      tp.setPortDetail(new PortDetail());

      tp.getPortDetail().setName(NAME_PREFIX+ index);
      tp.getPortDetail().setDisplayName(DISPLAY_NAME_PREFIX + index);
      tp.getPortDetail().setPortId(PORTID_PREFIX + index);

      terminationPoints.add(tp);
    }

    return terminationPoints;
  }

  public void setUsername(final String username) {
  }

  public void setPassword(final String password) {
  }

  public void setUrl(final String url) {
  }

}
