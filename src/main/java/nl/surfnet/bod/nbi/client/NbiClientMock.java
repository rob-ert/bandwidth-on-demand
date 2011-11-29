package nl.surfnet.bod.nbi.client;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import nl.surfnet.bod.nbi.client.generated.PortDetail;
import nl.surfnet.bod.nbi.client.generated.TerminationPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adventnet.security.authentication.RMIAccessException;

public class NbiClientMock implements NbiClient {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() throws RemoteException, RMIAccessException, MalformedURLException, NotBoundException,
      JAXBException {
    log.warn("U S I N G  M O C K  N B I  C L I E N T!");

  }

  @Override
  public List<TerminationPoint> findAllPorts() {
    ArrayList<TerminationPoint> terminationPoints = new ArrayList<TerminationPoint>();
    TerminationPoint tp = null;
    for (int index = 1; index < 10; index++) {
      tp = new TerminationPoint();
      tp.setPortDetail(new PortDetail());

      tp.getPortDetail().setName("name_" + index);
      tp.getPortDetail().setDisplayName("displayName_" + index);
      tp.getPortDetail().setPortId("portId_" + index);

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

  @Override
  public TerminationPoint findPortsByName(String name) {
    return findAllPorts().get(1);
  }

}
