package nl.surfnet.bod.web.noc;

import java.util.ArrayList;
import java.util.List;

import nl.surfnet.bod.web.base.AbstractReportController;
import nl.surfnet.bod.web.security.Security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("nocReportController")
@RequestMapping(ReportController.PAGE_URL)
public class ReportController extends AbstractReportController {
  public static final String PAGE_URL = "noc/report";

  @Override
  protected String getPageUrl() {
    return PAGE_URL;
  }

  @Override
  protected List<String> getAdminGroups() {

    if (Security.isSelectedNocRole()) {
      return new ArrayList<>();
    }

    throw new IllegalStateException("User has no NOC role");
  }

}
