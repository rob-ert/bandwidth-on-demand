/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.service;

import static com.google.common.collect.Iterables.limit;
import static com.google.common.collect.Iterables.skip;
import static com.google.common.collect.Lists.newArrayList;
import static nl.surfnet.bod.service.PhysicalPortPredicatesAndSpecifications.UNALIGNED_PORT_SPEC;
import static nl.surfnet.bod.service.PhysicalPortPredicatesAndSpecifications.UNALLOCATED_PORTS_PRED;
import static nl.surfnet.bod.service.PhysicalPortPredicatesAndSpecifications.byPhysicalResourceGroupSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.NmsAlignmentStatus;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.nbi.PortNotAvailableException;
import nl.surfnet.bod.repo.PhysicalPortRepo;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 */
@Service
@Transactional
public class PhysicalPortService extends AbstractFullTextSearchService<PhysicalPort> {

  private static final String PORT_DETECTION_CRON_KEY = "physicalport.detection.job.cron";

  private final Logger logger = LoggerFactory.getLogger(PhysicalPortService.class);

  @Resource
  private VirtualPortService virtualPortService;

  @Resource
  private PhysicalPortRepo physicalPortRepo;

  @Resource
  private NbiClient nbiClient;

  @Resource
  private LogEventService logEventService;

  @Resource
  private SnmpAgentService snmpAgentService;

  @PersistenceContext
  private EntityManager entityManager;

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

  public List<PhysicalPort> findAllocatedEntries(int firstResult, int maxResults, Sort sort) {
    return physicalPortRepo.findAll(new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  public Collection<PhysicalPort> findUnallocatedEntries(int firstResult, int sizeNo) {
    return limitPorts(findUnallocated(), firstResult, sizeNo);
  }

  public List<PhysicalPort> findUnalignedPhysicalPorts() {
    return physicalPortRepo.findAll(UNALIGNED_PORT_SPEC);
  }

  public List<PhysicalPort> findUnalignedPhysicalPorts(int firstResult, int maxResults, Sort sort) {
    return physicalPortRepo.findAll(UNALIGNED_PORT_SPEC, new PageRequest(firstResult / maxResults, maxResults, sort))
        .getContent();
  }

  private List<PhysicalPort> limitPorts(Collection<PhysicalPort> ports, int firstResult, int sizeNo) {
    return newArrayList(limit(skip(ports, firstResult), sizeNo));
  }

  public Collection<PhysicalPort> findUnallocated() {
    List<PhysicalPort> allPorts = findAll();

    return Collections2.filter(allPorts, UNALLOCATED_PORTS_PRED);
  }

  public long countAllocated() {
    return physicalPortRepo.count();
  }

  public long countUnallocated() {
    return nbiClient.getPhysicalPortsCount() - physicalPortRepo.count();
  }

  public long countUnalignedPhysicalPorts() {
    return physicalPortRepo.count(UNALIGNED_PORT_SPEC);
  }

  /**
   * @return could return null if port does not exist in db nor in NBI
   */
  public PhysicalPort findByNmsPortId(final String nmsPortId) {
    PhysicalPort repoPort = physicalPortRepo.findByNmsPortId(nmsPortId);

    try {
      PhysicalPort nbiPort = nbiClient.findPhysicalPortByNmsPortId(nmsPortId);
      return repoPort == null ? nbiPort : enrichPortWithPort(nbiPort, repoPort);
    }
    catch (PortNotAvailableException e) {
      logger.warn(e.getMessage());
      return repoPort;
    }
  }

  /**
   * Deletes the specified port and all its related objects like the mapped
   * {@link VirtualPort}s, and {@link Reservation}s
   *
   * @param nmsPortId
   *          NmsPort of the port
   */
  public void deleteByNmsPortId(final String nmsPortId) {
    PhysicalPort physicalPort = findByNmsPortId(nmsPortId);

    final RichUserDetails userDetails = Security.getUserDetails();
    final Collection<VirtualPort> virtualPorts = virtualPortService.findAllForPhysicalPort(physicalPort);
    virtualPortService.deleteVirtualPorts(virtualPorts, userDetails);

    delete(physicalPortRepo.findByNmsPortId(nmsPortId));
  }

  public PhysicalPort find(final Long id) {
    return physicalPortRepo.findOne(id);
  }

  public void save(final PhysicalPort physicalPort) {
    physicalPortRepo.save(physicalPort);

    // Log event after creation, so the ID is set by hibernate
    logEventService.logCreateEvent(Security.getUserDetails(), physicalPort);
  }

  public PhysicalPort update(final PhysicalPort physicalPort) {
    logEventService.logUpdateEvent(Security.getUserDetails(), "Allocated port "
        + getLogLabel(Security.getSelectedRole(), physicalPort), physicalPort);

    return physicalPortRepo.save(physicalPort);
  }

  @VisibleForTesting
  void delete(final PhysicalPort physicalPort) {
    logEventService.logDeleteEvent(Security.getUserDetails(), "Port "
        + getLogLabel(Security.getSelectedRole(), physicalPort), physicalPort);
    physicalPortRepo.delete(physicalPort);
  }

  /**
   * Adds data found in given ports to the specified ports, enriches them.
   *
   * @param nbiPorts
   *          {@link PhysicalPort}s to add the data to
   * @param repoPorts
   *          {@link PhysicalPort}s containing additional data
   */
  private void enrichPorts(List<PhysicalPort> nbiPorts, List<PhysicalPort> repoPorts) {
    ImmutableMap<String, PhysicalPort> repoPortMap = buildPortIdMap(repoPorts);
    for (PhysicalPort nbiPort : nbiPorts) {
      PhysicalPort matchingPort = repoPortMap.get(nbiPort.getNmsPortId());
      if (matchingPort != null) {
        enrichPortWithPort(nbiPort, matchingPort);
      }
    }
  }

  public List<PhysicalPort> findAllocatedEntriesForPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup,
      int firstResult, int maxResults, Sort sort) {

    return physicalPortRepo.findAll(byPhysicalResourceGroupSpec(physicalResourceGroup),
        new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  public long countAllocatedForPhysicalResourceGroup(final PhysicalResourceGroup physicalResourceGroup) {

    return physicalPortRepo.count(PhysicalPortPredicatesAndSpecifications
        .byPhysicalResourceGroupSpec(physicalResourceGroup));
  }

  public void forceCheckForPortInconsistencies() {
    detectAndPersistPortInconsistencies();
  }

  @Scheduled(cron = "${" + PORT_DETECTION_CRON_KEY + "}")
  public void detectAndPersistPortInconsistencies() {
    logger.info("Detecting port inconsistencies with the NMS, job based on configuration key: {}",
        PORT_DETECTION_CRON_KEY);

    List<PhysicalPort> bodPorts = physicalPortRepo.findAll();
    List<PhysicalPort> nbiPorts = nbiClient.findAllPhysicalPorts();

    List<PhysicalPort> realignedPorts = markRealignedPortsInNMS(bodPorts, nbiPorts);
    physicalPortRepo.save(realignedPorts);
    logEventService.logUpdateEvent(Security.getUserDetails(), "Reappeared ports in NMS", realignedPorts);

    List<PhysicalPort> unalignedPorts = markUnalignedWithNMS(bodPorts, nbiPorts);
    physicalPortRepo.save(unalignedPorts);
    logEventService.logUpdateEvent(Security.getUserDetails(), "Disappeared ports in NMS", unalignedPorts);
  }

  static ImmutableMap<String, PhysicalPort> buildPortIdMap(List<PhysicalPort> ports) {
    Map<String, PhysicalPort> physicalPorts = Maps.newHashMap();
    for (PhysicalPort port : ports) {
      physicalPorts.put(port.getNmsPortId(), port);
    }
    return ImmutableMap.copyOf(physicalPorts);
  }

  @VisibleForTesting
  List<PhysicalPort> markRealignedPortsInNMS(List<PhysicalPort> bodPorts, List<PhysicalPort> nbiPorts) {
    ImmutableMap<String, PhysicalPort> nbiPortsMap = buildPortIdMap(nbiPorts);

    List<PhysicalPort> realigned = Lists.newArrayList();
    for (PhysicalPort bodPort: bodPorts) {
      if (!bodPort.isAlignedWithNMS()) {
        PhysicalPort nbiPort = nbiPortsMap.get(bodPort.getNmsPortId());
        if (nbiPort != null) {
          if (bodPort.isVlanRequired() == nbiPort.isVlanRequired()) {
            bodPort.setNmsAlignmentStatus(NmsAlignmentStatus.ALIGNED);
            realigned.add(bodPort);
          } else {
            bodPort.setNmsAlignmentStatus(nbiPort.isVlanRequired()
                ? NmsAlignmentStatus.TYPE_CHANGED_TO_VLAN
                : NmsAlignmentStatus.TYPE_CHANGED_TO_LAN);
          }
        }
      }
    }

    logger.info("Found {} ports realigned in the NMS", realigned.size());
    return realigned;
  }

  /**
   * Checks the {@link PhysicalPort}s in the given Map which are aligned are now
   * unaligned by finding the differences between the ports in the given list
   * and the ports returned by the NMS based on the
   * {@link PhysicalPort#getNmsPortId()} and
   * {@link PhysicalPort#isVlanRequired()}.
   *
   * @param bodPorts
   *          List with ports from BoD
   * @param nbiPorts
   *          List with ports from the NMS
   * @return {@link List<PhysicalPort>} were aligned but are now unaligned.
   */
  @VisibleForTesting
  List<PhysicalPort> markUnalignedWithNMS(final List<PhysicalPort> bodPorts, final List<PhysicalPort> nbiPorts) {
    ImmutableMap<String, PhysicalPort> nbiPortsMap = buildPortIdMap(nbiPorts);

    List<PhysicalPort> unaligned = Lists.newArrayList();
    for (PhysicalPort bodPort : bodPorts) {
      if (bodPort.isAlignedWithNMS()) {
        PhysicalPort nbiPort = nbiPortsMap.get(bodPort.getNmsPortId());
        if (nbiPort == null) {
          bodPort.setNmsAlignmentStatus(NmsAlignmentStatus.DISAPPEARED);
          unaligned.add(bodPort);
        } else if (bodPort.isVlanRequired() != nbiPort.isVlanRequired()) {
          bodPort.setNmsAlignmentStatus(nbiPort.isVlanRequired()
              ? NmsAlignmentStatus.TYPE_CHANGED_TO_VLAN
              : NmsAlignmentStatus.TYPE_CHANGED_TO_LAN);
          unaligned.add(bodPort);
        }
      }
    }

    for (PhysicalPort port: unaligned) {
      snmpAgentService.sendMissingPortEvent(port.getId().toString());
    }

    logger.info("Found {} ports unaligned in the NMS", unaligned.size());
    return unaligned;
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
  private PhysicalPort enrichPortWithPort(final PhysicalPort portToEnrich, final PhysicalPort dataPort) {
    portToEnrich.setPhysicalResourceGroup(dataPort.getPhysicalResourceGroup());
    portToEnrich.setId(dataPort.getId());
    portToEnrich.setVersion(dataPort.getVersion());
    portToEnrich.setNocLabel(dataPort.getNocLabel());
    portToEnrich.setManagerLabel(dataPort.getManagerLabel());
    portToEnrich.setBodPortId(dataPort.getBodPortId());

    return portToEnrich;
  }

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
  }

  /**
   * Determines the label to log for the given Role
   *
   * @param bodRole
   *          Role
   * @param physicalPort
   *          Port
   * @return Label to log for the given {@link PhysicalPort}
   */
  private String getLogLabel(BodRole bodRole, PhysicalPort physicalPort) {
    if (bodRole.isManagerRole() && (physicalPort.hasManagerLabel())) {
      return physicalPort.getManagerLabel();
    }
    return physicalPort.getNocLabel();
  }

  public List<Long> findIdsByRoleAndPhysicalResourceGroup(BodRole bodRole,
      Optional<PhysicalResourceGroup> physicalResourceGroup, Optional<Sort> sort) {

    if (bodRole.isManagerRole() && physicalResourceGroup.isPresent()) {

      return physicalPortRepo.findIdsWithWhereClause(Optional.of(PhysicalPortPredicatesAndSpecifications
          .byPhysicalResourceGroupSpec(physicalResourceGroup.get())), sort);
    }
    else if (bodRole.isNocRole()) {
      if (physicalResourceGroup.isPresent()) {
        return physicalPortRepo.findIdsWithWhereClause(Optional.of(PhysicalPortPredicatesAndSpecifications
            .byPhysicalResourceGroupSpec(physicalResourceGroup.get())), sort);
      }
      else {
        return physicalPortRepo.findIdsWithWhereClause(Optional.<Specification<PhysicalPort>> absent(), sort);
      }
    }

    return new ArrayList<>();
  }

  public List<Long> findIds(Optional<Sort> sort) {
    return findIdsByRoleAndPhysicalResourceGroup(BodRole.createNocEngineer(), Optional.<PhysicalResourceGroup> absent(), sort);
  }
}