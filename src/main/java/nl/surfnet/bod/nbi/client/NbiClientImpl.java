package nl.surfnet.bod.nbi.client;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import nl.surfnet.bod.nbi.client.generated.InventoryResponse;
import nl.surfnet.bod.nbi.client.generated.TerminationPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adventnet.security.authentication.RMIAccessAPI;
import com.adventnet.security.authentication.RMIAccessException;
import com.esm.server.api.oss.OSSHandle;
import com.googlecode.ehcache.annotations.Cacheable;

public class NbiClientImpl implements NbiClient {

  private final Logger log = LoggerFactory.getLogger(getClass());

  // @Value("#{nbiProperties.nbiUsername}")
  private String username;

  // @Value("#{nbiProperties.nbiPassword}")
  private String password;

  // @Value("#{nbiProperties.nbiUrl}")
  private String url;

  private JAXBContext jaxbContext;
  private Unmarshaller unMarshaller;
  private OSSHandle ossHandle = null;

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() throws RemoteException, RMIAccessException, MalformedURLException, NotBoundException,
      JAXBException {
    log.info("Connecting with username {} to: {}", username, url);
    final RMIAccessAPI rmiAccessApi = (RMIAccessAPI) Naming.lookup(url);

    log.info("Looked up EMS RMI access API: {}", rmiAccessApi);
    ossHandle = (OSSHandle) rmiAccessApi.getAPI(username, password, "OSSHandle");
    log.info("Looked up OSS handle: {}", ossHandle);

    jaxbContext = JAXBContext.newInstance("nl.surfnet.bod.nbi.client.generated");
    unMarshaller = jaxbContext.createUnmarshaller();

  }

  private List<TerminationPoint> findByFilter(final String filter) {
    try {
      log.debug("Retrieving by filter: {}", filter);
      final String allPortsXml = ossHandle.getInventory(username, password, "getResourcesWithAttributes", filter, null);
      log.debug("Retrieved all ports: {}", allPortsXml);
      return ((InventoryResponse) unMarshaller.unmarshal(new StringReader(allPortsXml))).getTerminationPoint();
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return null;
    }
  }

  @Override
  public List<TerminationPoint> findAllPorts() {
    return findByFilter("type=Port");
  }

  @Override
  public TerminationPoint findPortsByName(final String name) {
    final List<TerminationPoint> terminationPoints = findByFilter("name=" + name);
    if (terminationPoints == null) {
      return null;
    }
    if (terminationPoints.size() != 1) {
      throw new IllegalStateException(String.format("Termination point using name: %s, expected 1 actual %s", name,
          terminationPoints.size()));
    }
    return terminationPoints.get(0);

  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public void setUrl(final String url) {
    this.url = url;
  }

}
