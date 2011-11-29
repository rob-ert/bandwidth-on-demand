package nl.surfnet.bod.service;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * Service implementation which combines {@link PhysicalPort} services using the
 * {@link PhysicalPortServiceNbiImpl} and the
 * {@link PhysicalPortServiceRepoImpl}.
 * 
 * The {@link PhysicalPort}s found in the {@link PhysicalPortServiceNbiImpl} are
 * leading and when more data is available in our repository they will be
 * enriched using this data.
 * 
 * Since {@link PhysicalPort}s from the {@link PhysicalPortServiceNbiImpl} are
 * considered readonly, the methods that change data are performed using the
 * {@link PhysicalPortServiceRepoImpl}.
 * 
 * 
 * @author Frank Mölder ($Author$)
 * @version $Revision$ $Date$
 */

@Service("physicalPortServiceImpl")
public class PhysicalPortServiceImpl implements PhysicalPortService {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  @Qualifier("physicalPortServiceRepoImpl")
  private PhysicalPortService physicalPortServiceRepoImpl;

  @Autowired
  @Qualifier("physicalPortServiceNbiImpl")
  private PhysicalPortService physicalPortServiceNbiImpl;

  /**
   * 
   * Finds all ports using the North Bound Interface and enhances these ports
   * with data found in our own database.
   */
  @Override
  public List<PhysicalPort> findAll() {
    // retrieve from nbi
    List<PhysicalPort> nbiPorts = physicalPortServiceNbiImpl.findAll();

    // retrieve from repo
    List<PhysicalPort> repoPorts = physicalPortServiceRepoImpl.findAll();

    enrichPorts(nbiPorts, repoPorts);

    return nbiPorts;
  }

  @Override
  public List<PhysicalPort> findEntries(final int firstResult, final int sizeNo) {
    List<PhysicalPort> nbiPorts = physicalPortServiceNbiImpl.findEntries(firstResult, sizeNo);
    // Find all repo ports, since the number of ports in both services may
    // not be equal
    List<PhysicalPort> repoPorts = physicalPortServiceRepoImpl.findAll();

    enrichPorts(nbiPorts, repoPorts);

    return nbiPorts;
  }

  @Override
  public long count() {
    return physicalPortServiceNbiImpl.count();
  }

  @Override
  public PhysicalPort findByName(final String name) {
    PhysicalPort nbiPort = physicalPortServiceNbiImpl.findByName(name);

    PhysicalPort repoPort = physicalPortServiceRepoImpl.findByName(name);

    enrichPortWithPort(nbiPort, repoPort);

    return nbiPort;
  }

  @Override
  public void delete(final PhysicalPort physicalPort) {
    physicalPortServiceRepoImpl.delete(physicalPort);
  }

  @Override
  public PhysicalPort find(final Long id) {
    return physicalPortServiceRepoImpl.find(id);
  }

  @Override
  public void save(final PhysicalPort physicalPort) {
    physicalPortServiceRepoImpl.save(physicalPort);

  }

  @Override
  public PhysicalPort update(final PhysicalPort physicalPort) {
    return physicalPortServiceRepoImpl.update(physicalPort);
  }

  /**
   * Adds data found in given ports to the specified ports, enriches them.
   * 
   * @param portsToEnrich
   *          {@link PhysicalPort}s to add the data to
   * @param dataPorts
   *          {@link PhysicalPort}s containing additional data
   */
  void enrichPorts(final List<PhysicalPort> portsToEnrich, final List<PhysicalPort> dataPorts) {
    // Iterate of the nbiPorts and find matching repoPort
    for (final PhysicalPort portToEnrich : portsToEnrich) {

      if (!CollectionUtils.isEmpty(dataPorts)) {
        Collection<PhysicalPort> filteredPorts = Collections2.filter(dataPorts, new Predicate<PhysicalPort>() {
          @Override
          public boolean apply(final PhysicalPort port) {
            boolean found = false;
            if ((StringUtils.hasText(portToEnrich.getName())) && (portToEnrich.getName().equals(port.getName()))) {
              found = true;
            }

            return found;
          };
        });

        if (!CollectionUtils.isEmpty(filteredPorts)) {
          if (filteredPorts.size() == 1) {
            // Enrich nbiports with data from repoPort
            enrichPortWithPort(portToEnrich, filteredPorts.iterator().next());
          }
          else {
            throw new IllegalStateException("Name is not unique. Found [" + filteredPorts.size()
                + "] ports with name: " + portToEnrich.getName());
          }
        }
      }
      else {
        log.debug("No ports found in repository");
      }
    }
  }

  /**
   * Enriches the port with additional data.
   * 
   * @param portToEnrich
   *          The port to enrich
   * @param dataPort
   *          The data to enrich with.
   */
  void enrichPortWithPort(final PhysicalPort portToEnrich, final PhysicalPort dataPort) {
    if ((portToEnrich != null) && (dataPort != null)) {
      portToEnrich.setPhysicalResourceGroup(dataPort.getPhysicalResourceGroup());
    }
  }

}
