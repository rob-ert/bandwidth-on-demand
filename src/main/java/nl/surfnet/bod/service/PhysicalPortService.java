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
import static nl.surfnet.bod.service.PhysicalPortPredicatesAndSpecifications.byPhysicalResourceGroupSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.EnniPort;
import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.NbiPort.InterfaceType;
import nl.surfnet.bod.domain.NsiVersion;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UniPort;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.nbi.PortNotAvailableException;
import nl.surfnet.bod.repo.EnniPortRepo;
import nl.surfnet.bod.nsi.NsiConstants;
import nl.surfnet.bod.repo.PhysicalPortRepo;
import nl.surfnet.bod.repo.UniPortRepo;
import nl.surfnet.bod.web.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation which combines {@link UniPort}s.
 *
 * The {@link UniPort}s found in the {@link NbiPortService} are leading and when
 * more data is available in our repository they will be enriched.
 *
 * Since {@link UniPort}s from the {@link NbiPortService} are considered read
 * only, the methods that change data are performed using the
 * {@link PhysicalPortRepo}.
 *
 */
@Service
@Transactional
public class PhysicalPortService extends AbstractFullTextSearchService<UniPort> {

  private static final String PORT_DETECTION_CRON_KEY = "physicalport.detection.job.cron";

  private final Logger logger = LoggerFactory.getLogger(PhysicalPortService.class);

  @Resource private VirtualPortService virtualPortService;
  @Resource private PhysicalPortRepo physicalPortRepo;
  @Resource private UniPortRepo uniPortRepo;
  @Resource private EnniPortRepo enniPortRepo;
  @Resource private NbiClient nbiClient;
  @Resource private LogEventService logEventService;
  @Resource private SnmpAgentService snmpAgentService;

  @PersistenceContext
  private EntityManager entityManager;

  public List<UniPort> findAllocatedUniEntries(int firstResult, int maxResults, Sort sort) {
    return uniPortRepo.findAll(new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  public List<EnniPort> findAllAllocatedEnniEntries() {
    return enniPortRepo.findAll();
  }

  public List<EnniPort> findAllocatedEnniEntries(int firstResult, int maxResults, Sort sort) {
    return enniPortRepo.findAll(new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  public Collection<NbiPort> findUnallocatedEntries(int firstResult, int sizeNo) {
    return limitPorts(findUnallocated(), firstResult, sizeNo);
  }

  public List<PhysicalPort> findUnalignedPhysicalPorts() {
    return physicalPortRepo.findAll(UNALIGNED_PORT_SPEC);
  }

  public List<PhysicalPort> findUnalignedPhysicalPorts(int firstResult, int maxResults, Sort sort) {
    return physicalPortRepo.findAll(UNALIGNED_PORT_SPEC, new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  private List<NbiPort> limitPorts(Collection<NbiPort> ports, int firstResult, int sizeNo) {
    return newArrayList(limit(skip(ports, firstResult), sizeNo));
  }

  public Collection<NbiPort> findUnallocatedUniPorts() {
    return Collections2.filter(findUnallocated(), new Predicate<NbiPort>() {
      @Override
      public boolean apply(NbiPort input) {
        return input.getInterfaceType() == InterfaceType.UNI;
      }
    });
  }

  public Collection<NbiPort> findUnallocated() {
    List<NbiPort> nbiPorts = nbiClient.findAllPorts();
    List<PhysicalPort> physicalPorts = physicalPortRepo.findAll();

    final ImmutableMap<String, PhysicalPort> repoPortMap = buildPhysicalPortIdMap(physicalPorts);

    return Collections2.filter(nbiPorts, new Predicate<NbiPort>() {
      @Override
      public boolean apply(NbiPort input) {
        return !repoPortMap.containsKey(input.getNmsPortId());
      }
    });
  }

  public long countUniPorts() {
    return uniPortRepo.count();
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

  public Optional<NbiPort> findNbiPort(String nmsPortId) {
    try {
      return Optional.of(nbiClient.findPhysicalPortByNmsPortId(nmsPortId));
    } catch (PortNotAvailableException e) {
      return Optional.absent();
    }
  }

  public PhysicalPort findByNmsPortId(String nmsPortId) {
    return physicalPortRepo.findByNbiPortNmsPortId(nmsPortId);
  }

  public EnniPort findByNsiV2StpId(String stpId) {
    String bodPortId = NsiConstants.parseLocalNsiId(stpId, NsiVersion.TWO);
    return physicalPortRepo.findEnniPortByBodPortId(bodPortId);
  }

  public PhysicalPort findByBodPortId(String bodPortId) {
    return physicalPortRepo.findByBodPortId(bodPortId);
  }

  public void delete(Long id) {
    PhysicalPort physicalPort = find(id);

    if (physicalPort instanceof UniPort) {
      Collection<VirtualPort> virtualPorts = virtualPortService.findAllForPhysicalPort((UniPort) physicalPort);
      virtualPortService.deleteVirtualPorts(virtualPorts, Security.getUserDetails());
    }

    logEventService.logDeleteEvent(Security.getUserDetails(), "Port " + getLogLabel(Security.getSelectedRole(), physicalPort), physicalPort);

    physicalPortRepo.delete(physicalPort);
  }

  public PhysicalPort find(Long id) {
    return physicalPortRepo.findOne(id);
  }

  public UniPort findUniPort(Long id) {
    return uniPortRepo.findOne(id);
  }

  public EnniPort findEnniPort(Long id) {
    return enniPortRepo.findOne(id);
  }

  public void save(PhysicalPort physicalPort) {
    physicalPortRepo.save(physicalPort);

    // Log event after creation, so the ID is set by hibernate
    logEventService.logCreateEvent(Security.getUserDetails(), physicalPort);
  }

  public UniPort update(UniPort physicalPort) {
    logEventService.logUpdateEvent(Security.getUserDetails(), "Allocated port "
        + getLogLabel(Security.getSelectedRole(), physicalPort), physicalPort);

    return physicalPortRepo.save(physicalPort);
  }

  public List<UniPort> findAllocatedEntriesForPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup, int firstResult, int maxResults, Sort sort) {
    return uniPortRepo.findAll(byPhysicalResourceGroupSpec(physicalResourceGroup), new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  public long countAllocatedForPhysicalResourceGroup(final PhysicalResourceGroup physicalResourceGroup) {
    return uniPortRepo.count(PhysicalPortPredicatesAndSpecifications.byPhysicalResourceGroupSpec(physicalResourceGroup));
  }

  public void forceCheckForPortInconsistencies() {
    detectAndPersistPortInconsistencies();
  }

  @Scheduled(cron = "${" + PORT_DETECTION_CRON_KEY + "}")
  public void detectAndPersistPortInconsistencies() {
    logger.info("Detecting port inconsistencies with the NMS, job based on configuration key: {}", PORT_DETECTION_CRON_KEY);

    List<PhysicalPort> bodPorts = physicalPortRepo.findAll();
    List<NbiPort> nbiPorts = nbiClient.findAllPorts();

    PortAlignmentChecker checker = new PortAlignmentChecker();
    checker.updateAlignment(bodPorts, nbiPorts);

    logger.info("Found {} ports realigned in the NMS", checker.getRealignedPorts().size());
    logger.info("Found {} ports unaligned in the NMS", checker.getUnalignedPorts().size());
    logger.info("Found {} ports changed alignment in the NMS", checker.getAlignmentChangedPorts().size());

    for (PhysicalPort port : checker.getUnalignedPorts()) {
      snmpAgentService.sendMissingPortEvent(port.getId().toString());
    }

    physicalPortRepo.save(checker.getRealignedPorts());
    physicalPortRepo.save(checker.getUnalignedPorts());
    physicalPortRepo.save(checker.getAlignmentChangedPorts());

    logEventService.logUpdateEvent(Security.getUserDetails(), "Reappeared ports in NMS", checker.getRealignedPorts());
    logEventService.logUpdateEvent(Security.getUserDetails(), "Alignment changed ports in NMS", checker.getAlignmentChangedPorts());
    logEventService.logUpdateEvent(Security.getUserDetails(), "Disappeared ports in NMS", checker.getUnalignedPorts());
  }

  static <T extends PhysicalPort> ImmutableMap<String, T> buildPhysicalPortIdMap(List<T> ports) {
    Map<String, T> physicalPorts = Maps.newHashMap();
    for (T port : ports) {
      physicalPorts.put(port.getNmsPortId(), port);
    }
    return ImmutableMap.copyOf(physicalPorts);
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
   * @return Label to log for the given {@link UniPort}
   */
  private String getLogLabel(BodRole bodRole, PhysicalPort physicalPort) {
    if (bodRole.isManagerRole() && physicalPort instanceof UniPort && ((UniPort) physicalPort).hasManagerLabel()) {
      return ((UniPort) physicalPort).getManagerLabel();
    }

    return physicalPort.getNocLabel();
  }

  public List<Long> findIdsByRoleAndPhysicalResourceGroup(BodRole bodRole, Optional<PhysicalResourceGroup> physicalResourceGroup, Optional<Sort> sort) {
    if (bodRole.isManagerRole() && physicalResourceGroup.isPresent()) {
      return uniPortRepo.findIdsWithWhereClause(PhysicalPortPredicatesAndSpecifications.byPhysicalResourceGroupSpec(physicalResourceGroup.get()), sort);
    } else if (bodRole.isNocRole()) {
      if (physicalResourceGroup.isPresent()) {
        return uniPortRepo.findIdsWithWhereClause(PhysicalPortPredicatesAndSpecifications.byPhysicalResourceGroupSpec(physicalResourceGroup.get()), sort);
      } else {
        return uniPortRepo.findIds(sort);
      }
    }

    return new ArrayList<>();
  }

  public List<Long> findIds(Optional<Sort> sort) {
    return findIdsByRoleAndPhysicalResourceGroup(BodRole.createNocEngineer(), Optional.<PhysicalResourceGroup> absent(), sort);
  }
}
