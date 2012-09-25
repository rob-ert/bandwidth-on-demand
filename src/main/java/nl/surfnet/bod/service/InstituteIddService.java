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

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.idd.IddClient;
import nl.surfnet.bod.repo.InstituteRepo;
import nl.surfnet.bod.snmp.SnmpAgent;
import nl.surfnet.bod.util.Functions;
import nl.surfnet.bod.web.security.Security;

@Service
@Transactional
public class InstituteIddService implements InstituteService {

  private static final String INSTITUTE_REFRESH_CRON_KEY = "institute.refresh.job.cron";

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource
  private IddClient iddClient;

  @Resource
  private InstituteRepo instituteRepo;

  @Resource
  private LogEventService logEventService;
  
  @Resource
  private SnmpAgent snmpAgent;

  @Override
  public Institute find(Long id) {
    return instituteRepo.findOne(id);
  }

  @Override
  public Collection<Institute> findAlignedWithIDD() {
    return instituteRepo.findByAlignedWithIDD(true);
  }

  @Override
  @Scheduled(cron = "${" + INSTITUTE_REFRESH_CRON_KEY + "}")
  public void refreshInstitutes() {
    logger.info("Refreshing institutes from IDD, job based on configuration key: {}", INSTITUTE_REFRESH_CRON_KEY);

    List<Institute> currentAlignedInstitutes = instituteRepo.findByAlignedWithIDD(true);
    Collection<Institute> iddInstitutes = Functions.transformKlanten(iddClient.getKlanten(), true);

    List<Institute> unalignedInstitutes = Lists.newArrayList(currentAlignedInstitutes);
    unalignedInstitutes.removeAll(iddInstitutes);

    logger.info(String.format("Found %d institutes that are not in IDD anymore, marking them not aligned",
        unalignedInstitutes.size()));
    markNotAligned(unalignedInstitutes);
    instituteRepo.save(unalignedInstitutes);

    logEventService.logUpdateEvent(Security.getUserDetails(), unalignedInstitutes, "Marked unaligned with IDD");

    iddInstitutes.removeAll(currentAlignedInstitutes);

    logger.info(String.format("Found %d new or updated institutes from IDD", iddInstitutes.size()));
    instituteRepo.save(iddInstitutes);

    logEventService.logUpdateEvent(Security.getUserDetails(), iddInstitutes, "Marked new or realigned with IDD");
  }

  private void markNotAligned(List<Institute> allInstitutes) {
    // Mark all not aligned
    for (Institute institute : allInstitutes) {
      institute.setAlignedWithIDD(false);
      snmpAgent.sendPdu(snmpAgent.getPdu(snmpAgent.getOidIddInstituteDisappeared(institute.getId().toString()), SnmpAgent.SEVERITY_MAJOR, PDU.TRAP));
    }
  }

}
