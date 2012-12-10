/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.service;

import java.io.IOException;

import javax.annotation.PostConstruct;

import nl.surfnet.bod.vers.SURFnetErStub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import surfnet_er.ErInsertReportDocument;
import surfnet_er.ErInsertReportDocument.ErInsertReport;
import surfnet_er.ErInsertReportResponseDocument.ErInsertReportResponse;
import surfnet_er.InsertReportInput;

@Service
public class VersReportingService {

  @Value("${vers.url}")
  private String serviceURL = "https://rapportage-test.surfnet.nl:9011/interface.php";

  @Value("${vers.user}")
  private String versUserName;

  @Value("${vers.password}")
  private String versUserPassword;

  private SURFnetErStub surFnetErStub;

  private final Logger log = LoggerFactory.getLogger(getClass());

  @PostConstruct
  void init() throws IOException {
    surFnetErStub = new SURFnetErStub(serviceURL);
  }

  public VersResponse sendReport() throws IOException {
    final ErInsertReportDocument soapCallDocument = ErInsertReportDocument.Factory.newInstance();
    soapCallDocument.setErInsertReport(getErInsertReport(getInsertReportInput()));

    final ErInsertReportResponse versRepsonse = surFnetErStub.er_InsertReport(soapCallDocument)
        .getErInsertReportResponse();

    log.warn(versRepsonse.getReturnText());
    return new VersResponse(versRepsonse.getReturnCode(), versRepsonse.getReturnText());
  }

  private ErInsertReport getErInsertReport(final InsertReportInput reportData) {
    final ErInsertReport messageBody = ErInsertReport.Factory.newInstance();

    messageBody.setUsername(versUserName);
    messageBody.setPassword(versUserPassword);
    messageBody.setParameters(reportData);
    return messageBody;
  }

  private InsertReportInput getInsertReportInput() {
    final InsertReportInput insertReportInput = InsertReportInput.Factory.newInstance();
    insertReportInput.setType("Beschikbaarheid2");
    insertReportInput.setUnit("%");
    insertReportInput.setNormComp(">");
    insertReportInput.setNormValue("60");
    insertReportInput.setDepartmentList("NWD");
    insertReportInput.setIsKPI(true);
    insertReportInput.setIsHidden(false);
    insertReportInput.setPeriod("11-2012");
    return insertReportInput;
  }

  public static class VersResponse {
    private final String errorMessage;
    private final int errorCode;

    public VersResponse(final int errorCode, final String errorMessage) {
      this.errorCode = errorCode;
      this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
      return errorCode;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

  }

}
