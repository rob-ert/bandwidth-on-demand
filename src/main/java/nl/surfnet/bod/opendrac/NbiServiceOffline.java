package nl.surfnet.bod.opendrac;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.service.NbiPortService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.nrb.NrbInterface;

/**
 * A wrapper 'service' around OpenDRAC's {@link NrbInterface}. The main
 * difference is that the methods in this class use a {@link LoginToken} instead
 * of a clear text password.
 * 
 * @author robert
 * 
 */
// @Service("nbiClient")
public class NbiServiceOffline implements NbiPortService {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final List<PhysicalPort> ports = new ArrayList<PhysicalPort>();

  @PostConstruct
  private void init() {
    log.info("USING OFFLINE NBI CLIENT!");

    final PhysicalPort physicalPort1 = new PhysicalPort();
    physicalPort1.setDisplayName("ETH-1-13-4");
    physicalPort1.setName("00-21-E1-D6-D6-70_ETH-1-13-4");
    ports.add(physicalPort1);

    final PhysicalPort physicalPort2 = new PhysicalPort();
    physicalPort2.setDisplayName("ETH10G-1-13-1");
    physicalPort2.setName("00-21-E1-D6-D6-70_ETH10G-1-13-1");
    ports.add(physicalPort2);

    final PhysicalPort physicalPort3 = new PhysicalPort();
    physicalPort3.setDisplayName("ETH10G-1-13-2");
    physicalPort3.setName("00-21-E1-D6-D6-70_ETH10G-1-13-2");
    ports.add(physicalPort3);

    final PhysicalPort physicalPort4 = new PhysicalPort();
    physicalPort4.setDisplayName("ETH-1-13-4");
    physicalPort4.setName("00-21-E1-D6-D5-DC_ETH-1-13-4");
    ports.add(physicalPort4);

    final PhysicalPort physicalPort5 = new PhysicalPort();
    physicalPort5.setDisplayName("ETH10G-1-13-1");
    physicalPort5.setName("00-21-E1-D6-D5-DC_ETH10G-1-13-1");
    ports.add(physicalPort5);

    final PhysicalPort physicalPort6 = new PhysicalPort();
    physicalPort6.setDisplayName("ETH10G-1-13-2");
    physicalPort6.setName("00-21-E1-D6-D5-DC_ETH10G-1-13-2");
    ports.add(physicalPort6);
  }

  @Override
  public List<PhysicalPort> findAll() {
    return ports;

  }

  @Override
  public long count() {
    return ports.size();
  }

  @Override
  public PhysicalPort findByName(String name) {
    for (final PhysicalPort port : ports) {
      if (port.getName().equals(name)) {
        return port;
      }
    }
    return null;
  }
}
