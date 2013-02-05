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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.web.view.ReservationReportView;

import org.joda.time.YearMonth;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import surfnet_er.ErInsertReportDocument;
import surfnet_er.ErInsertReportDocument.ErInsertReport;
import surfnet_er.InsertReportInput;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class VersReportingServiceTest {

  private static final String VERS_USERNAME = "VersUsername";
  private static final String VERS_USER_PASSWORD = "VersUserPassword";

  @InjectMocks
  private VersReportingService subject;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;

  @Mock
  private ReportingService reportingServiceMock;

  @Before
  public void setup() {
    subject.setVersUserName(VERS_USERNAME);
    subject.setVersUserPassword(VERS_USER_PASSWORD);
  }

  @Test
  public void createVersReportForInstitution() {
    YearMonth period = new YearMonth(2009, 2);
    long reportValue = 10L;
    String reportType = "Reservation modified";
    String reportInstance = "Failed";

    ErInsertReportDocument versRequest = subject.getVersRequest(reportType, reportValue, period, Optional.of("RUG"), reportInstance);

    ErInsertReport insertReport = versRequest.getErInsertReport();

    assertThat(insertReport.getUsername(), is(VERS_USERNAME));
    assertThat(insertReport.getPassword(), is(VERS_USER_PASSWORD));

    InsertReportInput reportInput = insertReport.getParameters();

    assertThat(reportInput.getIsHidden(), is(false));
    assertThat(reportInput.getOrganisation(), is("RUG"));
    assertThat(reportInput.getPeriod(), is("2009-02"));
    assertThat(reportInput.getNormComp(), is("="));
    assertThat(reportInput.getNormValue(), is("" + reportValue));
    assertThat(reportInput.getDepartmentList(), is("NWD"));
    assertThat(reportInput.getValue(), is("" + reportValue));
    assertThat(reportInput.getInstance(), is(reportInstance));
    assertThat(reportInput.getType(), is(reportType));
  }

  @Test
  public void createVersReportForMissingInstitution() {
    ErInsertReportDocument versRequest = subject.getVersRequest("Reservation modified", 10L, YearMonth.now(), Optional.<String>absent(), "instance");

    InsertReportInput reportInput = versRequest.getErInsertReport().getParameters();

    assertThat(reportInput.getIsHidden(), is(true));
    assertThat(reportInput.getOrganisation(), nullValue());
  }

  @Test
  public void retrieveAllReservationReportViews() {
    YearMonth period = new YearMonth(2011, 11);
    PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().create();
    ReservationReportView reportView = new ReservationReportView(period.toInterval().getStart(), period.toInterval().getEnd());

    when(physicalResourceGroupServiceMock.findAllWithPorts()).thenReturn(ImmutableList.of(prg));
    when(reportingServiceMock.determineReportForAdmin(eq(period.toInterval()), any(BodRole.class))).thenReturn(reportView);
    when(reportingServiceMock.determineReportForNoc(period.toInterval())).thenReturn(reportView);

    Map<Optional<String>, ReservationReportView> reportViews = subject.getAllReservationReportViews(period);

    assertThat(reportViews.values(), contains(reportView, reportView));
  }

  @Test
  public void servicesPkiShouldHaveFiveReports() {
    YearMonth period = YearMonth.now();
    ReservationReportView reportView = new ReservationReportView(period.toInterval().getStart(), period.toInterval().getEnd());

    Collection<ErInsertReportDocument> reports = subject.createServicesPki(
        period, Optional.of("UU"), reportView);

    assertThat(reports, hasSize(5));
  }

  @Test
  public void requestsCancelSucceededPkiShouldHaveTwoReports() {
    YearMonth period = YearMonth.now();
    ReservationReportView reportView = new ReservationReportView(period.toInterval().getStart(), period.toInterval().getEnd());

    Collection<ErInsertReportDocument> reports = subject.createRequestsCancelSucceededPki(
        period, Optional.of("UU"), reportView);

    assertThat(reports, hasSize(2));
  }

  @Test
  public void sendReportsShouldContainSixteenReports() {
    YearMonth period = new YearMonth(2011, 11);
    ReservationReportView reportView = new ReservationReportView(period.toInterval().getStart(), period.toInterval().getEnd());

    Collection<ErInsertReportDocument> reports = subject.createAllReports(YearMonth.now(), Optional.of("RUG"), reportView);

    assertThat(reports, hasSize(16));
  }

}