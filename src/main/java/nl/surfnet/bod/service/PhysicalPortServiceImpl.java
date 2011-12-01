package nl.surfnet.bod.service;

import static java.lang.String.format;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
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

  @Autowired
  @Qualifier("physicalPortServiceRepoImpl")
  private PhysicalPortService physicalPortServiceRepoImpl;

  @Autowired
  @Qualifier("physicalPortServiceNbiImpl")
  private PhysicalPortService physicalPortServiceNbiImpl;

  /**
   * Finds all ports using the North Bound Interface and enhances these ports
   * with data found in our own database.
   */
  @Override
  public List<PhysicalPort> findAll() {
    List<PhysicalPort> nbiPorts = physicalPortServiceNbiImpl.findAll();
    List<PhysicalPort> repoPorts = physicalPortServiceRepoImpl.findAll();

    enrichPorts(nbiPorts, repoPorts);

    return nbiPorts;
  }

  @Override
  public Collection<PhysicalPort> findUnallocated() {
    List<PhysicalPort> allPorts = findAll();

    return Collections2.filter(allPorts, new Predicate<PhysicalPort>() {
      @Override
      public boolean apply(PhysicalPort input) {
        return input.getId() == null;
      }
    });
  }

  @Override
  public List<PhysicalPort> findEntries(final int firstResult, final int sizeNo) {
    return Collections.emptyList();
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
  private void enrichPorts(final List<PhysicalPort> portsToEnrich, final List<PhysicalPort> dataPorts) {
    // Iterate of the nbiPorts and find matching repoPort
    for (final PhysicalPort portToEnrich : portsToEnrich) {

      Collection<PhysicalPort> matchingPorts = Collections2.filter(dataPorts, new Predicate<PhysicalPort>() {
        @Override
        public boolean apply(final PhysicalPort port) {
          return StringUtils.hasText(portToEnrich.getName()) && portToEnrich.getName().equals(port.getName());
        };
      });

      if (!matchingPorts.isEmpty()) {
        if (matchingPorts.size() == 1) {
          enrichPortWithPort(portToEnrich, matchingPorts.iterator().next());
        }
        else {
          throw new IllegalStateException(format("Name is not unique. Found [%s] ports with name: %s",
              matchingPorts.size(), portToEnrich.getName()));
        }
      }
    }
  }

  /**
   * Enriches the port with additional data.
   *
   * Clones JPA attributes (id and version), so a find will return these
   * preventing a additional save instead of an update.
   *
   * @param portToEnrich
   *          The port to enrich
   * @param dataPort
   *          The data to enrich with.
   */
  private void enrichPortWithPort(final PhysicalPort portToEnrich, final PhysicalPort dataPort) {
    if (portToEnrich == null || dataPort == null) {
      return;
    }
    portToEnrich.setPhysicalResourceGroup(dataPort.getPhysicalResourceGroup());

    portToEnrich.setId(dataPort.getId());
    portToEnrich.setVersion(dataPort.getVersion());
  }

  protected void setNbiService(PhysicalPortService nbiService) {
    this.physicalPortServiceNbiImpl = nbiService;
  }

  protected void setRepoService(PhysicalPortService repoService) {
    this.physicalPortServiceRepoImpl = repoService;
  }

}
