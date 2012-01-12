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
  
  
  @PostConstruct
  private void init(){
    log.info("USING OFFLINE NBI CLIENT!");
  }

  private final List<PhysicalPort> ports = new ArrayList<PhysicalPort>() {
    {
      add(new PhysicalPort() {
        {
          setDisplayName("ETH-1-13-4");
          setName("00-21-E1-D6-D6-70_ETH-1-13-4");
        }
      });

      add(new PhysicalPort() {
        {
          setDisplayName("00-21-E1-D6-D6-70_ETH10G-1-13-1");
          setName("ETH10G-1-13-1");
        }
      });

      add(new PhysicalPort() {
        {
          setDisplayName("00-21-E1-D6-D6-70_ETH10G-1-13-2");
          setName("ETH10G-1-13-2");
        }
      });

      add(new PhysicalPort() {
        {
          setDisplayName("ETH-1-13-4");
          setName("00-21-E1-D6-D5-DC_ETH-1-13-4");
        }
      });

      add(new PhysicalPort() {
        {
          setDisplayName("ETH10G-1-13-1");
          setName("00-21-E1-D6-D5-DC_ETH10G-1-13-1");
        }
      });

      add(new PhysicalPort() {
        {
          setDisplayName("ETH10G-1-13-2");
          setName("00-21-E1-D6-D5-DC_ETH10G-1-13-2");
        }
      });
    }
  };

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
