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
import static nl.surfnet.bod.service.PhysicalPortPredicatesAndSpecifications.byPhysicalResourceGroupSpec;
import static nl.surfnet.bod.service.PhysicalPortPredicatesAndSpecifications.UNALIGNED_PORT_SPEC;
import static nl.surfnet.bod.service.PhysicalPortPredicatesAndSpecifications.UNALLOCATED_PORTS_PRED;

import java.util.*;
import java.util.Map.Entry;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.mtosi.MtosiLiveClient;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.repo.PhysicalPortRepo;
import nl.surfnet.bod.util.Functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.google.common.collect.Sets.SetView;

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

  private static final String PORT_DETECTION_CRON_KEY = "physicalport.detection.job.cron";

  @Autowired
  private PhysicalPortRepo physicalPortRepo;

  @Autowired
  private NbiClient nbiClient;

  @Autowired
  private MtosiLiveClient mtosiClient;

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

  @Override
  public Collection<PhysicalPort> findUnallocatedMTOSIEntries(int firstResult, int sizeNo) {
    HashMap<String, String> unallocatedPorts = mtosiClient.getUnallocatedPorts();

    List<PhysicalPort> physicalPorts = Lists.newArrayList();
    for (Entry<String, String> entry : unallocatedPorts.entrySet()) {
      PhysicalPort physicalPort = transformMTOSIEToPhysicalPort(entry);
      physicalPorts.add(physicalPort);
    }

    return limitPorts(physicalPorts, firstResult, sizeNo);
  }

  @Override
  public List<PhysicalPort> findUnalignedPhysicalPorts() {
    return physicalPortRepo.findAll(UNALIGNED_PORT_SPEC);
  }

  @VisibleForTesting
  PhysicalPort transformMTOSIEToPhysicalPort(Entry<String, String> entry) {
    PhysicalPort physicalPort = new PhysicalPort();
    physicalPort.setBodPortId(entry.getKey());
    physicalPort.setNmsPortId(entry.getValue());
    return physicalPort;
  }

  private List<PhysicalPort> limitPorts(Collection<PhysicalPort> ports, int firstResult, int sizeNo) {
    return newArrayList(limit(skip(ports, firstResult), sizeNo));
  }

  @Override
  public Collection<PhysicalPort> findUnallocated() {
    List<PhysicalPort> allPorts = findAll();

    return Collections2.filter(allPorts, UNALLOCATED_PORTS_PRED);
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
  public long countUnallocatedMTOSI() {
    return mtosiClient.getUnallocatedMtosiPortCount();
  }

  @Override
  public long countUnalignedPhysicalPorts() {
    return physicalPortRepo.count(UNALIGNED_PORT_SPEC);
  }

  @Override
  public PhysicalPort findByNmsPortId(final String nmsPortId) {
    PhysicalPort nbiPort = nbiClient.findPhysicalPortByNmsPortId(nmsPortId);
    PhysicalPort repoPort = physicalPortRepo.findByNmsPortId(nmsPortId);

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
          return nbiPort.getNmsPortId().equals(port.getNmsPortId());
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

    return physicalPortRepo.findAll(byPhysicalResourceGroupSpec(physicalResourceGroup),
        new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  @Override
  public long countAllocatedForPhysicalResourceGroup(final PhysicalResourceGroup physicalResourceGroup) {

    return physicalPortRepo.count(PhysicalPortPredicatesAndSpecifications
        .byPhysicalResourceGroupSpec(physicalResourceGroup));
  }

  @Override
  public void forceCheckForPortInconsitencies() {
    detectAndPersistPortInconsistencies();
  }

  @Scheduled(cron = "${" + PORT_DETECTION_CRON_KEY + "}")
  public void detectAndPersistPortInconsistencies() {
    logger
        .info("Detecting port inconssistencies with the NMS, job based on configuration key: {}", PORT_DETECTION_CRON_KEY);
    final ImmutableSet<String> nbiPortIds = ImmutableSet.copyOf(Lists.transform(nbiClient.findAllPhysicalPorts(),
        Functions.TO_NMS_PORT_ID_FUNC));

    // Build map for easy lookup
    Map<String, PhysicalPort> physicalPorts = Maps.newHashMap();
    for (PhysicalPort port : physicalPortRepo.findAll()) {
      physicalPorts.put(port.getNmsPortId(), port);
    }
    final ImmutableMap<String, PhysicalPort> immutablePorts = ImmutableMap.copyOf(physicalPorts);

    List<PhysicalPort> reappearedPortsInNMS = markRealignedPortsInNMS(immutablePorts, nbiPortIds);
    physicalPortRepo.save(reappearedPortsInNMS);

    List<PhysicalPort> dissapearedPortsFromNMS = markUnalignedWithNMS(immutablePorts, nbiPortIds);
    physicalPortRepo.save(dissapearedPortsFromNMS);
  }

  @VisibleForTesting
  List<PhysicalPort> markRealignedPortsInNMS(Map<String, PhysicalPort> bodPorts, Set<String> nbiPortIds) {
    List<PhysicalPort> reappearedPorts = Lists.newArrayList();

    ImmutableSet<String> unalignedPortIds = FluentIterable.from(bodPorts.values()).filter(Functions.MISSING_PORTS_PRED)
        .transform(Functions.TO_NMS_PORT_ID_FUNC).toImmutableSet();

    SetView<String> reAlignedPortIds = Sets.intersection(unalignedPortIds, nbiPortIds);
    logger.info("Found {} ports realigned in the NMS", reAlignedPortIds.size());

    PhysicalPort reappearedPort = null;
    for (String portId : reAlignedPortIds) {
      reappearedPort = bodPorts.get(portId);
      reappearedPort.setAlignedWithNMS(true);
      reappearedPorts.add(reappearedPort);
      logger.debug("Port realigned in the NMS: {}", reappearedPort);
    }

    return reappearedPorts;
  }

  /**
   * Checks the {@link PhysicalPort}s in the given Map which are
   * <strong>not</strong> indicated as missing have disappeared from the NMS by
   * finding the differences between the ports in the given list and the ports
   * returned by the NMS based on the {@link PhysicalPort#getNmsPortId()} .
   * 
   * @param bodPorts
   *          List with ports from BoD
   * @param nbiPortIds
   *          List with portIds from the NMS
   * @return {@link List<PhysicalPort>} which were not missing but are now.
   */
  @VisibleForTesting
  List<PhysicalPort> markUnalignedWithNMS(final Map<String, PhysicalPort> bodPorts, final Set<String> nbiPortIds) {
    List<PhysicalPort> disappearedPorts = Lists.newArrayList();

    ImmutableSet<String> physicalPortIds = FluentIterable.from(bodPorts.values())
        .filter(Functions.NON_MISSING_PORTS_PRED).transform(Functions.TO_NMS_PORT_ID_FUNC).toImmutableSet();

    SetView<String> unalignedPortIds = Sets.difference(physicalPortIds, nbiPortIds);
    logger.info("Found {} ports disappeared in the NMS", unalignedPortIds.size());
    PhysicalPort disappearedPort = null;
    for (String portId : unalignedPortIds) {
      disappearedPort = bodPorts.get(portId);
      disappearedPort.setAlignedWithNMS(false);
      logger.debug("Port unaligned in the NMS: {}", disappearedPort);
      disappearedPorts.add(disappearedPort);
    }

    return disappearedPorts;
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
    portToEnrich.setBodPortId(dataPort.getBodPortId());
  }
}
