/**
 * Copyright (c) 2012, SURFnet BV
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

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.idd.IddClient;
import nl.surfnet.bod.repo.InstituteRepo;
import nl.surfnet.bod.web.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

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
  private SnmpAgentService snmpAgentService;

  @Override
  public Institute find(Long id) {
    return instituteRepo.findOne(id);
  }

  @Override
  public Institute findByShortName(String shortName) {
    return instituteRepo.findByShortName(shortName);
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
    Collection<Institute> iddInstitutes = iddClient.getInstitutes();

    detectRemovedInstitutes(currentAlignedInstitutes, iddInstitutes);

    detectAddedInstitutes(currentAlignedInstitutes, iddInstitutes);
  }

  private void detectAddedInstitutes(List<Institute> currentAlignedInstitutes, Collection<Institute> iddInstitutes) {
    iddInstitutes.removeAll(currentAlignedInstitutes);

    logger.info(String.format("Found %d new or updated institutes from IDD", iddInstitutes.size()));

    instituteRepo.save(iddInstitutes);

    logEventService.logUpdateEvent(Security.getUserDetails(), "Marked new or realigned with IDD", iddInstitutes);
  }

  private void detectRemovedInstitutes(List<Institute> currentAlignedInstitutes, Collection<Institute> iddInstitutes) {
    List<Institute> unalignedInstitutes = Lists.newArrayList(currentAlignedInstitutes);
    unalignedInstitutes.removeAll(iddInstitutes);

    logger.info(String.format(
        "Found %d institutes that are not in IDD anymore, marking them not aligned",
        unalignedInstitutes.size()));

    markNotAligned(unalignedInstitutes);
    instituteRepo.save(unalignedInstitutes);

    logEventService.logUpdateEvent(Security.getUserDetails(), "Marked unaligned with IDD", unalignedInstitutes);
  }

  private void markNotAligned(List<Institute> allInstitutes) {
    for (Institute institute : allInstitutes) {
      institute.setAlignedWithIDD(false);
      snmpAgentService.sendMissingInstituteEvent(institute.getId().toString());
    }
  }

}
