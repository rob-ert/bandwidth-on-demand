package nl.surfnet.bod.service;

import java.io.IOException;

import javax.annotation.PostConstruct;

import nl.surfnet.bod.vers.SURFnetErStub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;

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

  private SURFnetErStub surFnetErStub;
  
  private final Logger log = LoggerFactory.getLogger(getClass());

  @PostConstruct
  @VisibleForTesting
  void init() throws IOException {
    surFnetErStub = new SURFnetErStub(serviceURL);
  }

  public int sendReport() throws IOException {
    final ErInsertReportDocument soapCallDocument = ErInsertReportDocument.Factory.newInstance();
    soapCallDocument.setErInsertReport(getErInsertReport(getInsertReportInput()));

    final ErInsertReportResponse versRepsonse = surFnetErStub.er_InsertReport(soapCallDocument)
        .getErInsertReportResponse();
    
    log.warn(versRepsonse.getReturnText());
    return versRepsonse.getReturnCode();
  }

  private ErInsertReport getErInsertReport(InsertReportInput reportData) {
    final ErInsertReport messageBody = ErInsertReport.Factory.newInstance();

    messageBody.setUsername(versUserName);
    messageBody.setPassword(versUserPassword);
    messageBody.setParameters(reportData);
    return messageBody;
  }

  private InsertReportInput getInsertReportInput() {
    final InsertReportInput insertReportInput = InsertReportInput.Factory.newInstance();
    insertReportInput.setInstance("instance");
    insertReportInput.setValue("1");
    insertReportInput.setType("type");
    insertReportInput.setIsKPI(false);
    insertReportInput.setOrganisation("organisation");
    insertReportInput.setPeriod("2011-10");
    return insertReportInput;
  }

}
