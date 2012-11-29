package nl.surfnet.bod.web.user;

import java.util.Collection;

import nl.surfnet.bod.web.base.AbstractReportController;
import nl.surfnet.bod.web.security.Security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("userReportController")
@RequestMapping(ReportController.PAGE_URL)
public class ReportController extends AbstractReportController {
  public static final String PAGE_URL = "report";

  @Override
  protected String getPageUrl() {
    return PAGE_URL;
  }

  @Override
  protected Collection<String> getAdminGroups() {
    if (Security.isSelectedUserRole()) {
      return Security.getUserDetails().getUserGroupIds();
    }

    throw new IllegalStateException("User has no user role");
  }
}
