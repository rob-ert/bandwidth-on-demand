/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.service;

import static com.google.common.collect.Iterables.limit;
import static com.google.common.collect.Iterables.skip;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalPort_;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.repo.PhysicalPortRepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

/**
 * Service implementation which combines {@link PhysicalPort}s.
 * 
 * The {@link PhysicalPort}s found in the {@link NbiPortService} are leading and
 * when more data is available in our repository they will be enriched.
 * 
 * Since {@link PhysicalPort}s from the {@link NbiPortService} are considered
 * read only, the methods that change data are performed using the
 * {@link PhysicalPortRepo}.
 * 
 * 
 * @author Frank Mölder
 */
@Service
public class PhysicalPortServiceImpl implements PhysicalPortService {

  private Logger logger = LoggerFactory.getLogger(PhysicalPortServiceImpl.class);

  @Autowired
  private PhysicalPortRepo physicalPortRepo;

  @Autowired
  private NbiClient nbiClient;

  /**
   * Finds all ports using the North Bound Interface and enhances these ports
   * with data found in our own database.
   */
  protected List<PhysicalPort> findAll() {
    List<PhysicalPort> nbiPorts = nbiClient.findAllPhysicalPorts();
    List<PhysicalPort> repoPorts = physicalPortRepo.findAll();

    logger.debug("Got '{}' ports from nbi and '{}' ports from the repo", nbiPorts.size(), repoPorts.size());

    enrichPorts(nbiPorts, repoPorts);

    return nbiPorts;
  }

  @Override
  public List<PhysicalPort> findAllocatedEntries(int firstResult, int maxResults, Sort sort) {
    return physicalPortRepo.findAll(new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  @Override
  public Collection<PhysicalPort> findUnallocatedEntries(int firstResult, int sizeNo) {
    return limitPorts(findUnallocated(), firstResult, sizeNo);
  }

  private List<PhysicalPort> limitPorts(Collection<PhysicalPort> ports, int firstResult, int sizeNo) {
    return newArrayList(limit(skip(ports, firstResult), sizeNo));
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
  public long countAllocated() {
    return physicalPortRepo.count();
  }

  @Override
  public long countUnallocated() {
    return nbiClient.getPhysicalPortsCount() - physicalPortRepo.count();
  }

  @Override
  public PhysicalPort findByNetworkElementPk(final String networkElementPk) {
    PhysicalPort nbiPort = nbiClient.findPhysicalPortByNetworkElementId(networkElementPk);
    PhysicalPort repoPort = physicalPortRepo.findByNetworkElementPk(networkElementPk);

    if (repoPort != null) {
      enrichPortWithPort(nbiPort, repoPort);
    }

    return nbiPort;
  }

  @Override
  public void delete(final PhysicalPort physicalPort) {
    physicalPortRepo.delete(physicalPort);
  }

  @Override
  public PhysicalPort find(final Long id) {
    return physicalPortRepo.findOne(id);
  }

  @Override
  public void save(final PhysicalPort physicalPort) {
    physicalPortRepo.save(physicalPort);
  }

  @Override
  public PhysicalPort update(final PhysicalPort physicalPort) {
    return physicalPortRepo.save(physicalPort);
  }

  /**
   * Adds data found in given ports to the specified ports, enriches them.
   * 
   * @param nbiPorts
   *          {@link PhysicalPort}s to add the data to
   * @param repoPorts
   *          {@link PhysicalPort}s containing additional data
   */
  private void enrichPorts(final List<PhysicalPort> nbiPorts, final List<PhysicalPort> repoPorts) {
    for (final PhysicalPort nbiPort : nbiPorts) {
      Collection<PhysicalPort> matchingPorts = Collections2.filter(repoPorts, new Predicate<PhysicalPort>() {
        @Override
        public boolean apply(final PhysicalPort port) {
          return nbiPort.getNetworkElementPk().equals(port.getNetworkElementPk());
        };
      });

      if (matchingPorts.isEmpty()) {
        continue;
      }

      PhysicalPort matchingPort = Iterables.getOnlyElement(matchingPorts);
      enrichPortWithPort(nbiPort, matchingPort);
    }
  }

  @Override
  public List<PhysicalPort> findAllocatedEntriesForPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup,
      int firstResult, int maxResults, Sort sort) {

    return physicalPortRepo.findAll(specificationForPhysicalResourceGroup(physicalResourceGroup),
        new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  @Override
  public long countAllocatedForPhysicalResourceGroup(final PhysicalResourceGroup physicalResourceGroup) {

    return physicalPortRepo.count(specificationForPhysicalResourceGroup(physicalResourceGroup));
  }

  private Specification<PhysicalPort> specificationForPhysicalResourceGroup(
      final PhysicalResourceGroup physicalResourceGroup) {
    return new Specification<PhysicalPort>() {

      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<PhysicalPort> root, CriteriaQuery<?> query,
          CriteriaBuilder cb) {

        return cb.equal(root.get(PhysicalPort_.physicalResourceGroup), physicalResourceGroup);
      }
    };
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
    Preconditions.checkNotNull(portToEnrich);
    Preconditions.checkNotNull(dataPort);

    portToEnrich.setPhysicalResourceGroup(dataPort.getPhysicalResourceGroup());
    portToEnrich.setId(dataPort.getId());
    portToEnrich.setVersion(dataPort.getVersion());
    portToEnrich.setNocLabel(dataPort.getNocLabel());
    portToEnrich.setManagerLabel(dataPort.getManagerLabel());
    portToEnrich.setPortId(dataPort.getPortId());
  }

}
