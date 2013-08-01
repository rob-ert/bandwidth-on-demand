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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.util.TestHelper;
import nl.surfnet.bod.util.TestHelper.PropertiesEnvironment;
import nl.surfnet.bod.web.view.ReservationReportView;

import org.joda.time.YearMonth;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class VersReportingServiceTestIntegration {

  @InjectMocks
  private VersReportingService subject;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;

  @Mock
  private ReportingService reportingServiceMock;

  @Before
  public void setUp() throws Exception {
    PropertiesEnvironment testProperties = TestHelper.testProperties();
    subject.setVersUserName(testProperties.getProperty("vers.user"));
    subject.setVersUserPassword(testProperties.getDecryptedProperty("vers.password"));
    subject.setServiceURL(testProperties.getProperty("vers.url"));
  }

  @Test
  @Ignore("Only for testing uploading a real report, don't do it every run")
  public void insertReporting() throws Exception {
    YearMonth period = new YearMonth(2006, 3);
    Institute institute = new InstituteFactory().setShortName("RUG").setName("Rijks Universiteit Groningen").create();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setInstitute(institute).create();
    ReservationReportView adminReport = new ReservationReportView(period.toInterval().getStart(), period.toInterval().getEnd());
    adminReport.setAmountRunningReservationsFailed(1);
    adminReport.setAmountRequestsCreatedSucceeded(5);
    adminReport.setAmountRunningReservationsStillRunning(4);

    ReservationReportView nocReport = new ReservationReportView(period.toInterval().getStart(), period.toInterval().getEnd());
    nocReport.setAmountRunningReservationsFailed(2);
    nocReport.setAmountRequestsCreatedSucceeded(10);
    nocReport.setAmountRunningReservationsStillRunning(8);

    when(physicalResourceGroupServiceMock.findAllWithPorts()).thenReturn(Lists.newArrayList(group));
    when(reportingServiceMock.determineReportForAdmin(eq(period.toInterval()), any(BodRole.class))).thenReturn(adminReport);
    when(reportingServiceMock.determineReportForNoc(period.toInterval())).thenReturn(nocReport);

    subject.sendReports(period);
  }

}