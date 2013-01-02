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
package nl.surfnet.bod;

import nl.surfnet.bod.support.TestExternalSupport;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ReportTestSelenium extends TestExternalSupport {

  private final LocalDateTime reportStart = LocalDateTime.now().plusDays(1).plusHours(1);
  private final LocalDateTime reportEnd = reportStart.plusDays(1).plusHours(1);

  @Before
  public void setUp() {
    final LocalDate startDate = reportStart.toLocalDate();
    final LocalDate endDate = reportEnd.toLocalDate();
    final LocalTime startTime = reportStart.toLocalTime();
    final LocalTime endTime = reportEnd.toLocalTime();

    new ReservationTestSelenium().setup();

    getManagerDriver().switchToUser();
    getUserDriver().createNewReservation("Res Coming", startDate, endDate, startTime, endTime);

    getUserDriver().createNewReservation("Res Active, which will not become active");
  }

  @Ignore("not implemented yet")
  @Test
  public void verifyNocReport() {
    getManagerDriver().switchToNoc();

    getNocDriver().verifyReport();
  }

}
