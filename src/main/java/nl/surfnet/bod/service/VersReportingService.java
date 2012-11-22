package nl.surfnet.bod.service;

import java.io.IOException;

import nl.surfnet.bod.vers.SURFnetErStub;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import surfnet_er.ErInsertReportDocument;
import surfnet_er.ErInsertReportDocument.ErInsertReport;
import surfnet_er.ErInsertReportResponseDocument.ErInsertReportResponse;
import surfnet_er.InsertReportInput;

@Service
public class VersReportingService {

  @Value("${vers.url}")
  private String serviceURL = "https://rapportage-test.surfnet.nl:9001/interface.php";
  
  @Value("${vers.user}")
  private String versUserName = "DLPBeheer";
  
  @Value("${vers.password}")
  private String versUserPassword = "Testing123!";

  public String sendReport() throws IOException {
    final SURFnetErStub surFnetErStub = new SURFnetErStub(serviceURL);

    final InsertReportInput reportData = null;
    final ErInsertReport messageBody = ErInsertReport.Factory.newInstance();

    messageBody.setUsername(versUserName);
    messageBody.setPassword(versUserPassword);
    messageBody.setParameters(reportData);

    final ErInsertReportDocument soapCallDocument = ErInsertReportDocument.Factory.newInstance();
    soapCallDocument.setErInsertReport(messageBody);

    final ErInsertReportResponse versRepsonse = surFnetErStub.er_InsertReport(soapCallDocument).getErInsertReportResponse();
    return versRepsonse.getReturnText();
  }

}
